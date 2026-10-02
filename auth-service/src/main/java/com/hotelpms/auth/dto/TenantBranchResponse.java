package com.hotelpms.auth.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read-only branch response. The {@code hotelId} is always included so clients
 * and downstream services can verify tenant+branch binding.
 *
 * @param id        branch UUID
 * @param hotelId   tenant UUID that owns the branch
 * @param name      branch display name
 * @param active    whether the branch may currently be selected
 * @param createdAt branch creation timestamp
 */
public record TenantBranchResponse(
        UUID id,
        UUID hotelId,
        String name,
        boolean active,
        LocalDateTime createdAt) {
}
