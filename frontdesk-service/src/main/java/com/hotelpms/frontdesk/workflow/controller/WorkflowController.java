package com.hotelpms.frontdesk.workflow.controller;

import com.hotelpms.frontdesk.workflow.domain.WorkflowDefinition;
import com.hotelpms.frontdesk.workflow.domain.WorkflowState;
import com.hotelpms.frontdesk.workflow.domain.WorkflowTransition;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.AddStateRequest;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.AddTransitionRequest;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.CreateWorkflowRequest;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.StateResponse;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.TransitionRequest;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.TransitionResponse;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.UpdateStateLabelRequest;
import com.hotelpms.frontdesk.workflow.dto.WorkflowDtos.WorkflowResponse;
import com.hotelpms.frontdesk.workflow.service.WorkflowService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Type domain type. */
@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowController {
    private final WorkflowService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    public java.util.List<WorkflowResponse> list() {
        return service.list(hotel()).stream().map(this::view).toList();
    }

    /**
     * Creates a workflow definition.
     *
     * @param r workflow request
     * @return created workflow
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<WorkflowResponse> create(
            @Valid @RequestBody final CreateWorkflowRequest r) {
        final WorkflowDefinition w = service.create(hotel(), r);
        return ResponseEntity.status(HttpStatus.CREATED).body(view(w));
    }

    /**
     * Adds a state to a workflow.
     *
     * @param id workflow identifier
     * @param r state request
     * @return created state
     */
    @PostMapping("/{id}/states")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public StateResponse state(
            @PathVariable final UUID id, @Valid @RequestBody final AddStateRequest r) {
        return stateView(service.addState(hotel(), id, r));
    }

    @PatchMapping("/{id}/states/{key}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public StateResponse updateStateLabel(
            @PathVariable final UUID id,
            @PathVariable final String key,
            @Valid @RequestBody final UpdateStateLabelRequest request) {
        return stateView(service.updateStateLabel(hotel(), id, key, request));
    }

    @PatchMapping("/{id}/states/{key}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public StateResponse setStateActive(
            @PathVariable final UUID id,
            @PathVariable final String key,
            @RequestParam final boolean active) {
        return stateView(service.setStateActive(hotel(), id, key, active));
    }

    /**
     * Adds a transition definition.
     *
     * @param id workflow identifier
     * @param r transition definition request
     * @return created transition
     */
    @PostMapping("/{id}/transitions")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public TransitionResponse transitionDef(
            @PathVariable final UUID id, @Valid @RequestBody final AddTransitionRequest r) {
        return transitionView(service.addTransition(hotel(), id, r));
    }

    /**
     * Executes a workflow transition.
     *
     * @param id workflow identifier
     * @param r transition request
     * @return transition result
     */
    @PostMapping("/{id}/transition")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'TECHNICIAN')")
    public TransitionResponse transition(
            @PathVariable final UUID id, @Valid @RequestBody final TransitionRequest r) {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var candidate =
                service.transitions(hotel(), id).stream()
                        .filter(
                                t ->
                                        t.getKey().equals(r.transitionKey())
                                                && t.getFromState()
                                                        .getKey()
                                                        .equals(r.fromStateKey()))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new com.hotelpms.frontdesk.exception.ConflictException(
                                                "Transition is not allowed from current state"));
        if (!authentication.getAuthorities().stream()
                .anyMatch(
                        a ->
                                a.getAuthority().equals("ROLE_ADMIN")
                                        || a.getAuthority().equals("ROLE_OWNER")
                                        || a.getAuthority()
                                                .equals(candidate.getRequiredPermission()))) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Missing workflow permission");
        }
        return transitionView(
                service.transition(hotel(), id, r, String.valueOf(authentication.getPrincipal())));
    }

    @GetMapping("/{id}/instances/{aggregateId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'TECHNICIAN')")
    public StateResponse instanceState(
            @PathVariable final UUID id, @PathVariable final UUID aggregateId) {
        final var state = service.stateForAggregate(hotel(), id, aggregateId);
        if (state == null) return null;
        return stateView(state);
    }

    /**
     * Gets a workflow definition and its states and transitions.
     *
     * @param id workflow identifier
     * @return workflow response
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'RECEPTIONIST')")
    public WorkflowResponse get(@PathVariable final UUID id) {
        return view(service.definition(hotel(), id));
    }

    private WorkflowResponse view(final WorkflowDefinition w) {
        return new WorkflowResponse(
                w.getId(),
                w.getVerticalKey(),
                w.getName(),
                w.getVersion(),
                service.states(hotel(), w.getId()).stream().map(this::stateView).toList(),
                service.transitions(hotel(), w.getId()).stream()
                        .map(this::transitionView)
                        .toList());
    }

    private StateResponse stateView(final WorkflowState s) {
        return new StateResponse(
                s.getKey(),
                s.getLabel(),
                s.getSemanticPhase(),
                s.isInitialState(),
                s.isTerminalState(),
                s.isActive());
    }

    private TransitionResponse transitionView(final WorkflowTransition t) {
        return new TransitionResponse(
                t.getKey(),
                t.getFromState().getKey(),
                t.getToState().getKey(),
                t.getRequiredPermission());
    }

    private UUID hotel() {
        return UUID.fromString(
                String.valueOf(
                        SecurityContextHolder.getContext().getAuthentication().getDetails()));
    }
}
