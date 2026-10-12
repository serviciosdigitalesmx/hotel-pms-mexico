package com.hotelpms.frontdesk.inventory.repository;

import com.hotelpms.frontdesk.inventory.domain.Supplier;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** SupplierRepository domain type. */
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
  /**
   * Executes the operation.
   *
   * @param hotel parameter.
   *
   * @return operation result.
   */
  List<Supplier> findAllByHotelId(UUID hotel);
  /**
   * Executes the operation.
   *
   * @param id parameter.
   *
   * @param hotel parameter.
   *
   * @return operation result.
   */

  Optional<Supplier> findByIdAndHotelId(UUID id, UUID hotel);
}
