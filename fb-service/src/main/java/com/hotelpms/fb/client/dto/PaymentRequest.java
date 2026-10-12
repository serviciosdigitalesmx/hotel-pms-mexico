package com.hotelpms.fb.client.dto;

import java.math.BigDecimal;

/**
 * Payment payload forwarded to billing-service.
 *
 * @param amount payment amount.
 * @param paymentMethod payment method.
 * @param transactionReference transaction reference.
 */
public record PaymentRequest(BigDecimal amount, String paymentMethod, String transactionReference) { }
