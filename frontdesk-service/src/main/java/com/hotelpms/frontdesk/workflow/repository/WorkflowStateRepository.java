package com.hotelpms.frontdesk.workflow.repository;

import com.hotelpms.frontdesk.workflow.domain.WorkflowState;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** WorkflowStateRepository domain type. */
public interface WorkflowStateRepository extends JpaRepository<WorkflowState, UUID> {
    /**
     * Executes the operation.
     *
     * @param workflowId parameter.
     * @return operation result.
     */
    List<WorkflowState> findAllByWorkflowId(UUID workflowId);

    /**
     * Finds a state by workflow and key.
     *
     * @param workflowId workflow identifier.
     * @param key state key.
     * @return matching state.
     */
    Optional<WorkflowState> findByWorkflowIdAndKey(UUID workflowId, String key);

    boolean existsByWorkflowIdAndKeyAndActiveTrue(UUID workflowId, String key);

    /**
     * Finds and locks an active workflow state.
     *
     * @param workflowId workflow identifier.
     * @param key state key.
     * @param hotelId hotel identifier.
     * @return locked workflow state.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            "select s from WorkflowState s join fetch s.workflow w where s.workflow.id=:workflowId"
                + " and s.key=:key and w.hotelId=:hotelId and w.active=true")
    Optional<WorkflowState> findLocked(
            @Param("workflowId") UUID workflowId,
            @Param("key") String key,
            @Param("hotelId") UUID hotelId);
}
