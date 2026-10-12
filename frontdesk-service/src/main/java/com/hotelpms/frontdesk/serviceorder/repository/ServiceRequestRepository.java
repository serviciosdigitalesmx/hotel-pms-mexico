package com.hotelpms.frontdesk.serviceorder.repository;

import com.hotelpms.frontdesk.serviceorder.domain.ServiceRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** ServiceRequestRepository domain type. */
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, UUID> {
  /**
   * Executes the operation.
   *
   * @param id parameter.
   *
   * @param hotel parameter.
   *
   * @return operation result.
   */
  Optional<ServiceRequest> findByIdAndHotelId(UUID id, UUID hotel);
  /**
   * Executes the operation.
   *
   * @param hotel parameter.
   *
   * @return operation result.
   */

  List<ServiceRequest> findAllByHotelIdOrderByCreatedAtDesc(UUID hotel);
}
