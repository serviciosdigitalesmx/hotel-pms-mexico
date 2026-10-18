package com.hotelpms.frontdesk.workflow;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.hotelpms.frontdesk.exception.BadRequestException;
import com.hotelpms.frontdesk.exception.ConflictException;
import com.hotelpms.frontdesk.exception.NotFoundException;
import com.hotelpms.frontdesk.workflow.domain.SemanticPhase;
import com.hotelpms.frontdesk.workflow.domain.WorkflowDefinition;
import com.hotelpms.frontdesk.workflow.domain.WorkflowInstance;
import com.hotelpms.frontdesk.workflow.domain.WorkflowState;
import com.hotelpms.frontdesk.workflow.domain.WorkflowTransition;
import com.hotelpms.frontdesk.workflow.domain.WorkflowTransitionAudit;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.AddTransitionRequest;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.TransitionRequest;
import com.hotelpms.frontdesk.workflow.repository.WorkflowDefinitionRepository;
import com.hotelpms.frontdesk.workflow.repository.WorkflowInstanceRepository;
import com.hotelpms.frontdesk.workflow.repository.WorkflowStateRepository;
import com.hotelpms.frontdesk.workflow.repository.WorkflowTransitionAuditRepository;
import com.hotelpms.frontdesk.workflow.repository.WorkflowTransitionRepository;
import com.hotelpms.frontdesk.workflow.service.WorkflowService;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

class WorkflowServiceTest {
    private static final String RECEIVED_STATE = "received";
    private static final String DIAGNOSING_STATE = "diagnosing";
    private static final String DIAGNOSE_TRANSITION = "diagnose";
    private static final String TRANSITION_PERMISSION = "workflow.transition";
    private static final String TECHNICIAN_ACTOR = "technician";
    private final WorkflowDefinitionRepository definitions =
            mock(WorkflowDefinitionRepository.class);
    private final WorkflowStateRepository states = mock(WorkflowStateRepository.class);
    private final WorkflowTransitionRepository transitions =
            mock(WorkflowTransitionRepository.class);
    private final WorkflowTransitionAuditRepository audits =
            mock(WorkflowTransitionAuditRepository.class);
    private final WorkflowInstanceRepository instances = mock(WorkflowInstanceRepository.class);
    private final WorkflowService service =
            new WorkflowService(definitions, states, transitions, audits, instances);
    private final UUID tenant = UUID.randomUUID();
    private final UUID workflowId = UUID.randomUUID();
    private final UUID aggregate = UUID.randomUUID();
    private final WorkflowDefinition workflow = new WorkflowDefinition(tenant, "repair", "Repair");
    private final TransitionRequest request =
            new TransitionRequest(aggregate, "received", "diagnose");

    private void allowTransition(final boolean initial, final boolean terminal) {
        when(definitions.findLockedByIdAndHotelId(workflowId, tenant))
                .thenReturn(Optional.of(workflow));
        final WorkflowState from =
                new WorkflowState(
                        workflow, RECEIVED_STATE, "Received", SemanticPhase.INTAKE, initial, terminal);
        final WorkflowState to =
                new WorkflowState(
                        workflow,
                        DIAGNOSING_STATE,
                        "Diagnosing",
                        SemanticPhase.DIAGNOSIS,
                        false,
                        false);
        when(transitions.findByTenantAndWorkflowIdAndFromStateKeyAndKey(
                        tenant, workflowId, RECEIVED_STATE, DIAGNOSE_TRANSITION))
                .thenReturn(
                        Optional.of(
                                new WorkflowTransition(
                                        workflow, from, to, DIAGNOSE_TRANSITION, TRANSITION_PERMISSION)));
    }

    @Test
    void firstTransitionPersistsStateAndAudit() {
        allowTransition(true, false);
        service.transition(tenant, workflowId, request, TECHNICIAN_ACTOR);
        verify(instances).save(argThat(i -> i.getStateKey().equals(DIAGNOSING_STATE)));
        verify(audits).save(any(WorkflowTransitionAudit.class));
    }

    @Test
    void roleLabelsCannotBeUsedAsTransitionPermissions() {
        assertThrows(
                BadRequestException.class,
                () ->
                        service.addTransition(
                                tenant,
                                workflowId,
                                new AddTransitionRequest(
                                        RECEIVED_STATE,
                                        DIAGNOSING_STATE,
                                        DIAGNOSE_TRANSITION,
                                        "ROLE_OWNER")));
        verifyNoInteractions(definitions, states, transitions);
    }

    @Test
    void bareRoleLabelsCannotBeUsedAsTransitionPermissions() {
        assertThrows(
                BadRequestException.class,
                () ->
                        service.addTransition(
                                tenant,
                                workflowId,
                                new AddTransitionRequest(
                                        RECEIVED_STATE,
                                        DIAGNOSING_STATE,
                                        DIAGNOSE_TRANSITION,
                                        "ADMIN")));
        verifyNoInteractions(definitions, states, transitions);
    }

    @Test
    void lowercaseRoleLabelsCannotBeUsedAsTransitionPermissions() {
        assertThrows(
                BadRequestException.class,
                () ->
                        service.addTransition(
                                tenant,
                                workflowId,
                                new AddTransitionRequest(
                                        RECEIVED_STATE,
                                        DIAGNOSING_STATE,
                                        DIAGNOSE_TRANSITION,
                                        "role_owner")));
        verifyNoInteractions(definitions, states, transitions);
    }

    @Test
    void staleOrRepeatedTransitionCannotWriteAnotherAudit() {
        allowTransition(true, false);
        when(instances.findLockedByHotelIdAndWorkflowIdAndAggregateId(
                        tenant, workflowId, aggregate))
                .thenReturn(
                        Optional.of(
                                new WorkflowInstance(
                                        tenant, workflowId, aggregate, DIAGNOSING_STATE)));
        assertThrows(
                ConflictException.class,
                () -> service.transition(tenant, workflowId, request, TECHNICIAN_ACTOR));
        verify(instances, never()).save(any());
        verifyNoInteractions(audits);
    }

    @Test
    void foreignTenantCannotReadOrMutateInstance() {
        assertThrows(
                NotFoundException.class,
                () -> service.transition(UUID.randomUUID(), workflowId, request, "attacker"));
        verifyNoInteractions(instances, transitions, audits);
    }

    @Test
    void foreignTenantCannotReadWorkflowDefinition() {
        final UUID foreignTenant = UUID.randomUUID();
        when(definitions.findByIdAndHotelIdAndActiveTrue(workflowId, foreignTenant))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.definition(foreignTenant, workflowId));
        verify(definitions).findByIdAndHotelIdAndActiveTrue(workflowId, foreignTenant);
        verifyNoInteractions(states, transitions, instances, audits);
    }

    @Test
    void missingInstanceCannotStartHalfwayThroughWorkflow() {
        allowTransition(false, false);
        assertThrows(
                ConflictException.class,
                () -> service.transition(tenant, workflowId, request, TECHNICIAN_ACTOR));
        verify(instances, never()).save(any());
        verifyNoInteractions(audits);
    }

    @Test
    void terminalStateCannotTransitionEvenIfConfigurationAllowsIt() {
        allowTransition(true, true);
        assertThrows(
                ConflictException.class,
                () -> service.transition(tenant, workflowId, request, TECHNICIAN_ACTOR));
        verify(instances, never()).save(any());
        verifyNoInteractions(audits);
    }
}
