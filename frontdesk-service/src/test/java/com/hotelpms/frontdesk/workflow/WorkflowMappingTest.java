package com.hotelpms.frontdesk.workflow;

import com.hotelpms.frontdesk.workflow.domain.*;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class WorkflowMappingTest {
    @Test
    void tenantWorkflowMapsCustomStateToSharedSemanticPhase() {
        UUID tenant = UUID.randomUUID();
        WorkflowDefinition workflow = new WorkflowDefinition(tenant, "electronics-repair", "Repair");
        WorkflowState diagnosis = new WorkflowState(workflow, "bench-check", "Bench check", SemanticPhase.DIAGNOSIS, true, false);
        WorkflowState quote = new WorkflowState(workflow, "waiting-approval", "Waiting approval", SemanticPhase.AUTHORIZATION, false, false);
        WorkflowTransition transition = new WorkflowTransition(workflow, diagnosis, quote, "submit-quote", "workflow.transition");

        assertEquals(tenant, workflow.getHotelId());
        assertEquals(SemanticPhase.AUTHORIZATION, transition.getToState().getSemanticPhase());
        assertEquals("waiting-approval", transition.getToState().getKey());
    }
}
