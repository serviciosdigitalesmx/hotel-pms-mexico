package com.hotelpms.auth.repository;

import com.hotelpms.auth.domain.PlatformTenantSubscription;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Administrative plan-assignment history. */
public interface PlatformTenantSubscriptionRepository
    extends JpaRepository<PlatformTenantSubscription, UUID> {
  /** Finds the newest plan assignment for a tenant and status.
   * @param tenantId tenant identifier
   * @param status assignment status
   * @return newest matching assignment
   */
  Optional<PlatformTenantSubscription> findFirstByTenantIdAndStatusOrderByStartsAtDesc(
      UUID tenantId, String status);

  /** Lists plan assignments for one tenant from newest to oldest.
   * @param tenantId tenant identifier
   * @return tenant plan history
   */
  List<PlatformTenantSubscription> findAllByTenantIdOrderByStartsAtDesc(UUID tenantId);

  /** Lists all plan assignments from newest to oldest.
   * @return platform-wide plan history
   */
  List<PlatformTenantSubscription> findAllByOrderByStartsAtDesc();
}
