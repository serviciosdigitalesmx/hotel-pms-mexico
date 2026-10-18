package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Validated payment method supplied when delivering a completed repair order.
 *
 * @param paymentMethod recorded payment method or pending-payment marker
 */
public record ServiceOrderDeliveryRequest(
        @NotBlank @Pattern(regexp = "CASH|CHECK|BANK_DEPOSIT|PAYPAL|CARD|PENDING")
                String paymentMethod) { }
