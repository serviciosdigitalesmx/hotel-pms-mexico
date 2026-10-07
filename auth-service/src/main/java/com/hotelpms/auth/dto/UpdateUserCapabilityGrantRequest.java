package com.hotelpms.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request to set one user's tenant or branch-scoped capability.
 *
 * @param capabilityKey capability key
 * @param enabled whether the grant is enabled
 * @param branchId optional branch scope
 */
public record UpdateUserCapabilityGrantRequest(
    @NotBlank String capabilityKey, @NotNull Boolean enabled, UUID branchId) { }
