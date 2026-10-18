package com.hotelpms.frontdesk.inventory.repository;

import com.hotelpms.frontdesk.inventory.domain.PurchaseOrder;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Tenant-scoped purchase-order persistence with locking for receipt operations. */
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    /**
     * Executes the operation.
     *
     * @param id parameter.
     * @param hotel parameter.
     * @return operation result.
     */
    Optional<PurchaseOrder> findByIdAndHotelId(UUID id, UUID hotel);

    /**
     * Locks a tenant-owned purchase order for update.
     *
     * @param id purchase-order identifier
     * @param hotelId tenant identifier
     * @return locked purchase order, if present
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            "select purchase from PurchaseOrder purchase where purchase.id = :id and"
                + " purchase.hotelId = :hotelId")
    Optional<PurchaseOrder> findForUpdateByIdAndHotelId(
            @Param("id") UUID id, @Param("hotelId") UUID hotelId);

    /**
     * Executes the operation.
     *
     * @param hotel parameter.
     * @return operation result.
     */
    List<PurchaseOrder> findAllByHotelIdOrderByCreatedAtDesc(UUID hotel);
}
