package com.hotelpms.frontdesk.inventory.repository;

import com.hotelpms.frontdesk.inventory.domain.PosInventoryConsumption;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** Tenant-scoped POS inventory idempotency records. */
public interface PosInventoryConsumptionRepository
        extends JpaRepository<PosInventoryConsumption, UUID> {
    boolean existsByHotelIdAndOrderIdAndProductId(UUID hotelId, UUID orderId, UUID productId);

    java.util.Optional<com.hotelpms.frontdesk.inventory.domain.PosInventoryConsumption>
            findByHotelIdAndOrderIdAndProductId(UUID hotelId, UUID orderId, UUID productId);
}
