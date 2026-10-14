package com.hotelpms.frontdesk.serviceorder.repository;

import com.hotelpms.frontdesk.serviceorder.domain.WarrantyClaim;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** WarrantyClaimRepository domain type. */
public interface WarrantyClaimRepository extends JpaRepository<WarrantyClaim, UUID> {
  /**
   * Executes the operation.
   *
   * @param id parameter.
   *
   * @param hotelId parameter.
   *
   * @return operation result.
   */
  Optional<WarrantyClaim> findByIdAndHotelId(UUID id, UUID hotelId);
  /**
   * Executes the operation.
   *
   * @param hotelId parameter.
   *
   * @param customerId parameter.
   *
   * @return operation result.
   */

  List<WarrantyClaim> findAllByHotelIdAndCustomerIdOrderByCreatedAtDesc(
      UUID hotelId, UUID customerId);

  List<WarrantyClaim> findAllByHotelIdAndServiceOrderIdOrderByCreatedAtDesc(
      UUID hotelId, UUID serviceOrderId);
}
