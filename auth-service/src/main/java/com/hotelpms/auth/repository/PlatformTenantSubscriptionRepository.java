package com.hotelpms.auth.repository;

import com.hotelpms.auth.domain.PlatformTenantSubscription;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Administrative plan-assignment history. */
public interface PlatformTenantSubscriptionRepository
    extends JpaRepository<PlatformTenantSubscription, UUID> {
  Optional<PlatformTenantSubscription> findFirstByTenantIdAndStatusOrderByStartsAtDesc(
      UUID tenantId, String status);

  List<PlatformTenantSubscription> findAllByTenantIdOrderByStartsAtDesc(UUID tenantId);

  List<PlatformTenantSubscription> findAllByOrderByStartsAtDesc();
}
