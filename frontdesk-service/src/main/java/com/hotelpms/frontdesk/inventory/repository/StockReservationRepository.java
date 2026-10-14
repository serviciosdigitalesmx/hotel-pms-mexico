package com.hotelpms.frontdesk.inventory.repository;

import com.hotelpms.frontdesk.inventory.domain.StockReservation;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.Lock;
import java.util.Optional;
import java.util.UUID;
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

  @Query("select coalesce(sum(reservation.quantity), 0) from StockReservation reservation "
      + "where reservation.hotelId = :hotelId and reservation.productId = :productId "
      + "and reservation.status = 'RESERVED'")
  Long sumReservedQuantityByHotelAndProduct(
      @Param("hotelId") UUID hotelId, @Param("productId") UUID productId);
}
