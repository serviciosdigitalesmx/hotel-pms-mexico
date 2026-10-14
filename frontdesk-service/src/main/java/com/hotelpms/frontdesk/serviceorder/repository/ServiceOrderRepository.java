package com.hotelpms.frontdesk.serviceorder.repository;

import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;

/** ServiceOrderRepository domain type. */
public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, UUID> {
  /**
   * Executes the operation.
   *
   * @param id parameter.
   *
   * @param hotelId parameter.
   *
   * @return operation result.
   */
  Optional<ServiceOrder> findByIdAndHotelId(UUID id, UUID hotelId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select o from ServiceOrder o where o.id = :id and o.hotelId = :hotelId")
  Optional<ServiceOrder> findForUpdateByIdAndHotelId(@Param("id") UUID id, @Param("hotelId") UUID hotelId);
  /**
   * Executes the operation.
   *
   * @param hotelId parameter.
   *
   * @return operation result.
   */

  List<ServiceOrder> findAllByHotelIdOrderByCreatedAtDesc(UUID hotelId);

  Page<ServiceOrder> findAllByHotelId(UUID hotelId, Pageable pageable);
  /**
   * Executes the operation.
   *
   * @param hotelId parameter.
   *
   * @param customerId parameter.
   *
   * @return operation result.
   */

  List<ServiceOrder> findAllByHotelIdAndCustomerIdOrderByCreatedAtDesc(
      UUID hotelId, UUID customerId);
}
