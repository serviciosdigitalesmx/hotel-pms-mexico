package com.hotelpms.frontdesk.serviceorder.repository;

import com.hotelpms.frontdesk.serviceorder.domain.WarrantyClaim;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Tenant-scoped warranty claim lookups for Fixi repair orders. */
public interface WarrantyClaimRepository extends JpaRepository<WarrantyClaim, UUID> {
    /**
     * Executes the operation.
     *
     * @param id parameter.
     * @param hotelId parameter.
     * @return operation result.
     */
    Optional<WarrantyClaim> findByIdAndHotelId(UUID id, UUID hotelId);

    /**
     * Executes the operation.
     *
     * @param hotelId parameter.
     * @param customerId parameter.
     * @return operation result.
     */
    List<WarrantyClaim> findAllByHotelIdAndCustomerIdOrderByCreatedAtDesc(
            UUID hotelId, UUID customerId);

    /**
     * Lists warranty claims attached to a tenant-owned service order.
     *
     * @param hotelId tenant identifier
     * @param serviceOrderId repair order identifier
     * @return matching claims in reverse creation order
     */
    List<WarrantyClaim> findAllByHotelIdAndServiceOrderIdOrderByCreatedAtDesc(
            UUID hotelId, UUID serviceOrderId);
}
