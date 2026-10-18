package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.UUID;

public record ServiceOrderPaymentUpdateRequest(
        @NotBlank @Pattern(regexp = "CASH|CHECK|BANK_DEPOSIT|PAYPAL|CARD") String paymentMethod,
        @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount,
        UUID requestId) {
    public ServiceOrderPaymentUpdateRequest(final String paymentMethod) {
        this(paymentMethod, null, null);
    }

    public ServiceOrderPaymentUpdateRequest(final String paymentMethod, final BigDecimal amount) {
        this(paymentMethod, amount, null);
    }
}
