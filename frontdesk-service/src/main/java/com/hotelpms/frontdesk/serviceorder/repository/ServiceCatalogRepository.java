package com.hotelpms.frontdesk.serviceorder.repository;

import com.hotelpms.frontdesk.serviceorder.domain.ServiceCatalogEntry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Tenant-scoped service catalog persistence. */
public interface ServiceCatalogRepository extends JpaRepository<ServiceCatalogEntry, UUID> {
    /**
     * Lists services for a tenant ordered by display name.
     *
     * @param tenantId tenant identifier
     * @return tenant-owned service entries
     */
    List<ServiceCatalogEntry> findAllByTenantIdOrderByNameAsc(UUID tenantId);

    /**
     * Finds a catalog entry by identifier and tenant.
     *
     * @param id service identifier
     * @param tenantId tenant identifier
     * @return matching service, if present
     */
    Optional<ServiceCatalogEntry> findByIdAndTenantId(UUID id, UUID tenantId);

    /**
     * Checks whether a tenant already has a service with the given name.
     *
     * @param tenantId tenant identifier
     * @param name service name
     * @return whether a case-insensitive match exists
     */
    boolean existsByTenantIdAndNameIgnoreCase(UUID tenantId, String name);

    /**
     * Checks for a duplicate name excluding one catalog entry.
     *
     * @param tenantId tenant identifier
     * @param name proposed service name
     * @param id entry excluded from the check
     * @return whether another case-insensitive match exists
     */
    boolean existsByTenantIdAndNameIgnoreCaseAndIdNot(UUID tenantId, String name, UUID id);
}
