package com.hotelpms.auth.repository;

import com.hotelpms.auth.domain.UserCapabilityGrant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Tenant-scoped persistence for user capability grants. */
public interface UserCapabilityGrantRepository extends JpaRepository<UserCapabilityGrant, UUID> {
  /**
   * Lists capability overrides for one tenant user in stable key order.
   *
   * @param tenantId tenant identifier
   * @param userId user identifier
   * @return explicit capability overrides
   */
  List<UserCapabilityGrant> findAllByTenantIdAndUserIdOrderByCapabilityKeyAsc(
      UUID tenantId, UUID userId);

  /**
   * Finds a branch-specific grant, if present.
   *
   * @param tenantId tenant identifier
   * @param userId user identifier
   * @param branchId branch identifier
   * @param capabilityKey capability key
   * @return matching grant
   */
  Optional<UserCapabilityGrant> findByTenantIdAndUserIdAndBranchIdAndCapabilityKey(
      UUID tenantId, UUID userId, UUID branchId, String capabilityKey);

  /**
   * Finds a tenant-wide grant, if present.
   *
   * @param tenantId tenant identifier
   * @param userId user identifier
   * @param capabilityKey capability key
   * @return matching grant
   */
  Optional<UserCapabilityGrant> findByTenantIdAndUserIdAndBranchIdIsNullAndCapabilityKey(
      UUID tenantId, UUID userId, String capabilityKey);
}
