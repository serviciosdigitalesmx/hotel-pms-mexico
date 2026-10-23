package com.hotelpms.auth.dto;

import java.time.Instant;
import java.util.UUID;

/** Safe platform audit projection. */
public record PlatformAuditSummary(
    UUID id, String actorUsername, String action, UUID targetTenantId, Instant occurredAt, String metadataJson) { }
