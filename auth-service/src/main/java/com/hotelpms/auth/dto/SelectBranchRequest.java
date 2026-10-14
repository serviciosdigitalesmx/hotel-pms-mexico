package com.hotelpms.auth.dto;

import java.util.UUID;

/**
 * Request payload for selecting a branch. The branch is validated against the authenticated tenant
 * and the user's branch membership before a scoped token is issued.
 *
 * @param branchId the branch UUID, or null to return to tenant-wide scope for owners/admins
 */
public record SelectBranchRequest(UUID branchId) { }
