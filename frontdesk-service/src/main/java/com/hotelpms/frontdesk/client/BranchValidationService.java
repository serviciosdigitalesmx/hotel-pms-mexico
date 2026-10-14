package com.hotelpms.frontdesk.client;

import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Validates operational branch identifiers against the authenticated tenant in auth-service. */
@Service
@RequiredArgsConstructor
public class BranchValidationService {
  private final BranchClient branches;

  public void requireActiveBranch(final UUID hotelId, final UUID branchId) {
    if (branchId == null) throw new IllegalArgumentException("BRANCH_REQUIRED");
    final Map<String, Object> branch = branches.validateBranch(branchId);
    if (!branchId.toString().equals(String.valueOf(branch.get("id")))
        || !hotelId.toString().equals(String.valueOf(branch.get("hotelId")))
        || !Boolean.TRUE.equals(branch.get("active"))) {
      throw new IllegalArgumentException("BRANCH_NOT_FOUND");
    }
  }
}
