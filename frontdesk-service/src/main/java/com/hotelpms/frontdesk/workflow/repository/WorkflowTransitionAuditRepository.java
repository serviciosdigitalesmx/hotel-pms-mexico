package com.hotelpms.frontdesk.workflow.repository;

import com.hotelpms.frontdesk.workflow.domain.WorkflowTransitionAudit;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** WorkflowTransitionAuditRepository domain type. */
public interface WorkflowTransitionAuditRepository
    extends JpaRepository<WorkflowTransitionAudit, UUID> {
  /**
   * Executes the operation.
   *
   * @param hotelId parameter.
   *
   * @return operation result.
   */
  List<WorkflowTransitionAudit> findAllByHotelIdOrderByCreatedAtDesc(UUID hotelId);
}
