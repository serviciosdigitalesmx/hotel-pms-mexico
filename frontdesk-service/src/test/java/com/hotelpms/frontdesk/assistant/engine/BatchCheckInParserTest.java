package com.hotelpms.frontdesk.assistant.engine;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BatchCheckInParserTest {

    private final DeterministicParser parser =
            new DeterministicParser();

    @Test
    void detectsTenRoomGroupCheckIn() {

        final LocalDate today =
                LocalDate.of(2026, 8, 17);

        final var parsed =
                parser.parse(
                        "Haz check in en 10 habitaciones triples, "
                                + "entran hoy y salen mañana",
                        today);

        assertThat(parsed.intent())
                .isEqualTo(LocalIntent.BATCH_CHECK_IN);

        assertThat(parsed.entities())
                .containsEntry("batchCount", "10")
                .containsEntry("roomType", "triple")
                .containsEntry("checkInDate", "2026-08-17")
                .containsEntry("checkOutDate", "2026-08-18");
    }

    @Test
    void normalSingleCheckInRemainsSingle() {

        final var parsed =
                parser.parse(
                        "checkin de Roberto para dos personas en sencilla",
                        LocalDate.of(2026, 8, 17));

        assertThat(parsed.intent())
                .isEqualTo(LocalIntent.PREPARE_CHECK_IN);
    }

    @Test
    void detectsGroupPhraseWithoutHardcodedTen() {

        final var parsed =
                parser.parse(
                        "checkin de grupo para 7 habitaciones dobles",
                        LocalDate.of(2026, 8, 17));

        assertThat(parsed.intent())
                .isEqualTo(LocalIntent.BATCH_CHECK_IN);

        assertThat(parsed.entities())
                .containsEntry("batchCount", "7");
    }
}
