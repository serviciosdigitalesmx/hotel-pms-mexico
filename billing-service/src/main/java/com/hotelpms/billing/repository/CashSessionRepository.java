package com.hotelpms.billing.repository;

import com.hotelpms.billing.domain.CashSession;
import com.hotelpms.billing.domain.CashSessionStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Tenant-scoped cash session access. */
public interface CashSessionRepository extends JpaRepository<CashSession, UUID> {
  /**
   * Finds the most recently opened session in a status.
   *
   * @param hotelId tenant identifier.
   *
   * @param status session status.
   * @return matching session, if any.
   */
  Optional<CashSession> findFirstByHotelIdAndStatusOrderByOpenedAtDesc(
      UUID hotelId, CashSessionStatus status);

  /**
   * Finds a session within a tenant.
   *
   * @param id session identifier.
   *
   * @param hotelId tenant identifier.
   * @return matching session, if any.
   */
  Optional<CashSession> findByIdAndHotelId(UUID id, UUID hotelId);
}
