package com.hotelpms.auth.repository;

import com.hotelpms.auth.domain.TenantCapability;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantCapabilityRepository extends JpaRepository<TenantCapability, TenantCapability.Key> {
    Optional<TenantCapability> findByTenantIdAndCapabilityKey(UUID tenantId, String capabilityKey);
}
