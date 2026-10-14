package com.hotelpms.frontdesk.inventory.repository;

import com.hotelpms.frontdesk.inventory.domain.StockReservation;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** StockReservationRepository domain type. */
public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {
  /**
   * Executes the operation.
   *
   * @param hotel parameter.
   *
   * @param order parameter.
   *
   * @param product parameter.
   *
   * @return operation result.
   */
  Optional<StockReservation> findByHotelIdAndServiceOrderIdAndProductId(
      UUID hotel, UUID order, UUID product);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<StockReservation> findForUpdateByHotelIdAndServiceOrderIdAndProductId(
      UUID hotel, UUID order, UUID product);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  List<StockReservation> findAllByHotelIdAndServiceOrderIdOrderByCreatedAtAsc(
      UUID hotel, UUID order);

  @Query("select reservation.productId, sum(reservation.quantity) from StockReservation reservation "
      + "where reservation.hotelId = :hotelId and reservation.status = 'CONSUMED' "
      + "and reservation.consumedAt >= :from and reservation.consumedAt < :to "
      + "group by reservation.productId order by sum(reservation.quantity) desc")
  List<Object[]> findTopConsumedProducts(@Param("hotelId") UUID hotelId,
      @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  @Query("select coalesce(sum(reservation.quantity), 0) from StockReservation reservation "
      + "where reservation.hotelId = :hotelId and reservation.productId = :productId "
      + "and reservation.status = 'RESERVED'")
  Long sumReservedQuantityByHotelAndProduct(
      @Param("hotelId") UUID hotelId, @Param("productId") UUID productId);
}
