package com.hotelpms.frontdesk.workflow.service;

import com.hotelpms.frontdesk.exception.BadRequestException;
import com.hotelpms.frontdesk.exception.ConflictException;
import com.hotelpms.frontdesk.exception.NotFoundException;
import com.hotelpms.frontdesk.workflow.domain.WorkflowDefinition;
import com.hotelpms.frontdesk.workflow.domain.WorkflowInstance;
import com.hotelpms.frontdesk.workflow.domain.WorkflowState;
import com.hotelpms.frontdesk.workflow.domain.WorkflowTransition;
import com.hotelpms.frontdesk.workflow.domain.WorkflowTransitionAudit;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.AddStateRequest;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.AddTransitionRequest;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.CreateWorkflowRequest;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.TransitionRequest;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.UpdateStateLabelRequest;
import com.hotelpms.frontdesk.workflow.repository.WorkflowDefinitionRepository;
import com.hotelpms.frontdesk.workflow.repository.WorkflowInstanceRepository;
import com.hotelpms.frontdesk.workflow.repository.WorkflowStateRepository;
import com.hotelpms.frontdesk.workflow.repository.WorkflowTransitionAuditRepository;
import com.hotelpms.frontdesk.workflow.repository.WorkflowTransitionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Tenant-scoped workflow definitions, transitions, execution state, and audit history. */
@Service
@RequiredArgsConstructor
public class WorkflowService {
    private static final String WORKFLOW_STATE_NOT_FOUND = "Workflow state not found";

    private final WorkflowDefinitionRepository definitions;
    private final WorkflowStateRepository states;
    private final WorkflowTransitionRepository transitions;
    private final WorkflowTransitionAuditRepository audits;
    private final WorkflowInstanceRepository instances;

    /**
     * Creates a workflow definition for the tenant.
     *
     * @param hotel tenant identifier
     * @param r workflow definition details
     * @return persisted workflow definition
     * @implSpec Overrides must preserve tenant ownership and request validation.
     */
    @Transactional
    public WorkflowDefinition create(final UUID hotel, final CreateWorkflowRequest r) {
        return definitions.save(new WorkflowDefinition(hotel, r.verticalKey(), r.name()));
    }

    /**
     * Lists active workflow definitions owned by a tenant.
     *
     * @param hotel tenant identifier
     * @return active tenant-owned workflows
     * @implSpec Overrides must preserve tenant scoping.
     */
    @Transactional
    public List<WorkflowDefinition> list(final UUID hotel) {
        return definitions.findAllByHotelIdAndActiveTrue(hotel);
    }

    /**
     * Adds a state to a tenant-owned workflow.
     *
     * @param hotel tenant identifier
     * @param workflowId workflow identifier
     * @param r validated state details
     * @return persisted workflow state
     * @implSpec Overrides must preserve tenant ownership and state semantics.
     */
    @Transactional
    public WorkflowState addState(
            final UUID hotel, final UUID workflowId, final AddStateRequest r) {
        final WorkflowDefinition w = definition(hotel, workflowId);
        return states.save(
                new WorkflowState(
                        w,
                        r.key(),
                        r.label(),
                        r.semanticPhase(),
                        r.initialState(),
                        r.terminalState()));
    }

    /**
     * Adds a permission-protected transition between active states.
     *
     * @param hotel tenant identifier
     * @param workflowId workflow identifier
     * @param r validated transition details
     * @return persisted transition
     * @implSpec Overrides must reject role labels and retain explicit permission checks.
     */
    @Transactional
    public WorkflowTransition addTransition(
            final UUID hotel, final UUID workflowId, final AddTransitionRequest r) {
        if (r.requiredPermission() == null
                || r.requiredPermission().isBlank()
                || isRoleLabel(r.requiredPermission())) {
            throw new BadRequestException(
                    "Workflow transitions require an explicit permission authority");
        }
        final WorkflowDefinition w = definition(hotel, workflowId);
        final WorkflowState from =
                states.findByWorkflowIdAndKey(workflowId, r.fromStateKey())
                        .orElseThrow(() -> new NotFoundException(WORKFLOW_STATE_NOT_FOUND));
        final WorkflowState to =
                states.findByWorkflowIdAndKey(workflowId, r.toStateKey())
                        .orElseThrow(() -> new NotFoundException(WORKFLOW_STATE_NOT_FOUND));
        if (!from.isActive() || !to.isActive()) {
            throw new ConflictException("Inactive workflow states cannot be used in transitions");
        }
        return transitions.save(
                new WorkflowTransition(w, from, to, r.transitionKey(), r.requiredPermission()));
    }

    /**
     * Applies a transition atomically and writes its tenant-owned audit record.
     *
     * @param hotel tenant identifier
     * @param workflowId workflow identifier
     * @param r requested aggregate transition
     * @param actor authenticated actor label
     * @return applied transition
     * @implSpec Overrides must preserve row locking, transition validation, and audit persistence.
     */
    @Transactional
    public WorkflowTransition transition(
            final UUID hotel,
            final UUID workflowId,
            final TransitionRequest r,
            final String actor) {
        definitions
                .findLockedByIdAndHotelId(workflowId, hotel)
                .orElseThrow(() -> new NotFoundException("Workflow not found"));
        final WorkflowTransition t =
                transitions
                        .findByTenantAndWorkflowIdAndFromStateKeyAndKey(
                                hotel, workflowId, r.fromStateKey(), r.transitionKey())
                        .orElseThrow(
                                () ->
                                        new ConflictException(
                                                "Transition is not allowed from current state"));
        if (!t.getFromState().isActive() || !t.getToState().isActive()) {
            throw new ConflictException("Transition uses an inactive workflow state");
        }
        final WorkflowInstance instance =
                instances
                        .findLockedByHotelIdAndWorkflowIdAndAggregateId(
                                hotel, workflowId, r.aggregateId())
                        .orElseGet(
                                () -> {
                                    if (!t.getFromState().isInitialState()) {
                                        throw new ConflictException(
                                                "A new workflow instance must start at its initial"
                                                    + " state");
                                    }
                                    return new WorkflowInstance(
                                            hotel, workflowId, r.aggregateId(), r.fromStateKey());
                                });
        if (!instance.getStateKey().equals(r.fromStateKey())
                || t.getFromState().isTerminalState()) {
            throw new ConflictException("Workflow state changed or is terminal");
        }
        instance.moveTo(t.getToState().getKey());
        instances.save(instance);
        audits.save(
                new WorkflowTransitionAudit(
                        hotel,
                        workflowId,
                        r.aggregateId(),
                        t.getFromState().getKey(),
                        t.getToState().getKey(),
                        t.getToState().getSemanticPhase(),
                        actor));
        return t;
    }

    /**
     * Finds an active workflow for a tenant and vertical.
     *
     * @param hotel tenant identifier
     * @param verticalKey configured vertical key
     * @return matching workflow, or {@code null} when none exists
     * @implSpec Overrides must scope the lookup to the tenant.
     */
    @Transactional(readOnly = true)
    public WorkflowDefinition activeForVertical(final UUID hotel, final String verticalKey) {
        return definitions
                .findByVerticalKeyAndHotelIdAndActiveTrue(verticalKey, hotel)
                .orElse(null);
    }

    /**
     * Reads the current state of an aggregate's tenant-owned workflow instance.
     *
     * @param hotel tenant identifier
     * @param workflowId workflow identifier
     * @param aggregateId aggregate identifier
     * @return current state, or {@code null} when no instance exists
     * @implSpec Overrides must preserve tenant isolation for workflow instances.
     */
    @Transactional(readOnly = true)
    public WorkflowState stateForAggregate(
            final UUID hotel, final UUID workflowId, final UUID aggregateId) {
        return instances
                .findByHotelIdAndWorkflowIdAndAggregateId(hotel, workflowId, aggregateId)
                .flatMap(
                        instance ->
                                states.findByWorkflowIdAndKey(workflowId, instance.getStateKey()))
                .orElse(null);
    }

    /**
     * Reads an active workflow definition owned by a tenant.
     *
     * @param hotel tenant identifier
     * @param id workflow identifier
     * @return active tenant-owned workflow
     * @implSpec Overrides must not return definitions across tenants.
     */
    @Transactional
    public WorkflowDefinition definition(final UUID hotel, final UUID id) {
        return definitions
                .findByIdAndHotelIdAndActiveTrue(id, hotel)
                .orElseThrow(() -> new NotFoundException("Workflow not found"));
    }

    private boolean isRoleLabel(final String permission) {
        final String normalized = permission.trim().toUpperCase(Locale.ROOT);
        return normalized.startsWith("ROLE_")
                || "ADMIN".equals(normalized)
                || "OWNER".equals(normalized);
    }

    /**
     * Lists states for a tenant-owned workflow.
     *
     * @param hotel tenant identifier
     * @param id workflow identifier
     * @return workflow states
     * @implSpec Overrides must verify the workflow belongs to the tenant.
     */
    public List<WorkflowState> states(final UUID hotel, final UUID id) {
        definition(hotel, id);
        return states.findAllByWorkflowId(id);
    }

    /**
     * Updates a state label after verifying tenant ownership.
     *
     * @param hotel tenant identifier
     * @param workflowId workflow identifier
     * @param key state key
     * @param request validated replacement label
     * @return persisted workflow state
     * @implSpec Overrides must preserve tenant ownership and state identity.
     */
    @Transactional
    public WorkflowState updateStateLabel(
            final UUID hotel,
            final UUID workflowId,
            final String key,
            final UpdateStateLabelRequest request) {
        definition(hotel, workflowId);
        final WorkflowState state =
                states.findByWorkflowIdAndKey(workflowId, key)
                        .orElseThrow(() -> new NotFoundException(WORKFLOW_STATE_NOT_FOUND));
        state.setLabel(request.label().trim());
        return states.save(state);
    }

    /**
     * Enables or disables a state without invalidating initial or active-order states.
     *
     * @param hotel tenant identifier
     * @param workflowId workflow identifier
     * @param key state key
     * @param active requested availability
     * @return persisted workflow state
     * @implSpec Overrides must retain tenant ownership and active-order safeguards.
     */
    @Transactional
    public WorkflowState updateStateActive(
            final UUID hotel, final UUID workflowId, final String key, final boolean active) {
        definition(hotel, workflowId);
        final WorkflowState state =
                states.findByWorkflowIdAndKey(workflowId, key)
                        .orElseThrow(() -> new NotFoundException(WORKFLOW_STATE_NOT_FOUND));
        if (!active && state.isInitialState()) {
            throw new ConflictException("The initial workflow state cannot be deactivated");
        }
        if (!active
                && state.isActive()
                && instances.existsByHotelIdAndWorkflowIdAndStateKey(hotel, workflowId, key)) {
            throw new ConflictException("A state used by active orders cannot be deactivated");
        }
        state.setActive(active);
        return states.save(state);
    }

    /**
     * Lists transitions for a tenant-owned workflow.
     *
     * @param hotel tenant identifier
     * @param id workflow identifier
     * @return configured workflow transitions
     * @implSpec Overrides must verify the workflow belongs to the tenant.
     */
    public List<WorkflowTransition> transitions(final UUID hotel, final UUID id) {
        definition(hotel, id);
        return transitions.findAllByWorkflowId(id);
    }
}
