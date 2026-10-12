package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain type.
 *
 * @param branchId value.
 *
 * @param customerId value.
 *
 * @param deviceId value.
 *
 * @param summary value.
 */
public record ServiceOrderRequest(
    @NotNull UUID customerId, UUID deviceId, UUID branchId, @NotBlank String summary,
    @NotBlank String reportedFailure, @NotBlank String intakeChecklist,
    @FutureOrPresent LocalDateTime promisedAt, String intakeEvidence) { }
