package com.hotelpms.auth.dto;

import com.hotelpms.auth.domain.UserCapabilityGrant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Safe API representation of a user capability grant.
 *
 * @param id grant identifier
 * @param tenantId tenant identifier
 * @param userId user identifier
 * @param branchId optional branch identifier
 * @param capabilityKey capability key
 * @param enabled whether the grant is enabled
 * @param updatedAt update timestamp
 */
public record UserCapabilityGrantResponse(
    UUID id,
    UUID tenantId,
    UUID userId,
    UUID branchId,
    String capabilityKey,
    boolean enabled,
    LocalDateTime updatedAt) {

  /** Maps a persisted grant to its API representation. */
  public static UserCapabilityGrantResponse from(final UserCapabilityGrant grant) {
    return new UserCapabilityGrantResponse(
        grant.getId(),
        grant.getTenantId(),
        grant.getUserId(),
        grant.getBranchId(),
        grant.getCapabilityKey(),
        grant.isEnabled(),
        grant.getUpdatedAt());
  }
}
