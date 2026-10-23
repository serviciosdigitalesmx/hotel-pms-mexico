package com.hotelpms.auth.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Safe platform-wide tenant summary; contains no credentials or operational records.
 *
 * @param id tenant identifier
 * @param name tenant display name
 * @param slug tenant URL key
 * @param status current lifecycle status
 * @param createdAt tenant creation instant
 * @param ownerUsername owner login name, when present
 * @param ownerName owner display name, when present
 * @param ownerEmail owner email, when present
 * @param planKey currently assigned plan key, when present
 * @param planActive whether the current plan assignment is active
 * @param onboardingCompleted whether tenant onboarding is complete
 */
public record PlatformTenantSummary(
    UUID id,
    String name,
    String slug,
    String status,
    Instant createdAt,
    String ownerUsername,
    String ownerName,
    String ownerEmail,
    String planKey,
    boolean planActive,
    boolean onboardingCompleted) { }
