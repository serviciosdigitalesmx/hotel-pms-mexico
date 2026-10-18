package com.hotelpms.frontdesk.workflow.repository;

import com.hotelpms.frontdesk.workflow.domain.WorkflowTransition;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** WorkflowTransitionRepository domain type. */
public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, UUID> {
    /**
     * Executes the operation.
     *
     * @param workflowId parameter.
     * @return operation result.
     */
    List<WorkflowTransition> findAllByWorkflowId(UUID workflowId);

    /**
     * Keep the tenant predicate in the transition query itself. Workflow IDs are opaque, but tenant
     * scoping must not depend on their global uniqueness.
     *
     * @param hotelId hotel identifier.
     * @param workflowId workflow identifier.
     * @param fromStateKey source state key.
     * @param transitionKey transition key.
     * @return matching transition.
     */
    @Query(
            "select t from WorkflowTransition t join t.workflow w "
                    + "where w.id = :workflowId and w.hotelId = :hotelId "
                    + "and t.fromState.key = :fromStateKey and t.key = :transitionKey")
    Optional<WorkflowTransition> findByTenantAndWorkflowIdAndFromStateKeyAndKey(
            @Param("hotelId") UUID hotelId,
            @Param("workflowId") UUID workflowId,
            @Param("fromStateKey") String fromStateKey,
            @Param("transitionKey") String transitionKey);
}
