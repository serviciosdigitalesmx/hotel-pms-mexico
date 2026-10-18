package com.hotelpms.frontdesk.inventory.repository;

import com.hotelpms.frontdesk.inventory.domain.StockReservation;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Tenant- and branch-scoped stock reservation queries for service orders. */
public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {
    /**
     * Finds a reservation for one tenant-owned order and product.
     *
     * @param hotel tenant identifier
     * @param order service order identifier
     * @param product inventory product identifier
     * @return matching reservation, if present
     */
    Optional<StockReservation> findByHotelIdAndServiceOrderIdAndProductId(
            UUID hotel, UUID order, UUID product);

    /**
     * Locks a reservation for an order and product.
     *
     * @param hotel tenant identifier
     * @param order service order identifier
     * @param product inventory product identifier
     * @return locked reservation, if present
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<StockReservation> findForUpdateByHotelIdAndServiceOrderIdAndProductId(
            UUID hotel, UUID order, UUID product);

    /**
     * Locks all reservations for one tenant-owned order in creation order.
     *
     * @param hotel tenant identifier
     * @param order service order identifier
     * @return locked reservations
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<StockReservation> findAllByHotelIdAndServiceOrderIdOrderByCreatedAtAsc(
            UUID hotel, UUID order);

    /**
     * Aggregates product consumption over a tenant date range and optional branch.
     *
     * @param hotelId tenant identifier
     * @param branchId optional branch filter
     * @param from inclusive start timestamp
     * @param to exclusive end timestamp
     * @return rows containing product identifiers and consumed totals
     */
    @Query(
            "select reservation.productId, sum(reservation.quantity) from StockReservation"
                + " reservation where reservation.hotelId = :hotelId and reservation.status ="
                + " 'CONSUMED' and (:branchId is null or reservation.branchId = :branchId) and"
                + " reservation.consumedAt >= :from and reservation.consumedAt < :to group by"
                + " reservation.productId order by sum(reservation.quantity) desc")
    List<Object[]> findTopConsumedProducts(
            @Param("hotelId") UUID hotelId,
            @Param("branchId") UUID branchId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    /**
     * Totals reserved quantity for a product across a tenant's branches.
     *
     * @param hotelId tenant identifier
     * @param productId inventory product identifier
     * @return reserved quantity
     */
    @Query(
            "select coalesce(sum(reservation.quantity), 0) from StockReservation reservation "
                    + "where reservation.hotelId = :hotelId and reservation.productId = :productId "
                    + "and reservation.status = 'RESERVED'")
    Long sumReservedQuantityByHotelAndProduct(
            @Param("hotelId") UUID hotelId, @Param("productId") UUID productId);

    /**
     * Totals reserved quantity for a product at one tenant branch.
     *
     * @param hotelId tenant identifier
     * @param branchId branch identifier
     * @param productId inventory product identifier
     * @return reserved quantity
     */
    @Query(
            "select coalesce(sum(reservation.quantity), 0) from StockReservation reservation "
                    + "where reservation.hotelId = :hotelId and reservation.branchId = :branchId "
                    + "and reservation.productId = :productId and reservation.status = 'RESERVED'")
    Long sumReservedQuantityByHotelAndBranchAndProduct(
            @Param("hotelId") UUID hotelId,
            @Param("branchId") UUID branchId,
            @Param("productId") UUID productId);
}
