package com.hotelpms.frontdesk.workflow.repository;

import com.hotelpms.frontdesk.workflow.domain.WorkflowTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, UUID> {
    List<WorkflowTransition> findAllByWorkflowId(UUID workflowId);

    /**
     * Keep the tenant predicate in the transition query itself.  Workflow IDs are
     * opaque, but tenant scoping must not depend on their global uniqueness.
     */
    @Query("select t from WorkflowTransition t join t.workflow w " +
            "where w.id = :workflowId and w.hotelId = :hotelId " +
            "and t.fromState.key = :fromStateKey and t.key = :transitionKey")
    Optional<WorkflowTransition> findByTenantAndWorkflowIdAndFromStateKeyAndKey(
            @Param("hotelId") UUID hotelId,
            @Param("workflowId") UUID workflowId,
            @Param("fromStateKey") String fromStateKey,
            @Param("transitionKey") String transitionKey);
}
