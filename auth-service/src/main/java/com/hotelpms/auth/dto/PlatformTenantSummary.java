package com.hotelpms.auth.dto;

import java.time.Instant;
import java.util.UUID;

/** Safe platform-wide tenant summary; contains no credentials or operational records. */
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
