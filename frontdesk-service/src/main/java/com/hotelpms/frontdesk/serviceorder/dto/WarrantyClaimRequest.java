package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain type.
 *
 * @param customerId value.
 * @param description value.
 * @param expiresAt value.
 * @param serviceOrderId value.
 */
public record WarrantyClaimRequest(
        @NotNull UUID serviceOrderId,
        @NotNull UUID customerId,
        @NotNull @FutureOrPresent LocalDate expiresAt,
        @NotBlank String description) {}
