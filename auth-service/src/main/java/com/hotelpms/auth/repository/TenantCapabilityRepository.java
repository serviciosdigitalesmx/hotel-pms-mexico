package com.hotelpms.auth.repository;

import com.hotelpms.auth.domain.TenantCapability;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Provides persistence operations for tenant capability settings. */
public interface TenantCapabilityRepository
    extends JpaRepository<TenantCapability, TenantCapability.Key> {
  /**
   * Finds a capability setting by tenant and capability key.
   *
   * @param tenantId tenant identifier
   * @param capabilityKey capability key
   * @return the matching setting, if present
   */
  Optional<TenantCapability> findByTenantIdAndCapabilityKey(UUID tenantId, String capabilityKey);
}
