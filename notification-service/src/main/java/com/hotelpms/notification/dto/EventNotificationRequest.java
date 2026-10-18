package com.hotelpms.notification.dto;

import com.hotelpms.notification.validation.NotificationValidationLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Internal event envelope; payload is the JSON form of the DTO selected by eventType.
 *
 * @param hotelId tenant UUID matching the HMAC-authenticated X-Auth-Hotel header
 * @param eventKey idempotency key, unique within this tenant
 * @param eventType transactional email type or OPERATIONAL_EMAIL
 * @param payload serialized event DTO, limited by
 *     {@link NotificationValidationLimits#MAX_EVENT_PAYLOAD_LENGTH}
 */
public record EventNotificationRequest(
    @NotNull UUID hotelId,
    @NotBlank @Size(max = NotificationValidationLimits.MAX_EVENT_KEY_LENGTH) String eventKey,
    @NotBlank @Pattern(regexp = "RESERVATION_CONFIRMED|CHECKIN|CHECKOUT|QUOTATION|OPERATIONAL_EMAIL")
        @Size(max = NotificationValidationLimits.MAX_EVENT_TYPE_LENGTH) String eventType,
    @NotBlank @Size(max = NotificationValidationLimits.MAX_EVENT_PAYLOAD_LENGTH) String payload) {}
