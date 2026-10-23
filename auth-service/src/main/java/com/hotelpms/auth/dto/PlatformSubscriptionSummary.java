package com.hotelpms.auth.dto;

import java.time.Instant;
import java.util.UUID;

/** Historical administrative plan assignment; not a representation of external billing.
 * @param id assignment identifier
 * @param tenantId owning tenant identifier
 * @param tenantName owning tenant display name
 * @param planKey assigned plan key
 * @param status assignment lifecycle status
 * @param startsAt assignment start instant
 * @param endsAt assignment end instant, when ended
 * @param createdBy administrator that assigned the plan
 */
public record PlatformSubscriptionSummary(
    UUID id, UUID tenantId, String tenantName, String planKey, String status,
    Instant startsAt, Instant endsAt, String createdBy) { }
