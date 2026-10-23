package com.hotelpms.auth.dto;

import java.time.Instant;
import java.util.UUID;

/** Safe platform audit projection.
 * @param id event identifier
 * @param actorUsername username that performed the operation
 * @param action operation name
 * @param targetTenantId affected tenant identifier, when applicable
 * @param occurredAt event timestamp
 * @param metadataJson non-secret structured event metadata
 */
public record PlatformAuditSummary(
    UUID id, String actorUsername, String action, UUID targetTenantId, Instant occurredAt, String metadataJson) { }
