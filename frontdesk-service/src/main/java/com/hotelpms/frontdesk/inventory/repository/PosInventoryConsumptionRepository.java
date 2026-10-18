package com.hotelpms.frontdesk.inventory.repository;

import com.hotelpms.frontdesk.inventory.domain.PosInventoryConsumption;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

/** Tenant-scoped POS inventory idempotency records. */
public interface PosInventoryConsumptionRepository
        extends JpaRepository<PosInventoryConsumption, UUID> {
    /**
     * Checks whether a POS order product was already consumed for a tenant.
     *
     * @param hotelId tenant identifier
     * @param orderId POS order identifier
     * @param productId inventory product identifier
     * @return whether the consumption record exists
     */
    boolean existsByHotelIdAndOrderIdAndProductId(UUID hotelId, UUID orderId, UUID productId);

    /**
     * Finds a tenant-scoped POS inventory consumption record.
     *
     * @param hotelId tenant identifier
     * @param orderId POS order identifier
     * @param productId inventory product identifier
     * @return consumption record, if present
     */
    Optional<PosInventoryConsumption> findByHotelIdAndOrderIdAndProductId(
            UUID hotelId, UUID orderId, UUID productId);
}
