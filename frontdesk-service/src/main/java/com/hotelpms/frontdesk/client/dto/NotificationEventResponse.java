package com.hotelpms.frontdesk.client.dto;

import java.util.UUID;

/**
 * Acknowledgement that an event was durably accepted by the notification outbox.
 *
 * @param id notification event identifier
 * @param eventKey accepted idempotency key
 * @param status current delivery status
 * @param attempts delivery attempts so far
 */
public record NotificationEventResponse(UUID id, String eventKey, String status, int attempts) { }
