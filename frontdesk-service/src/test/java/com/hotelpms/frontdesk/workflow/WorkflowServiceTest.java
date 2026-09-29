package com.hotelpms.frontdesk.workflow;

import com.hotelpms.frontdesk.exception.ConflictException;
import com.hotelpms.frontdesk.exception.NotFoundException;
import com.hotelpms.frontdesk.exception.BadRequestException;
import com.hotelpms.frontdesk.workflow.domain.*;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.TransitionRequest;
import com.hotelpms.frontdesk.workflow.repository.*;
import com.hotelpms.frontdesk.workflow.service.WorkflowService;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkflowServiceTest {
    private final WorkflowDefinitionRepository definitions = mock(WorkflowDefinitionRepository.class);
    private final WorkflowStateRepository states = mock(WorkflowStateRepository.class);
    private final WorkflowTransitionRepository transitions = mock(WorkflowTransitionRepository.class);
    private final WorkflowTransitionAuditRepository audits = mock(WorkflowTransitionAuditRepository.class);
    private final WorkflowInstanceRepository instances = mock(WorkflowInstanceRepository.class);
    private final WorkflowService service = new WorkflowService(definitions, states, transitions, audits, instances);
    private final UUID tenant = UUID.randomUUID(), workflowId = UUID.randomUUID(), aggregate = UUID.randomUUID();
    private final WorkflowDefinition workflow = new WorkflowDefinition(tenant, "repair", "Repair");
    private final TransitionRequest request = new TransitionRequest(aggregate, "received", "diagnose");

    private void allowTransition(boolean initial, boolean terminal) {
        when(definitions.findLockedByIdAndHotelId(workflowId, tenant)).thenReturn(Optional.of(workflow));
        WorkflowState from = new WorkflowState(workflow, "received", "Received", SemanticPhase.INTAKE, initial, terminal);
        WorkflowState to = new WorkflowState(workflow, "diagnosing", "Diagnosing", SemanticPhase.DIAGNOSIS, false, false);
        when(transitions.findByTenantAndWorkflowIdAndFromStateKeyAndKey(tenant, workflowId, "received", "diagnose"))
                .thenReturn(Optional.of(new WorkflowTransition(workflow, from, to, "diagnose", "workflow.transition")));
    }

    @Test void firstTransitionPersistsStateAndAudit() {
        allowTransition(true, false);
        service.transition(tenant, workflowId, request, "technician");
        verify(instances).save(argThat(i -> i.getStateKey().equals("diagnosing")));
        verify(audits).save(any(WorkflowTransitionAudit.class));
    }

    @Test void roleLabelsCannotBeUsedAsTransitionPermissions() {
        assertThrows(BadRequestException.class, () -> service.addTransition(tenant, workflowId,
                new com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.AddTransitionRequest(
                        "received", "diagnosing", "diagnose", "ROLE_OWNER")));
        verifyNoInteractions(definitions, states, transitions);
    }

    @Test void bareRoleLabelsCannotBeUsedAsTransitionPermissions() {
        assertThrows(BadRequestException.class, () -> service.addTransition(tenant, workflowId,
                new com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.AddTransitionRequest(
                        "received", "diagnosing", "diagnose", "ADMIN")));
        verifyNoInteractions(definitions, states, transitions);
    }

    @Test void staleOrRepeatedTransitionCannotWriteAnotherAudit() {
        allowTransition(true, false);
        when(instances.findLockedByHotelIdAndWorkflowIdAndAggregateId(tenant, workflowId, aggregate))
                .thenReturn(Optional.of(new WorkflowInstance(tenant, workflowId, aggregate, "diagnosing")));
        assertThrows(ConflictException.class, () -> service.transition(tenant, workflowId, request, "technician"));
        verify(instances, never()).save(any());
        verifyNoInteractions(audits);
    }

    @Test void foreignTenantCannotReadOrMutateInstance() {
        assertThrows(NotFoundException.class, () -> service.transition(UUID.randomUUID(), workflowId, request, "attacker"));
        verifyNoInteractions(instances, transitions, audits);
    }

    @Test void foreignTenantCannotReadWorkflowDefinition() {
        UUID foreignTenant = UUID.randomUUID();
        when(definitions.findByIdAndHotelIdAndActiveTrue(workflowId, foreignTenant)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.definition(foreignTenant, workflowId));
        verify(definitions).findByIdAndHotelIdAndActiveTrue(workflowId, foreignTenant);
        verifyNoInteractions(states, transitions, instances, audits);
    }

    @Test void missingInstanceCannotStartHalfwayThroughWorkflow() {
        allowTransition(false, false);
        assertThrows(ConflictException.class, () -> service.transition(tenant, workflowId, request, "technician"));
        verify(instances, never()).save(any());
        verifyNoInteractions(audits);
    }

    @Test void terminalStateCannotTransitionEvenIfConfigurationAllowsIt() {
        allowTransition(true, true);
        assertThrows(ConflictException.class, () -> service.transition(tenant, workflowId, request, "technician"));
        verify(instances, never()).save(any());
        verifyNoInteractions(audits);
    }
}
