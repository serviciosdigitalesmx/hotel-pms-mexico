package com.hotelpms.billing.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Request to reverse part or all of an existing payment.
 *
 * @param amount amount to reverse.
 * @param reference optional reference.
 */
public record RefundRequest(
    @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal amount,
    @Size(max = 100) String reference) { }
