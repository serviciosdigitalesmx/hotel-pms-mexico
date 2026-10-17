package com.hotelpms.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Plain-text operational alert delivered to a business-configured recipient. */
public record OperationalEmailRequest(
    @Email @NotBlank @Size(max = 254) String recipient,
    @NotBlank @Size(max = 180) String subject,
    @NotBlank @Size(max = 12_000) String message,
    @Size(max = 120) String hotelName) {}
