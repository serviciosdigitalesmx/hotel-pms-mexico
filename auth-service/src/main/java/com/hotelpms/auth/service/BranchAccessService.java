package com.hotelpms.auth.service;

import com.hotelpms.auth.domain.TenantBranch;
import com.hotelpms.auth.dto.CreateBranchRequest;
import com.hotelpms.auth.dto.TenantBranchResponse;
import com.hotelpms.auth.dto.UpdateBranchRequest;
import java.util.List;
import java.util.UUID;

/**
 * Tenant-scoped branch operations. Every lookup validates the branch together with its tenant; a
 * branch UUID alone never authorizes anything.
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
   * Lists tenant user IDs assigned to an active tenant branch.
   *
   * @param hotelId owning tenant UUID
   * @param branchId branch UUID
   * @return member user IDs for this tenant branch
   */
  List<UUID> listBranchMembers(UUID hotelId, UUID branchId);

  /**
   * Creates a branch and immediately gives the requesting user membership.
   *
   * @param hotelId the tenant UUID
   * @param name branch display name
   * @param requestingUsername the authenticated username creating the branch
   * @return the created branch
   */
  TenantBranchResponse createBranch(UUID hotelId, String name, String requestingUsername);

  /**
   * Creates a tenant branch from the validated request and adds the requesting user.
   *
   * @param hotelId tenant UUID
   * @param request branch details
   * @param requestingUsername authenticated actor
   * @return created branch response
   */
  TenantBranchResponse createBranch(
      UUID hotelId, CreateBranchRequest request, String requestingUsername);

  /**
   * Validates that a branch exists and belongs to the given tenant.
   *
   * @param tenantId the tenant UUID
   * @param branchId the branch UUID
   * @return the branch if it is active and belongs to the tenant
   */
  TenantBranch getBranchForTenant(UUID tenantId, UUID branchId);

  /**
   * Validates that a user is allowed to select the given branch: the branch must belong to the
   * tenant and the user must be a member.
   *
   * @param userId the user UUID
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

  /**
   * Lists active branches where the user has a persisted membership.
   *
   * @param username authenticated username
   * @param hotelId tenant identifier
   * @return active branches assigned to that user
   */
  List<TenantBranchResponse> listBranchesForUser(String username, UUID hotelId);

  /**
   * Assigns an employee to a tenant branch.
   *
   * @param hotelId tenant identifier
   * @param userId employee identifier
   * @param branchId branch identifier
   */
  void assignUserToBranch(UUID hotelId, UUID userId, UUID branchId);

  /**
   * Removes an employee's membership from a tenant branch.
   *
   * @param hotelId tenant identifier
   * @param userId employee identifier
   * @param branchId branch identifier
   */
  void removeUserFromBranch(UUID hotelId, UUID userId, UUID branchId);

  /**
   * Renames a branch after verifying it belongs to the tenant.
   *
   * @param hotelId tenant UUID
   * @param branchId branch UUID
   * @param name new display name
   * @return updated branch response
   */
  TenantBranchResponse updateBranch(UUID hotelId, UUID branchId, String name);

  /**
   * Updates editable details of a tenant-owned branch.
   *
   * @param hotelId tenant UUID
   * @param branchId branch UUID
   * @param request validated branch changes
   * @return updated branch response
   */
  TenantBranchResponse updateBranch(UUID hotelId, UUID branchId, UpdateBranchRequest request);

  /**
   * Enables or disables a tenant-owned branch.
   *
   * @param hotelId tenant UUID
   * @param branchId branch UUID
   * @param active requested active state
   */
  void setBranchActive(UUID hotelId, UUID branchId, boolean active);
}
