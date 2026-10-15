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
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Type domain type. */
@Service
@RequiredArgsConstructor
public class WorkflowService {
  private final WorkflowDefinitionRepository definitions;
  private final WorkflowStateRepository states;
  private final WorkflowTransitionRepository transitions;
  private final WorkflowTransitionAuditRepository audits;
  private final WorkflowInstanceRepository instances;
  /**
   * Executes the operation.
   *
   * @param hotel parameter.
   *
   * @param r parameter.
   *
   * @return operation result.
   */

  @Transactional
  public WorkflowDefinition create(final UUID hotel, final CreateWorkflowRequest r) {
    return definitions.save(new WorkflowDefinition(hotel, r.verticalKey(), r.name()));
  }

  @Transactional
  public List<WorkflowDefinition> list(final UUID hotel) {
    return definitions.findAllByHotelIdAndActiveTrue(hotel);
  }
  /**
   * Executes the operation.
   *
   * @param hotel parameter.
   *
   * @param workflowId parameter.
   *
   * @param r parameter.
   *
   * @return operation result.
   */

  @Transactional
  public WorkflowState addState(final UUID hotel, final UUID workflowId, final AddStateRequest r) {
    final WorkflowDefinition w = definition(hotel, workflowId);
    return states.save(
        new WorkflowState(
            w, r.key(), r.label(), r.semanticPhase(), r.initialState(), r.terminalState()));
  }
  /**
   * Executes the operation.
   *
   * @param hotel parameter.
   *
   * @param workflowId parameter.
   *
   * @param r parameter.
   *
   * @return operation result.
   */

  @Transactional
  public WorkflowTransition addTransition(final UUID hotel, final UUID workflowId, final AddTransitionRequest r) {
    if (r.requiredPermission() == null
        || r.requiredPermission().isBlank()
        || isRoleLabel(r.requiredPermission())) {
      throw new BadRequestException(
          "Workflow transitions require an explicit permission authority");
    }
    final WorkflowDefinition w = definition(hotel, workflowId);
    final WorkflowState from =
        states
            .findByWorkflowIdAndKey(workflowId, r.fromStateKey())
            .orElseThrow(() -> new NotFoundException("Workflow state not found"));
    final WorkflowState to =
        states
            .findByWorkflowIdAndKey(workflowId, r.toStateKey())
            .orElseThrow(() -> new NotFoundException("Workflow state not found"));
    if (!from.isActive() || !to.isActive()) {
      throw new ConflictException("Inactive workflow states cannot be used in transitions");
    }
    return transitions.save(
        new WorkflowTransition(w, from, to, r.transitionKey(), r.requiredPermission()));
  }
  /**
   * Executes the operation.
   *
   * @param hotel parameter.
   *
   * @param workflowId parameter.
   *
   * @param r parameter.
   *
   * @param actor parameter.
   *
   * @return operation result.
   */

  @Transactional
  public WorkflowTransition transition(
      final UUID hotel, final UUID workflowId, final TransitionRequest r, final String actor) {
    definitions
        .findLockedByIdAndHotelId(workflowId, hotel)
        .orElseThrow(() -> new NotFoundException("Workflow not found"));
    final WorkflowTransition t =
        transitions
            .findByTenantAndWorkflowIdAndFromStateKeyAndKey(
                hotel, workflowId, r.fromStateKey(), r.transitionKey())
            .orElseThrow(
                () -> new ConflictException("Transition is not allowed from current state"));
    if (!t.getFromState().isActive() || !t.getToState().isActive()) {
      throw new ConflictException("Transition uses an inactive workflow state");
    }
    final WorkflowInstance instance =
        instances
            .findLockedByHotelIdAndWorkflowIdAndAggregateId(hotel, workflowId, r.aggregateId())
            .orElseGet(
                () -> {
                  if (!t.getFromState().isInitialState()) {
                    throw new ConflictException(
                        "A new workflow instance must start at its initial state");
                  }
                  return new WorkflowInstance(hotel, workflowId, r.aggregateId(), r.fromStateKey());
                });
    if (!instance.getStateKey().equals(r.fromStateKey()) || t.getFromState().isTerminalState()) {
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

  @Transactional(readOnly = true)
  public WorkflowDefinition activeForVertical(final UUID hotel, final String verticalKey) {
    return definitions.findByVerticalKeyAndHotelIdAndActiveTrue(verticalKey, hotel).orElse(null);
  }

  @Transactional(readOnly = true)
  public WorkflowState stateForAggregate(final UUID hotel, final UUID workflowId, final UUID aggregateId) {
    return instances.findByHotelIdAndWorkflowIdAndAggregateId(hotel, workflowId, aggregateId)
        .flatMap(instance -> states.findByWorkflowIdAndKey(workflowId, instance.getStateKey()))
        .orElse(null);
  }
  /**
   * Executes the operation.
   *
   * @param hotel parameter.
   *
   * @param id parameter.
   *
   * @return operation result.
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
   * Executes the operation.
   *
   * @param hotel parameter.
   *
   * @param id parameter.
   *
   * @return operation result.
   */

  public List<WorkflowState> states(final UUID hotel, final UUID id) {
    definition(hotel, id);
    return states.findAllByWorkflowId(id);
  }

  @Transactional
  public WorkflowState updateStateLabel(final UUID hotel, final UUID workflowId, final String key,
      final UpdateStateLabelRequest request) {
    definition(hotel, workflowId);
    final WorkflowState state = states.findByWorkflowIdAndKey(workflowId, key)
        .orElseThrow(() -> new NotFoundException("Workflow state not found"));
    state.setLabel(request.label().trim());
    return states.save(state);
  }

  @Transactional
  public WorkflowState setStateActive(final UUID hotel, final UUID workflowId, final String key,
      final boolean active) {
    definition(hotel, workflowId);
    final WorkflowState state = states.findByWorkflowIdAndKey(workflowId, key)
        .orElseThrow(() -> new NotFoundException("Workflow state not found"));
    if (!active && state.isInitialState()) {
      throw new ConflictException("The initial workflow state cannot be deactivated");
    }
    if (!active && state.isActive()
        && instances.existsByHotelIdAndWorkflowIdAndStateKey(hotel, workflowId, key)) {
      throw new ConflictException("A state used by active orders cannot be deactivated");
    }
    state.setActive(active);
    return states.save(state);
  }
  /**
   * Executes the operation.
   *
   * @param hotel parameter.
   *
   * @param id parameter.
   *
   * @return operation result.
   */

  public List<WorkflowTransition> transitions(final UUID hotel, final UUID id) {
    definition(hotel, id);
    return transitions.findAllByWorkflowId(id);
  }
}
