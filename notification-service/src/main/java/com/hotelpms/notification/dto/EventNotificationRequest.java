package com.hotelpms.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Event envelope used by internal notification producers. */
public record EventNotificationRequest(@NotNull UUID hotelId, @NotBlank String eventKey,
    @NotBlank String eventType, @NotBlank String payload) {}
