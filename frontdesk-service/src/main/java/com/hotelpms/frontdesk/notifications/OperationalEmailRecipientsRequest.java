package com.hotelpms.frontdesk.notifications;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

/** Validated per-event distribution lists for operational email alerts. */
public record OperationalEmailRecipientsRequest(
        @NotNull @Size(max = 6)
                Map<
                                @NotBlank
                                @Pattern(
                                        regexp =
                                                "ORDER_ASSIGNED|STOCK_LOW|DELIVERY_DUE|PAYMENT_DUE|ORDER_READY|DAY_SUMMARY")
                                String,
                                @NotNull @Size(max = 20) List<
                                        @NotBlank @Email @Size(max = 254) String>>
                        recipients) {}
