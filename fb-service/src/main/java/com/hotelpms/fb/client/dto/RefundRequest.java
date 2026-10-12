package com.hotelpms.fb.client.dto;

import java.math.BigDecimal;

/**
 * Payment reversal payload forwarded to billing-service.
 *
 * @param amount refund amount.
 * @param reference refund reference.
 */
public record RefundRequest(BigDecimal amount, String reference) { }
