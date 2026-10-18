package com.hotelpms.notification.dto;

import com.hotelpms.notification.validation.NotificationValidationLimits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Plain-text operational alert delivered to a business-configured recipient.
 *
 * @param recipient destination email address
 * @param subject message subject
 * @param message plain-text message body
 * @param hotelName hotel display name used in the email, if available
 */
public record OperationalEmailRequest(
    @Email @NotBlank @Size(max = NotificationValidationLimits.MAX_EMAIL_ADDRESS_LENGTH)
        String recipient,
    @NotBlank @Size(max = NotificationValidationLimits.MAX_SUBJECT_LENGTH) String subject,
    @NotBlank @Size(max = NotificationValidationLimits.MAX_MESSAGE_LENGTH) String message,
    @Size(max = NotificationValidationLimits.MAX_HOTEL_NAME_LENGTH) String hotelName) {}
