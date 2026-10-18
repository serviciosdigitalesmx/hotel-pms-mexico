package com.hotelpms.frontdesk.serviceorder.repository;

import com.hotelpms.frontdesk.serviceorder.domain.ServiceCatalogEntry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceCatalogRepository extends JpaRepository<ServiceCatalogEntry, UUID> {
    List<ServiceCatalogEntry> findAllByTenantIdOrderByNameAsc(UUID tenantId);

    Optional<ServiceCatalogEntry> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndNameIgnoreCase(UUID tenantId, String name);

    boolean existsByTenantIdAndNameIgnoreCaseAndIdNot(UUID tenantId, String name, UUID id);
}
