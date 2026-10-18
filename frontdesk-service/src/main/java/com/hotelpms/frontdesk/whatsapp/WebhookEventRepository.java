package com.hotelpms.frontdesk.whatsapp;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** WebhookEventRepository domain type. */
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, UUID> {
    /**
     * Executes the operation.
     *
     * @param hotelId parameter.
     * @param eventId parameter.
     * @return operation result.
     */
    boolean existsByHotelIdAndEventId(UUID hotelId, String eventId);
}
