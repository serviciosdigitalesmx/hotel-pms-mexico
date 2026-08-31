package com.hotelpms.frontdesk.assistant.engine;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Classifies supported local intents and extracts high-confidence entities. */
@Component
public final class DeterministicParser {

    static final String SLOT_GUEST_QUERY = "guestSearchQuery";
    static final String SLOT_OCCUPANT_COUNT = "occupantCount";
    static final String SLOT_ROOM_TYPE = "roomType";
    static final String SLOT_CHECK_IN = "checkInDate";
    static final String SLOT_CHECK_OUT = "checkOutDate";
    static final String SLOT_SINGLE_DATE = "singleDate";
    static final String SLOT_BATCH_COUNT = "batchCount";

    private static final String CHECK_IN_WORD = "checkin";

    private static final Pattern EMAIL = Pattern.compile(
            "[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern ISO_DATE =
            Pattern.compile("\\b(\\d{4})-(\\d{1,2})-(\\d{1,2})\\b");

    private static final Pattern SLASH_DATE =
            Pattern.compile("\\b(\\d{1,2})/(\\d{1,2})(?:/(\\d{2,4}))?\\b");

    private static final String MONTH_NAMES =
            "enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|setiembre|"
                    + "octubre|noviembre|diciembre";

    private static final Pattern TEXT_DATE = Pattern.compile(
            "\\b(\\d{1,2})\\s+(?:de\\s+)?(" + MONTH_NAMES + ")"
                    + "(?:\\s+(?:de\\s+)?(\\d{4}))?\\b");

    private static final Pattern OCCUPANTS = Pattern.compile(
            "\\b(\\d+|un[oa]?|dos|tres|cuatro|cinco|seis|siete|ocho|nueve|diez)\\s*"
                    + "(adultos?|personas?|huespedes?)\\b");

    private static final Pattern BATCH_COUNT = Pattern.compile(
            "\\b(\\d+|un[oa]?|dos|tres|cuatro|cinco|seis|siete|ocho|nueve|diez)\\s*"
                    + "(habitaciones?|cuartos?|checkins?|llegadas?)\\b");

    private static final Pattern ROOM_TYPE = Pattern.compile(
            "\\b(sencill[ao]|doble|suite|estandar|presidencial|triple|cuadruple)(?:s|es)?\\b");

    private static final Pattern CHECK_IN_GUEST = Pattern.compile(
            "(?:checkin|registrar entrada)\\s+(?:para|de)\\s+(.+)");

    private static final Pattern ACTION_GUEST = Pattern.compile(
            "(?:ingresa(?:r)?|hospeda(?:r)?)\\s+a\\s+(.+)");

    private static final Pattern LEADING_GUEST = Pattern.compile(
            "^([a-z][a-z ]{1,80}?)\\s+(?:entra|llega)\\b");

    private static final Pattern GUEST_STOP = Pattern.compile(
            "\\s+(?:para\\s+)?(?:\\d+|un[oa]?|dos|tres|cuatro|cinco|seis|siete|ocho|nueve|diez)\\s*"
                    + "(?:adultos?|personas?|huespedes?)\\b"
                    + "|\\s+en\\s+(?:una?\\s+)?"
                    + "(?:sencill[ao]|doble|suite|estandar|presidencial|triple|cuadruple)\\b"
                    + "|\\s+(?:hoy|manana|pasado manana|del|desde|checkin|salida|sale|se va)\\b"
                    + "|[,.;]",
            Pattern.CASE_INSENSITIVE);

    private static final Map<String, Integer> NUMBER_WORDS = Map.ofEntries(
            Map.entry("un", 1),
            Map.entry("una", 1),
            Map.entry("uno", 1),
            Map.entry("dos", 2),
            Map.entry("tres", 3),
            Map.entry("cuatro", 4),
            Map.entry("cinco", 5),
            Map.entry("seis", 6),
            Map.entry("siete", 7),
            Map.entry("ocho", 8),
            Map.entry("nueve", 9),
            Map.entry("diez", 10));

    private static final Map<String, Integer> MONTHS = Map.ofEntries(
            Map.entry("enero", 1),
            Map.entry("febrero", 2),
            Map.entry("marzo", 3),
            Map.entry("abril", 4),
            Map.entry("mayo", 5),
            Map.entry("junio", 6),
            Map.entry("julio", 7),
            Map.entry("agosto", 8),
            Map.entry("septiembre", 9),
            Map.entry("setiembre", 9),
            Map.entry("octubre", 10),
            Map.entry("noviembre", 11),
            Map.entry("diciembre", 12));

    /**
     * Parses one operator message relative to the hotel's current date.
     *
     * @param input operator message
     * @param today current date in the hotel's configured timezone
     * @return classified local command and independently extracted entities
     */
    public ParsedCommand parse(final String input, final LocalDate today) {
        final String normalized = normalize(input);
        final Map<String, String> entities = new LinkedHashMap<>();

        extractEmail(input, entities);
        extractOccupants(normalized, entities);
        extractBatchCount(normalized, entities);
        extractRoomType(normalized, entities);
        extractDates(normalized, today, entities);
        extractGuest(normalized, entities);

        return new ParsedCommand(classify(normalized), entities);
    }

    private static LocalIntent classify(final String text) {
        if (text.matches("^(cancelar|cancela|abortar|salir|detener)(?:\\s+.*)?$")) {
            return LocalIntent.CANCEL;
        }

        if (text.matches("^(si|confirmo|confirmar|de acuerdo|ok|dale|va)$")) {
            return LocalIntent.CONFIRM;
        }

        if (text.matches("^(no|rechazar|declino|negativo)$")) {
            return LocalIntent.DECLINE;
        }

        /*
         * Batch/group check-in must win over the normal CHECK_IN classifier.
         *
         * Examples:
         * - "haz checkin en 10 habitaciones triples"
         * - "checkin de grupo"
         * - "checkin masivo"
         * - "checkin 10 cuartos"
         */
        if ((text.contains("checkin")
                && (text.contains("grupo")
                    || text.contains("masivo")
                    || BATCH_COUNT.matcher(text).find()))
                || containsAny(
                        text,
                        "checkin de grupo",
                        "checkin grupal",
                        "checkin masivo")) {
            return LocalIntent.BATCH_CHECK_IN;
        }

        if (containsAny(text, "crear huesped", "registrar huesped", "nuevo huesped")) {
            return LocalIntent.CREATE_GUEST;
        }

        if (containsAny(text, "buscar huesped", "encontrar huesped", "localizar huesped")) {
            return LocalIntent.FIND_GUEST;
        }

        if (containsAny(
                text,
                "habitaciones disponibles",
                "habitaciones hay disponibles",
                "habitacion disponible",
                "cuartos disponibles",
                "cuarto disponible",
                "que hay libre",
                "que cuartos estan libres",
                "disponibilidad")) {
            return LocalIntent.ROOM_AVAILABILITY;
        }

        if (containsAny(text, "checkin", "registrar entrada", "hospedar a", "hospeda a", "ingresa a")
                || text.matches(".*\\b(?:entra|llega)\\b.*")) {
            return LocalIntent.PREPARE_CHECK_IN;
        }

        return LocalIntent.UNKNOWN;
    }

    private static void extractEmail(
            final String input,
            final Map<String, String> entities) {

        final Matcher matcher = EMAIL.matcher(input == null ? "" : input);

        if (matcher.find()) {
            entities.put("newGuestEmail", matcher.group());
        }
    }

    private static void extractOccupants(
            final String text,
            final Map<String, String> entities) {

        final Matcher matcher = OCCUPANTS.matcher(text);

        if (!matcher.find()) {
            return;
        }

        final String raw = matcher.group(1);

        final Integer count = raw.chars().allMatch(Character::isDigit)
                ? Integer.valueOf(raw)
                : NUMBER_WORDS.get(raw);

        if (count != null) {
            entities.put(SLOT_OCCUPANT_COUNT, count.toString());
        }
    }

    private static void extractBatchCount(
            final String text,
            final Map<String, String> entities) {

        final Matcher matcher = BATCH_COUNT.matcher(text);

        if (!matcher.find()) {
            return;
        }

        final String raw = matcher.group(1);

        final Integer count = raw.chars().allMatch(Character::isDigit)
                ? Integer.valueOf(raw)
                : NUMBER_WORDS.get(raw);

        if (count != null) {
            entities.put(SLOT_BATCH_COUNT, count.toString());
        }
    }

    private static void extractRoomType(
            final String text,
            final Map<String, String> entities) {

        final Matcher matcher = ROOM_TYPE.matcher(text);

        if (matcher.find()) {
            entities.put(SLOT_ROOM_TYPE, matcher.group(1));
        }
    }

    private static void extractDates(
            final String text,
            final LocalDate today,
            final Map<String, String> entities) {

        final List<DateMention> mentions = extractDateMentions(text, today);

        if (mentions.isEmpty()) {
            return;
        }

        if (mentions.size() >= 2) {
            assignDateRange(text, mentions, entities);
            return;
        }

        final LocalDate date = mentions.getFirst().date();

        if (hasCheckInSemantics(text)) {
            entities.put(SLOT_CHECK_IN, date.toString());
        } else if (hasCheckOutSemantics(text)) {
            entities.put(SLOT_CHECK_OUT, date.toString());
        } else {
            entities.put(SLOT_SINGLE_DATE, date.toString());
        }
    }

    private static List<DateMention> extractDateMentions(
            final String text,
            final LocalDate today) {

        final List<DateMention> result = new ArrayList<>();

        addRelativeDates(text, today, result);
        addIsoDates(text, result);
        addSlashDates(text, today, result);
        addTextDates(text, today, result);

        return result.stream()
                .sorted((a, b) -> Integer.compare(a.start(), b.start()))
                .filter(DeterministicParser::notDuplicateMention)
                .toList();
    }

    private static boolean notDuplicateMention(final DateMention mention) {
        return true;
    }

    private static void addRelativeDates(
            final String text,
            final LocalDate today,
            final List<DateMention> result) {

        final Pattern pattern = Pattern.compile(
                "\\b(pasado\\s+manana|manana|hoy)\\b");

        final Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            final String value = matcher.group(1);

            final LocalDate date = switch (value) {
                case "hoy" -> today;
                case "manana" -> today.plusDays(1);
                case "pasado manana" -> today.plusDays(2);
                default -> throw new IllegalStateException("Unsupported relative date: " + value);
            };

            result.add(new DateMention(matcher.start(), matcher.end(), date));
        }
    }

    private static void addIsoDates(
            final String text,
            final List<DateMention> result) {

        final Matcher matcher = ISO_DATE.matcher(text);

        while (matcher.find()) {
            try {
                final LocalDate date = LocalDate.of(
                        Integer.parseInt(matcher.group(1)),
                        Integer.parseInt(matcher.group(2)),
                        Integer.parseInt(matcher.group(3)));

                result.add(new DateMention(matcher.start(), matcher.end(), date));
            } catch (final DateTimeException ignored) {
                // Invalid calendar dates are not extracted.
            }
        }
    }

    private static void addSlashDates(
            final String text,
            final LocalDate today,
            final List<DateMention> result) {

        final Matcher matcher = SLASH_DATE.matcher(text);

        while (matcher.find()) {
            final int day = Integer.parseInt(matcher.group(1));
            final int month = Integer.parseInt(matcher.group(2));

            final String rawYear = matcher.group(3);

            final Integer explicitYear = rawYear == null
                    ? null
                    : normalizeYear(Integer.parseInt(rawYear));

            final LocalDate resolved = explicitYear == null
                    ? resolveMonthDay(day, month, today)
                    : safeDate(explicitYear, month, day);

            if (resolved != null) {
                result.add(new DateMention(matcher.start(), matcher.end(), resolved));
            }
        }
    }

    private static void addTextDates(
            final String text,
            final LocalDate today,
            final List<DateMention> result) {

        final Matcher matcher = TEXT_DATE.matcher(text);

        while (matcher.find()) {
            final int day = Integer.parseInt(matcher.group(1));
            final Integer month = MONTHS.get(matcher.group(2));

            if (month == null) {
                continue;
            }

            final String rawYear = matcher.group(3);

            final LocalDate resolved = rawYear == null
                    ? resolveMonthDay(day, month, today)
                    : safeDate(Integer.parseInt(rawYear), month, day);

            if (resolved != null) {
                result.add(new DateMention(matcher.start(), matcher.end(), resolved));
            }
        }
    }

    private static void assignDateRange(
            final String text,
            final List<DateMention> mentions,
            final Map<String, String> entities) {

        final DateMention first = mentions.get(0);
        final DateMention second = mentions.get(1);

        if (looksLikeExplicitRange(text)
                || hasBothDateSemantics(text)
                || second.start() > first.start()) {

            entities.put(SLOT_CHECK_IN, first.date().toString());
            entities.put(SLOT_CHECK_OUT, second.date().toString());
        }
    }

    private static boolean looksLikeExplicitRange(final String text) {
        return text.matches(".*\\bdel\\b.+\\bal\\b.*")
                || text.matches(".*\\bdesde\\b.+\\bhasta\\b.*");
    }

    private static boolean hasBothDateSemantics(final String text) {
        final boolean input = text.matches(
                ".*\\b(?:entra|entrada|llega|llegada|checkin|desde)\\b.*");

        final boolean output = text.matches(
                ".*\\b(?:sale|salida|checkout|se va|hasta)\\b.*");

        return input && output;
    }

    private static boolean hasCheckInSemantics(final String text) {
        return text.matches(
                ".*\\b(?:entra|entrada|llega|llegada|checkin|desde)\\b.*")
                && !hasCheckOutSemantics(text);
    }

    private static boolean hasCheckOutSemantics(final String text) {
        return text.matches(
                ".*\\b(?:sale|salida|checkout|se va|hasta)\\b.*")
                && !text.matches(
                        ".*\\b(?:entra|entrada|llega|llegada|checkin|desde)\\b.*");
    }

    private static LocalDate resolveMonthDay(
            final int day,
            final int month,
            final LocalDate today) {

        LocalDate candidate = safeDate(today.getYear(), month, day);

        if (candidate == null) {
            return null;
        }

        /*
         * For hotel operational dates, a month/day without year means the
         * nearest non-past occurrence. This prevents "2 enero" in December
         * from resolving eleven months backwards.
         */
        if (candidate.isBefore(today)) {
            candidate = safeDate(today.getYear() + 1, month, day);
        }

        return candidate;
    }

    private static LocalDate safeDate(
            final int year,
            final int month,
            final int day) {

        try {
            final YearMonth yearMonth = YearMonth.of(year, month);

            if (day < 1 || day > yearMonth.lengthOfMonth()) {
                return null;
            }

            return LocalDate.of(year, month, day);
        } catch (final DateTimeException ignored) {
            return null;
        }
    }

    private static int normalizeYear(final int year) {
        if (year >= 100) {
            return year;
        }

        return 2000 + year;
    }

    private static void extractGuest(
            final String text,
            final Map<String, String> entities) {

        for (final Pattern pattern : List.of(CHECK_IN_GUEST, ACTION_GUEST, LEADING_GUEST)) {
            final Matcher matcher = pattern.matcher(text);

            if (!matcher.find()) {
                continue;
            }

            final String candidate = trimGuestCandidate(matcher.group(1));

            if (candidate.matches("[a-z][a-z ]{1,80}")) {
                entities.put(SLOT_GUEST_QUERY, candidate);
            }

            return;
        }
    }

    private static String trimGuestCandidate(final String candidate) {
        final Matcher stop = GUEST_STOP.matcher(candidate);

        final String trimmed = stop.find()
                ? candidate.substring(0, stop.start())
                : candidate;

        return trimmed.trim().replaceAll("\\s+", " ");
    }

    static String normalize(final String input) {
        final String decomposed = Normalizer.normalize(
                input == null ? "" : input,
                Normalizer.Form.NFD);

        return decomposed
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replace("check-in", CHECK_IN_WORD)
                .replace("check in", CHECK_IN_WORD)
                .replaceAll("[¿?¡!]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static boolean containsAny(
            final String text,
            final String... phrases) {

        for (final String phrase : phrases) {
            if (text.contains(phrase)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Parsed intent and independently extracted entities.
     *
     * @param intent classified local intent
     * @param entities high-confidence values extracted from the message
     */
    public record ParsedCommand(
            LocalIntent intent,
            Map<String, String> entities) {
    }

    private record DateMention(
            int start,
            int end,
            LocalDate date) {
    }
}
