package com.hotelpms.auth.dto;

import java.time.Instant;
import java.util.UUID;

/** Administrative account directory projection without password or token fields. */
public record PlatformUserSummary(
    UUID id,
    String username,
    String fullName,
    String email,
    String role,
    boolean active,
    UUID tenantId,
    String tenantName,
    Instant createdAt) { }
