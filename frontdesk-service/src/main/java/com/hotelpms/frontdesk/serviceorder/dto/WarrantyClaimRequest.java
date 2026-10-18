package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Customer's request to evaluate a repair order under warranty.
 *
 * @param serviceOrderId repair order identifier
 * @param customerId owning customer identifier
 * @param expiresAt warranty expiration date
 * @param description reported warranty issue
 */
public record WarrantyClaimRequest(
        @NotNull UUID serviceOrderId,
        @NotNull UUID customerId,
        @NotNull @FutureOrPresent LocalDate expiresAt,
        @NotBlank String description) { }
