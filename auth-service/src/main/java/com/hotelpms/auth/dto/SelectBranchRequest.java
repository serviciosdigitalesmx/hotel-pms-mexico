package com.hotelpms.auth.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request payload for selecting a branch. The branch is validated against the authenticated tenant
 * and the user's branch membership before a scoped token is issued.
 *
 * @param branchId the branch UUID the user wants to operate in
 */
public record SelectBranchRequest(@NotNull UUID branchId) { }
