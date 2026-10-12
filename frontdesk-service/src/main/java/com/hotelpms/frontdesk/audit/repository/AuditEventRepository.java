package com.hotelpms.frontdesk.audit.repository;

import com.hotelpms.frontdesk.audit.domain.AuditEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Tenant-scoped append-only audit access. */
public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {
  List<AuditEvent> findTop100ByHotelIdOrderByCreatedAtDesc(UUID hotelId);
}
