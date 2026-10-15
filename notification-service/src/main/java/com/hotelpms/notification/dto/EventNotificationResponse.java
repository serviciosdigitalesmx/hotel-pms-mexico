package com.hotelpms.notification.dto;

import com.hotelpms.notification.domain.NotificationDelivery;
import java.util.UUID;

/** Acknowledgement that an event has been durably accepted for delivery. */
public record EventNotificationResponse(UUID id, String eventKey, String status, int attempts) {
  public static EventNotificationResponse from(final NotificationDelivery delivery) {
    return new EventNotificationResponse(
        delivery.getId(), delivery.getEventKey(), delivery.getStatus().name(),
        delivery.getAttempts());
  }
}
