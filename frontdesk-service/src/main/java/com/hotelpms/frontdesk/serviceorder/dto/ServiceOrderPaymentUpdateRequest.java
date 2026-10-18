package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Validated incremental payment for a tenant-owned repair order.
 *
 * @param paymentMethod accepted payment channel
 * @param amount payment amount
 * @param requestId optional idempotency identifier
 */
public record ServiceOrderPaymentUpdateRequest(
        @NotBlank @Pattern(regexp = "CASH|CHECK|BANK_DEPOSIT|PAYPAL|CARD") String paymentMethod,
        @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount,
        UUID requestId) {
    /**
     * Creates a payment request without amount-level idempotency details.
     *
     * @param paymentMethod accepted payment channel
     */
    public ServiceOrderPaymentUpdateRequest(final String paymentMethod) {
        this(paymentMethod, null, null);
    }

    /**
     * Creates a payment request without an explicit request identifier.
     *
     * @param paymentMethod accepted payment channel
     * @param amount payment amount
     */
    public ServiceOrderPaymentUpdateRequest(final String paymentMethod, final BigDecimal amount) {
        this(paymentMethod, amount, null);
    }
}
