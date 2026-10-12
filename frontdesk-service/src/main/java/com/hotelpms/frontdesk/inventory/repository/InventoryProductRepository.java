package com.hotelpms.frontdesk.inventory.repository;

import com.hotelpms.frontdesk.inventory.domain.InventoryProduct;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** InventoryProductRepository domain type. */
public interface InventoryProductRepository extends JpaRepository<InventoryProduct, UUID> {
  /**
   * Executes the operation.
   *
   * @param hotelId parameter.
   *
   * @return operation result.
   */
  List<InventoryProduct> findAllByHotelId(UUID hotelId);
  /**
   * Executes the operation.
   *
   * @param hotelId parameter.
   *
   * @param sku parameter.
   *
   * @return operation result.
   */

  Optional<InventoryProduct> findByHotelIdAndSku(UUID hotelId, String sku);
}
