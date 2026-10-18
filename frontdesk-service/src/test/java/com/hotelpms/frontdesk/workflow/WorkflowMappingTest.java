package com.hotelpms.frontdesk.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hotelpms.frontdesk.workflow.domain.SemanticPhase;
import com.hotelpms.frontdesk.workflow.domain.WorkflowDefinition;
import com.hotelpms.frontdesk.workflow.domain.WorkflowState;
import com.hotelpms.frontdesk.workflow.domain.WorkflowTransition;

import org.junit.jupiter.api.Test;

import java.util.UUID;

class WorkflowMappingTest {
    @Test
    void tenantWorkflowMapsCustomStateToSharedSemanticPhase() {
        final UUID tenant = UUID.randomUUID();
        final WorkflowDefinition workflow =
                new WorkflowDefinition(tenant, "electronics-repair", "Repair");
        final WorkflowState diagnosis =
                new WorkflowState(
                        workflow,
                        "bench-check",
                        "Bench check",
                        SemanticPhase.DIAGNOSIS,
                        true,
                        false);
        final WorkflowState quote =
                new WorkflowState(
                        workflow,
                        "waiting-approval",
                        "Waiting approval",
                        SemanticPhase.AUTHORIZATION,
                        false,
                        false);
        final WorkflowTransition transition =
                new WorkflowTransition(
                        workflow, diagnosis, quote, "submit-quote", "workflow.transition");

        assertEquals(tenant, workflow.getHotelId());
        assertEquals(SemanticPhase.AUTHORIZATION, transition.getToState().getSemanticPhase());
        assertEquals("waiting-approval", transition.getToState().getKey());
    }
}
