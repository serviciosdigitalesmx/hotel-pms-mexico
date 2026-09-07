package com.hotelpms.frontdesk.assistant.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DeterministicParserTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 17);
    private static final String GUEST_NAME = "roberto";
    private static final String ROOM_TYPE_SIMPLE = "sencilla";
    private static final String OCCUPANT_COUNT = "2";
    private static final String DATE_2026_08_17 = "2026-08-17";
    private static final String DATE_2026_08_18 = "2026-08-18";
    private static final String DATE_2026_08_19 = "2026-08-19";
    private static final String DATE_2027_01_02 = "2027-01-02";

    private final DeterministicParser parser = new DeterministicParser();

    @Test
    void extractsIndependentCheckInSlotsWithoutInventingDates() {
        final var parsed =
                parser.parse("Check-in para Roberto, 2 adultos, sencilla", TODAY);

        assertThat(parsed.intent())
                .isEqualTo(LocalIntent.PREPARE_CHECK_IN);

        assertThat(parsed.entities())
                .containsEntry(
                        DeterministicParser.SLOT_GUEST_QUERY,
                        GUEST_NAME)
                .containsEntry(
                        DeterministicParser.SLOT_OCCUPANT_COUNT,
                        OCCUPANT_COUNT)
                .containsEntry(
                        DeterministicParser.SLOT_ROOM_TYPE,
                        ROOM_TYPE_SIMPLE)
                .doesNotContainKeys(
                        DeterministicParser.SLOT_CHECK_IN,
                        DeterministicParser.SLOT_CHECK_OUT,
                        DeterministicParser.SLOT_SINGLE_DATE);
    }

    @ParameterizedTest
    @MethodSource("singleDateFormats")
    void parsesGeneralSingleDateFormats(
            final String input,
            final String expected) {

        assertThat(parser.parse(input, TODAY).entities())
                .containsEntry(DeterministicParser.SLOT_SINGLE_DATE, expected);
    }

    static Stream<Arguments> singleDateFormats() {
        return Stream.of(
                Arguments.of("17 agosto", DATE_2026_08_17),
                Arguments.of("17 de agosto", DATE_2026_08_17),
                Arguments.of("18 agosto", DATE_2026_08_18),
                Arguments.of("17/08", DATE_2026_08_17),
                Arguments.of("18/08/2026", DATE_2026_08_18),
                Arguments.of("18/08/26", DATE_2026_08_18),
                Arguments.of("2026-08-18", DATE_2026_08_18),
                Arguments.of("hoy", DATE_2026_08_17),
                Arguments.of("mañana", DATE_2026_08_18),
                Arguments.of("pasado mañana", DATE_2026_08_19)
        );
    }

    @ParameterizedTest
    @MethodSource("dateRangeFormats")
    void parsesGeneralDateRangeFormats(
            final String input,
            final String expectedCheckIn,
            final String expectedCheckOut) {

        assertThat(parser.parse(input, TODAY).entities())
                .containsEntry(
                        DeterministicParser.SLOT_CHECK_IN,
                        expectedCheckIn)
                .containsEntry(
                        DeterministicParser.SLOT_CHECK_OUT,
                        expectedCheckOut);
    }

    static Stream<Arguments> dateRangeFormats() {
        return Stream.of(
                Arguments.of(
                        "17 agosto, salida 19 agosto",
                        DATE_2026_08_17,
                        DATE_2026_08_19),
                Arguments.of(
                        "entra 17 de agosto y sale 19 de agosto",
                        DATE_2026_08_17,
                        DATE_2026_08_19),
                Arguments.of(
                        "del 17 agosto al 19 agosto",
                        DATE_2026_08_17,
                        DATE_2026_08_19),
                Arguments.of(
                        "entrada 17/08, salida 19/08",
                        DATE_2026_08_17,
                        DATE_2026_08_19),
                Arguments.of(
                        "del 17/08/2026 al 19/08/2026",
                        DATE_2026_08_17,
                        DATE_2026_08_19),
                Arguments.of(
                        "check-in 2026-08-17 salida 2026-08-19",
                        DATE_2026_08_17,
                        DATE_2026_08_19),
                Arguments.of(
                        "entra hoy y sale mañana",
                        DATE_2026_08_17,
                        DATE_2026_08_18),
                Arguments.of(
                        "llega hoy y se va pasado mañana",
                        DATE_2026_08_17,
                        DATE_2026_08_19)
        );
    }

    @Test
    void rollsMonthDayWithoutYearToNextOccurrenceWhenAlreadyPast() {
        assertThat(parser.parse("2 enero", TODAY).entities())
                .containsEntry(
                        DeterministicParser.SLOT_SINGLE_DATE,
                        DATE_2027_01_02);
    }

    @Test
    void rejectsInvalidCalendarDates() {
        assertThat(parser.parse("31 febrero", TODAY).entities())
                .doesNotContainKeys(
                        DeterministicParser.SLOT_SINGLE_DATE,
                        DeterministicParser.SLOT_CHECK_IN,
                        DeterministicParser.SLOT_CHECK_OUT);
    }

    @Test
    void supportsNaturalCheckInPhrases() {
        assertThat(
                parser.parse(
                        "checkin de Roberto para dos personas en sencilla",
                        TODAY).entities())
                .containsEntry(
                        DeterministicParser.SLOT_GUEST_QUERY,
                        GUEST_NAME)
                .containsEntry(
                        DeterministicParser.SLOT_OCCUPANT_COUNT,
                        OCCUPANT_COUNT)
                .containsEntry(
                        DeterministicParser.SLOT_ROOM_TYPE,
                        ROOM_TYPE_SIMPLE);

        assertThat(
                parser.parse(
                        "ingresa a Roberto en una sencilla",
                        TODAY).entities())
                .containsEntry(
                        DeterministicParser.SLOT_GUEST_QUERY,
                        GUEST_NAME)
                .containsEntry(
                        DeterministicParser.SLOT_ROOM_TYPE,
                        ROOM_TYPE_SIMPLE);
    }

    @Test
    void classifiesAvailabilityWithoutGroq() {
        assertThat(
                parser.parse(
                        "qué habitaciones hay disponibles",
                        TODAY).intent())
                .isEqualTo(LocalIntent.ROOM_AVAILABILITY);

        assertThat(
                parser.parse(
                        "qué cuartos están libres",
                        TODAY).intent())
                .isEqualTo(LocalIntent.ROOM_AVAILABILITY);
    }
}
