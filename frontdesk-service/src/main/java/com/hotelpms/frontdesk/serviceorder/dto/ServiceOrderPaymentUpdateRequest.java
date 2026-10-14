package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ServiceOrderPaymentUpdateRequest(
    @NotBlank @Pattern(regexp = "CASH|CHECK|BANK_DEPOSIT|PAYPAL|CARD") String paymentMethod) {}
