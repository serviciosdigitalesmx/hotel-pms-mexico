package com.hotelpms.frontdesk.client.dto;

import java.util.UUID;

/** Acknowledgement that an event was durably accepted by the notification outbox. */
public record NotificationEventResponse(UUID id, String eventKey, String status, int attempts) {}
