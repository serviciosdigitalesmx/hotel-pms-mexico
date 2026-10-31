package com.hotelpms.frontdesk.serviceorder.repository;

import com.hotelpms.frontdesk.serviceorder.domain.WarrantyClaim;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Collection;

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
     * Lists the tenant customer's warranty claims attached to authorized branches.
     *
     * @param hotelId tenant identifier
     * @param customerId customer identifier
     * @param branchIds authorized branch identifiers
     * @return matching tenant-owned claims
     */
    @Query(value = "select w.* from warranty_claims w join service_orders o "
            + "on o.id = w.service_order_id and o.hotel_id = w.hotel_id "
            + "where w.hotel_id = :hotelId and w.customer_id = :customerId "
            + "and o.branch_id in (:branchIds) order by w.created_at desc", nativeQuery = true)
    List<WarrantyClaim> findAllByTenantCustomerAndBranches(
            @Param("hotelId") UUID hotelId,
            @Param("customerId") UUID customerId,
            @Param("branchIds") Collection<UUID> branchIds);

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
