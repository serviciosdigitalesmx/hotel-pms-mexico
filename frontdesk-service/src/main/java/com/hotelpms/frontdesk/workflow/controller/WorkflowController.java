package com.hotelpms.frontdesk.workflow.controller;

import com.hotelpms.frontdesk.exception.ConflictException;
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
import org.springframework.security.access.AccessDeniedException;
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

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Authenticated API for tenant-owned workflow definitions and transitions.
 */
@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowController {
    private static final String ADMIN_OWNER_ROLES = "hasAnyRole('ADMIN', 'OWNER')";
    private final WorkflowService service;

    /**
     * Lists workflows configured for the authenticated tenant.
     *
     * @return tenant-owned workflow definitions
     * @implSpec Overrides must preserve tenant scoping and role authorization.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    public List<WorkflowResponse> list() {
        return service.list(hotel()).stream().map(this::view).toList();
    }

    /**
     * Creates a workflow definition.
     *
     * @param r workflow request
     * @return created workflow
     * @implSpec Overrides must preserve authenticated tenant ownership and role authorization.
     */
    @PostMapping
    @PreAuthorize(ADMIN_OWNER_ROLES)
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
     * @implSpec Overrides must preserve tenant ownership and role authorization.
     */
    @PostMapping("/{id}/states")
    @PreAuthorize(ADMIN_OWNER_ROLES)
    public StateResponse state(
            @PathVariable final UUID id, @Valid @RequestBody final AddStateRequest r) {
        return stateView(service.addState(hotel(), id, r));
    }

    /**
     * Updates the display label of a tenant-owned workflow state.
     *
     * @param id workflow identifier
     * @param key state identifier
     * @param request validated label update
     * @return updated workflow state
     * @implSpec Overrides must preserve tenant ownership and role authorization.
     */
    @PatchMapping("/{id}/states/{key}")
    @PreAuthorize(ADMIN_OWNER_ROLES)
    public StateResponse updateStateLabel(
            @PathVariable final UUID id,
            @PathVariable final String key,
            @Valid @RequestBody final UpdateStateLabelRequest request) {
        return stateView(service.updateStateLabel(hotel(), id, key, request));
    }

    /**
     * Enables or disables a tenant-owned workflow state.
     *
     * @param id workflow identifier
     * @param key state identifier
     * @param active requested availability
     * @return updated workflow state
     * @implSpec Overrides must preserve tenant ownership and role authorization.
     */
    @PatchMapping("/{id}/states/{key}/active")
    @PreAuthorize(ADMIN_OWNER_ROLES)
    public StateResponse updateStateActive(
            @PathVariable final UUID id,
            @PathVariable final String key,
            @RequestParam final boolean active) {
        return stateView(service.updateStateActive(hotel(), id, key, active));
    }

    /**
     * Adds a transition definition.
     *
     * @param id workflow identifier
     * @param r transition definition request
     * @return created transition
     * @implSpec Overrides must preserve tenant ownership and role authorization.
     */
    @PostMapping("/{id}/transitions")
    @PreAuthorize(ADMIN_OWNER_ROLES)
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
     * @implSpec Overrides must preserve backend capability checks and tenant-scoped logic.
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
                                        r.transitionKey().equals(t.getKey())
                                                && r.fromStateKey()
                                                        .equals(t.getFromState().getKey()))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new ConflictException(
                                                "Transition is not allowed from current state"));
        if (!authentication.getAuthorities().stream()
                .anyMatch(
                        a ->
                                "ROLE_ADMIN".equals(a.getAuthority())
                                        || "ROLE_OWNER".equals(a.getAuthority())
                                        || Objects.equals(
                                                candidate.getRequiredPermission(),
                                                a.getAuthority()))) {
            throw new AccessDeniedException("Missing workflow permission");
        }
        return transitionView(
                service.transition(hotel(), id, r, String.valueOf(authentication.getPrincipal())));
    }

    /**
     * Reads a workflow state for one aggregate in the authenticated tenant.
     *
     * @param id workflow identifier
     * @param aggregateId aggregate identifier
     * @return current state, or {@code null} when no instance has started
     * @implSpec Overrides must not expose workflow instances across tenants.
     */
    @GetMapping("/{id}/instances/{aggregateId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'TECHNICIAN')")
    public StateResponse instanceState(
            @PathVariable final UUID id, @PathVariable final UUID aggregateId) {
        final var state = service.stateForAggregate(hotel(), id, aggregateId);
        if (state == null) {
            return null;
        }
        return stateView(state);
    }

    /**
     * Gets a workflow definition and its states and transitions.
     *
     * @param id workflow identifier
     * @return workflow response
     * @implSpec Overrides must preserve tenant ownership and role authorization.
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
