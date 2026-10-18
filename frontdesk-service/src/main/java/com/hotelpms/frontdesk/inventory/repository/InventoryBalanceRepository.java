package com.hotelpms.frontdesk.inventory.repository;

import com.hotelpms.frontdesk.inventory.domain.InventoryBalance;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** InventoryBalanceRepository domain type. */
public interface InventoryBalanceRepository extends JpaRepository<InventoryBalance, UUID> {
    /**
     * Executes the operation.
     *
     * @param hotelId parameter.
     * @param branchId parameter.
     * @param productId parameter.
     * @return operation result.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InventoryBalance> findByHotelIdAndBranchIdAndProductId(
            UUID hotelId, UUID branchId, UUID productId);

    /**
     * Executes the operation.
     *
     * @param hotelId parameter.
     * @param branchId parameter.
     * @return operation result.
     */
    List<InventoryBalance> findAllByHotelIdAndBranchId(UUID hotelId, UUID branchId);

    @Query(
            "select coalesce(sum(balance.quantity), 0) from InventoryBalance balance "
                    + "where balance.hotelId = :hotelId and balance.productId = :productId")
    Long sumQuantityByHotelAndProduct(
            @Param("hotelId") UUID hotelId, @Param("productId") UUID productId);
}
