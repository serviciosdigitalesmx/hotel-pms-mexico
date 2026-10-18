package com.hotelpms.frontdesk.notifications;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

/**
 * Validated per-event distribution lists for operational email alerts.
 *
 * @param recipients recipient addresses grouped by operational event
 */
public record OperationalEmailRecipientsRequest(
        @NotNull @Size(max = MAX_EVENTS)
                Map<
                                @NotBlank
                                @Pattern(
                                        regexp =
                                                "ORDER_ASSIGNED|STOCK_LOW|DELIVERY_DUE|PAYMENT_DUE|ORDER_READY|DAY_SUMMARY")
                                String,
                                @NotNull @Size(max = MAX_RECIPIENTS_PER_EVENT) List<
                                        @NotBlank @Email @Size(max = MAX_EMAIL_LENGTH) String>>
                        recipients) {
    private static final int MAX_EVENTS = 6;
    private static final int MAX_RECIPIENTS_PER_EVENT = 20;
    private static final int MAX_EMAIL_LENGTH = 254;
}
