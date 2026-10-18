package com.hotelpms.notification.dto;

import com.hotelpms.notification.domain.NotificationDelivery;
import java.util.UUID;

/**
 * Acknowledgement that an event has been durably accepted for delivery.
 *
 * @param id persisted delivery identifier
 * @param eventKey idempotency key supplied by the caller
 * @param status current delivery status
 * @param attempts number of delivery attempts so far
 */
public record EventNotificationResponse(UUID id, String eventKey, String status, int attempts) {
  /**
   * Creates the API acknowledgement from a persisted outbox record.
   *
   * @param delivery persisted delivery record
   * @return acknowledgement response
   */
  public static EventNotificationResponse from(final NotificationDelivery delivery) {
    return new EventNotificationResponse(
        delivery.getId(), delivery.getEventKey(), delivery.getStatus().name(),
        delivery.getAttempts());
  }
}
