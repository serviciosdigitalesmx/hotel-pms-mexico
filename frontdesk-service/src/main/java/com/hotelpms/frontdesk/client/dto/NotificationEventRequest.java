package com.hotelpms.frontdesk.client.dto;

import java.util.UUID;

/**
 * Tenant-scoped idempotent event sent to notification-service's durable outbox.
 *
 * @param hotelId tenant identifier
 * @param eventKey idempotency key
 * @param eventType stable event type
 * @param payload serialized event payload
 */
public record NotificationEventRequest(
        UUID hotelId, String eventKey, String eventType, String payload) { }
