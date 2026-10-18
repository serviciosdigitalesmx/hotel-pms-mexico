package com.hotelpms.frontdesk.client.dto;

import java.util.UUID;

/** Tenant-scoped idempotent event sent to notification-service's durable outbox. */
public record NotificationEventRequest(
        UUID hotelId, String eventKey, String eventType, String payload) {}
