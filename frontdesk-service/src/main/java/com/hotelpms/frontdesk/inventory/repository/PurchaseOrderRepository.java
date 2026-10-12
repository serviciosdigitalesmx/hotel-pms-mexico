package com.hotelpms.frontdesk.inventory.repository;

import com.hotelpms.frontdesk.inventory.domain.PurchaseOrder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** PurchaseOrderRepository domain type. */
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
  /**
   * Executes the operation.
   *
   * @param id parameter.
   *
   * @param hotel parameter.
   *
   * @return operation result.
   */
  Optional<PurchaseOrder> findByIdAndHotelId(UUID id, UUID hotel);
  /**
   * Executes the operation.
   *
   * @param hotel parameter.
   *
   * @return operation result.
   */

  List<PurchaseOrder> findAllByHotelIdOrderByCreatedAtDesc(UUID hotel);
}
