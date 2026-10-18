package com.hotelpms.frontdesk.inventory.repository;

import com.hotelpms.frontdesk.inventory.domain.Supplier;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Tenant-scoped supplier queries used by procurement operations. */
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    /**
     * Lists all suppliers owned by a tenant.
     *
     * @param hotel tenant identifier
     * @return tenant-owned suppliers
     */
    List<Supplier> findAllByHotelId(UUID hotel);

    /**
     * Finds one supplier by its identifier and tenant.
     *
     * @param id supplier identifier
     * @param hotel tenant identifier
     * @return matching supplier, if it belongs to the tenant
     */
    Optional<Supplier> findByIdAndHotelId(UUID id, UUID hotel);

    /**
     * Checks whether a supplier identifier belongs to a tenant.
     *
     * @param id supplier identifier
     * @param hotel tenant identifier
     * @return whether the tenant owns the supplier
     */
    boolean existsByIdAndHotelId(UUID id, UUID hotel);

    /**
     * Checks whether a tax identifier is already registered for a tenant.
     *
     * @param hotel tenant identifier
     * @param taxId normalized tax identifier
     * @return whether a matching supplier exists
     */
    boolean existsByHotelIdAndTaxIdIgnoreCase(UUID hotel, String taxId);

    /**
     * Checks for a tax identifier owned by another supplier in the same tenant.
     *
     * @param hotel tenant identifier
     * @param taxId normalized tax identifier
     * @param id supplier excluded from the uniqueness check
     * @return whether another matching supplier exists
     */
    boolean existsByHotelIdAndTaxIdIgnoreCaseAndIdNot(UUID hotel, String taxId, UUID id);
}
