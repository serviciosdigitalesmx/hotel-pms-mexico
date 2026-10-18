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

/** Branch and tenant scoped stock balance persistence. */
public interface InventoryBalanceRepository extends JpaRepository<InventoryBalance, UUID> {
    /**
     * Finds and locks a branch balance owned by a tenant.
     *
     * @param hotelId tenant identifier
     * @param branchId branch identifier
     * @param productId product identifier
     * @return matching balance, if present
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InventoryBalance> findByHotelIdAndBranchIdAndProductId(
            UUID hotelId, UUID branchId, UUID productId);

    /**
     * Lists product balances for a tenant branch.
     *
     * @param hotelId tenant identifier
     * @param branchId branch identifier
     * @return branch-owned stock balances
     */
    List<InventoryBalance> findAllByHotelIdAndBranchId(UUID hotelId, UUID branchId);

    /**
     * Totals stock quantity for one tenant and product across its branches.
     *
     * @param hotelId tenant identifier
     * @param productId product identifier
     * @return total on-hand quantity
     */
    @Query(
            "select coalesce(sum(balance.quantity), 0) from InventoryBalance balance "
                    + "where balance.hotelId = :hotelId and balance.productId = :productId")
    Long sumQuantityByHotelAndProduct(
            @Param("hotelId") UUID hotelId, @Param("productId") UUID productId);
}
