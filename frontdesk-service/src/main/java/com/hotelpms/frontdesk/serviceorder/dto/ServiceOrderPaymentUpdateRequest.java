package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record ServiceOrderPaymentUpdateRequest(
    @NotBlank @Pattern(regexp = "CASH|CHECK|BANK_DEPOSIT|PAYPAL|CARD") String paymentMethod,
    @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount) {
  public ServiceOrderPaymentUpdateRequest(final String paymentMethod) {
    this(paymentMethod, null);
  }
}
