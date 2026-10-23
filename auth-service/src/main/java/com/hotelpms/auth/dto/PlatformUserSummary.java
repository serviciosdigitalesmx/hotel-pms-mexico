package com.hotelpms.auth.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Administrative account directory projection without password or token fields.
 *
 * @param id account identifier
 * @param username login name
 * @param fullName display name
 * @param email account email
 * @param role tenant-scoped role
 * @param active whether the account is active
 * @param tenantId owning tenant identifier
 * @param tenantName owning tenant display name
 * @param createdAt account creation instant
 */
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
