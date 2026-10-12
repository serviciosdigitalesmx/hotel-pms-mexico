package com.hotelpms.frontdesk.whatsapp;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** WebhookEventRepository domain type. */
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, UUID> {
  /**
   * Executes the operation.
   *
   * @param hotelId parameter.
   *
   * @param eventId parameter.
   *
   * @return operation result.
   */
  boolean existsByHotelIdAndEventId(UUID hotelId, String eventId);
}
