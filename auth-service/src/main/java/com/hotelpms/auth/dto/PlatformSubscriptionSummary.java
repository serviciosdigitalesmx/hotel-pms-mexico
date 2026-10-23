package com.hotelpms.auth.dto;

import java.time.Instant;
import java.util.UUID;

/** Historical administrative plan assignment; not a representation of external billing. */
public record PlatformSubscriptionSummary(
    UUID id, UUID tenantId, String tenantName, String planKey, String status,
    Instant startsAt, Instant endsAt, String createdBy) { }
