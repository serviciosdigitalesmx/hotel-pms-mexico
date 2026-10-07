package com.hotelpms.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for renaming a branch inside the authenticated tenant. Tenant and branch
 * ownership are resolved server-side.
 *
 * @param name new branch display name
 */
public record UpdateBranchRequest(
    @NotBlank @Size(max = TenantBranchRequestConstraints.MAX_NAME_LENGTH) String name) { }
