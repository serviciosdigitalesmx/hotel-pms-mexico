package com.hotelpms.frontdesk.audit.repository;

import com.hotelpms.frontdesk.audit.domain.AuditEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Tenant-scoped append-only audit access. */
public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {
  /**
   * Lists the latest audit events created by the specified tenant.
   *
   * @param hotelId tenant identifier
   * @return up to 100 tenant-owned events in reverse creation order
   */
  List<AuditEvent> findTop100ByHotelIdOrderByCreatedAtDesc(UUID hotelId);
}
