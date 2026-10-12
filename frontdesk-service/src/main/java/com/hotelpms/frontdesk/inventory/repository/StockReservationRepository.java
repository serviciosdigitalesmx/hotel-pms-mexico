package com.hotelpms.frontdesk.inventory.repository;

import com.hotelpms.frontdesk.inventory.domain.StockReservation;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
