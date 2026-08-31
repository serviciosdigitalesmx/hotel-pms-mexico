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

    private final DeterministicParser parser = new DeterministicParser();

    @Test
    void extractsIndependentCheckInSlotsWithoutInventingDates() {
        final var parsed =
                parser.parse("Check-in para Roberto, 2 adultos, sencilla", TODAY);

        assertThat(parsed.intent())
                .isEqualTo(LocalIntent.PREPARE_CHECK_IN);

        assertThat(parsed.entities())
                .containsEntry("guestSearchQuery", "roberto")
                .containsEntry("occupantCount", "2")
                .containsEntry("roomType", "sencilla")
                .doesNotContainKeys("checkInDate", "checkOutDate", "singleDate");
    }

    @ParameterizedTest
    @MethodSource("singleDateFormats")
    void parsesGeneralSingleDateFormats(
            final String input,
            final String expected) {

        assertThat(parser.parse(input, TODAY).entities())
                .containsEntry("singleDate", expected);
    }

    static Stream<Arguments> singleDateFormats() {
        return Stream.of(
                Arguments.of("17 agosto", "2026-08-17"),
                Arguments.of("17 de agosto", "2026-08-17"),
                Arguments.of("18 agosto", "2026-08-18"),
                Arguments.of("17/08", "2026-08-17"),
                Arguments.of("18/08/2026", "2026-08-18"),
                Arguments.of("18/08/26", "2026-08-18"),
                Arguments.of("2026-08-18", "2026-08-18"),
                Arguments.of("hoy", "2026-08-17"),
                Arguments.of("mañana", "2026-08-18"),
                Arguments.of("pasado mañana", "2026-08-19")
        );
    }

    @ParameterizedTest
    @MethodSource("dateRangeFormats")
    void parsesGeneralDateRangeFormats(
            final String input,
            final String expectedCheckIn,
            final String expectedCheckOut) {

        assertThat(parser.parse(input, TODAY).entities())
                .containsEntry("checkInDate", expectedCheckIn)
                .containsEntry("checkOutDate", expectedCheckOut);
    }

    static Stream<Arguments> dateRangeFormats() {
        return Stream.of(
                Arguments.of(
                        "17 agosto, salida 19 agosto",
                        "2026-08-17",
                        "2026-08-19"),
                Arguments.of(
                        "entra 17 de agosto y sale 19 de agosto",
                        "2026-08-17",
                        "2026-08-19"),
                Arguments.of(
                        "del 17 agosto al 19 agosto",
                        "2026-08-17",
                        "2026-08-19"),
                Arguments.of(
                        "entrada 17/08, salida 19/08",
                        "2026-08-17",
                        "2026-08-19"),
                Arguments.of(
                        "del 17/08/2026 al 19/08/2026",
                        "2026-08-17",
                        "2026-08-19"),
                Arguments.of(
                        "check-in 2026-08-17 salida 2026-08-19",
                        "2026-08-17",
                        "2026-08-19"),
                Arguments.of(
                        "entra hoy y sale mañana",
                        "2026-08-17",
                        "2026-08-18"),
                Arguments.of(
                        "llega hoy y se va pasado mañana",
                        "2026-08-17",
                        "2026-08-19")
        );
    }

    @Test
    void rollsMonthDayWithoutYearToNextOccurrenceWhenAlreadyPast() {
        assertThat(parser.parse("2 enero", TODAY).entities())
                .containsEntry("singleDate", "2027-01-02");
    }

    @Test
    void rejectsInvalidCalendarDates() {
        assertThat(parser.parse("31 febrero", TODAY).entities())
                .doesNotContainKeys(
                        "singleDate",
                        "checkInDate",
                        "checkOutDate");
    }

    @Test
    void supportsNaturalCheckInPhrases() {
        assertThat(
                parser.parse(
                        "checkin de Roberto para dos personas en sencilla",
                        TODAY).entities())
                .containsEntry("guestSearchQuery", "roberto")
                .containsEntry("occupantCount", "2")
                .containsEntry("roomType", "sencilla");

        assertThat(
                parser.parse(
                        "ingresa a Roberto en una sencilla",
                        TODAY).entities())
                .containsEntry("guestSearchQuery", "roberto")
                .containsEntry("roomType", "sencilla");
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
