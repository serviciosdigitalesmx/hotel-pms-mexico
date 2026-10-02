package com.hotelpms.frontdesk.workflow.repository;

import com.hotelpms.frontdesk.workflow.domain.WorkflowInstance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from WorkflowInstance i where i.hotelId = :hotelId and i.workflowId = :workflowId and i.aggregateId = :aggregateId")
    Optional<WorkflowInstance> findLockedByHotelIdAndWorkflowIdAndAggregateId(
            @Param("hotelId") UUID hotelId, @Param("workflowId") UUID workflowId, @Param("aggregateId") UUID aggregateId);

    Optional<WorkflowInstance> findByHotelIdAndWorkflowIdAndAggregateId(
            UUID hotelId, UUID workflowId, UUID aggregateId);
}
