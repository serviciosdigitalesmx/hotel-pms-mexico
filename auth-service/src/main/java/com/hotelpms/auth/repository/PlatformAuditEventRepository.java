package com.hotelpms.auth.repository;

import com.hotelpms.auth.domain.PlatformAuditEvent;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Global platform audit queries used only by platform administration services. */
public interface PlatformAuditEventRepository extends JpaRepository<PlatformAuditEvent, UUID> {
  /**
   * Searches platform audit events by optional tenant and action filters.
   *
   * @param tenantId optional target tenant
   * @param action optional action name
   * @param pageable bounded page request
   * @return newest matching audit events
   */
  @Query(
      value = """
          SELECT event FROM PlatformAuditEvent event
          WHERE (:tenantId IS NULL OR event.targetTenantId = :tenantId)
            AND (:action IS NULL OR event.action = :action)
          ORDER BY event.occurredAt DESC
          """,
      countQuery = """
          SELECT count(event) FROM PlatformAuditEvent event
          WHERE (:tenantId IS NULL OR event.targetTenantId = :tenantId)
            AND (:action IS NULL OR event.action = :action)
          """)
  Page<PlatformAuditEvent> search(
      @Param("tenantId") UUID tenantId,
      @Param("action") String action,
      Pageable pageable);
}
