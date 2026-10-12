package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Domain type.
 *
 * @param minutes value.
 *
 * @param note value.
 *
 * @param userId value.
 */
public record TimeEntryRequest(
    @NotNull UUID userId, @Positive int minutes, @Size(max = 1000) String note) { }
