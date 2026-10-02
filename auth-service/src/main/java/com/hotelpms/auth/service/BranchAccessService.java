package com.hotelpms.auth.service;

import com.hotelpms.auth.domain.TenantBranch;
import com.hotelpms.auth.dto.TenantBranchResponse;

import java.util.List;
import java.util.UUID;

/**
 * Tenant-scoped branch operations. Every lookup validates the branch together
 * with its tenant; a branch UUID alone never authorizes anything.
 */
public interface BranchAccessService {

    /**
     * Lists active branches for a tenant.
     *
     * @param hotelId the tenant UUID
     * @return branch responses for that tenant only
     */
    List<TenantBranchResponse> listBranches(UUID hotelId);

    /**
     * Creates a branch and immediately gives the requesting user membership.
     *
     * @param hotelId           the tenant UUID
     * @param name              branch display name
     * @param requestingUsername the authenticated username creating the branch
     * @return the created branch
     */
    TenantBranchResponse createBranch(UUID hotelId, String name, String requestingUsername);

    /**
     * Validates that a branch exists and belongs to the given tenant.
     *
     * @param tenantId the tenant UUID
     * @param branchId the branch UUID
     * @return the branch if it is active and belongs to the tenant
     */
    TenantBranch getBranchForTenant(UUID tenantId, UUID branchId);

    /**
     * Validates that a user is allowed to select the given branch: the branch
     * must belong to the tenant and the user must be a member.
     *
     * @param userId   the user UUID
     * @param tenantId the tenant UUID
     * @param branchId the branch UUID
     * @return the branch if selection is allowed
     */
    TenantBranch assertUserCanSelectBranch(UUID userId, UUID tenantId, UUID branchId);

    /**
     * Returns branch responses for the branches the user is member of within a tenant.
     *
     * @param userId the user UUID
     * @param hotelId the tenant UUID
     * @return membership-scoped branch responses
     */
    List<TenantBranchResponse> listBranchesForUser(UUID userId, UUID hotelId);
}
