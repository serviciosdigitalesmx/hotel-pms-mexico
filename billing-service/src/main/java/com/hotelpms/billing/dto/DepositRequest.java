package com.hotelpms.billing.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Request for an idempotent invoice deposit.
 *
 * @param amount positive deposit amount
 * @param referenceKey unique tenant-scoped replay protection key
 */
public record DepositRequest(
    @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal amount,
    @NotBlank @Size(max = 180) String referenceKey) {}
