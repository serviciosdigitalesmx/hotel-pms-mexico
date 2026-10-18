package com.hotelpms.frontdesk.workflow.dto;

import com.hotelpms.frontdesk.util.ImmutableCopies;
import com.hotelpms.frontdesk.workflow.domain.SemanticPhase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Validated request and response records for tenant-configured repair workflows.
 */
public final class WorkflowDtos {
    private WorkflowDtos() { }

    /**
     * Returns an immutable defensive copy for response collection components.
     *
     * @param <T> list element type
     * @param source source list
     * @return immutable copy, preserving null input
     */
    private static <T> List<T> immutableList(final List<T> source) {
        return ImmutableCopies.list(source);
    }

    /**
     * Creates a workflow for a supported business vertical.
     *
     * @param verticalKey business vertical key
     * @param name workflow display name
     */
    public record CreateWorkflowRequest(@NotBlank String verticalKey, @NotBlank String name) { }

    /**
     * Adds a named state to a workflow.
     *
     * @param key stable state key
     * @param label display label
     * @param semanticPhase operational meaning shared with repair orders
     * @param initialState whether this is the initial state
     * @param terminalState whether transitions may leave this state
     */
    public record AddStateRequest(
            @NotBlank String key,
            @NotBlank String label,
            @NotNull SemanticPhase semanticPhase,
            boolean initialState,
            boolean terminalState) { }

    /**
     * Adds a directed transition between workflow states.
     *
     * @param fromStateKey source state key
     * @param toStateKey destination state key
     * @param transitionKey stable transition identifier
     * @param requiredPermission server-enforced capability required to execute it
     */
    public record AddTransitionRequest(
            @NotBlank String fromStateKey,
            @NotBlank String toStateKey,
            @NotBlank String transitionKey,
            @NotBlank String requiredPermission) { }

    /**
     * Requests a workflow transition for one aggregate.
     *
     * @param aggregateId tenant-owned aggregate identifier
     * @param fromStateKey expected current state
     * @param transitionKey requested transition
     */
    public record TransitionRequest(
            @NotNull UUID aggregateId,
            @NotBlank String fromStateKey,
            @NotBlank String transitionKey) { }

    /**
     * Workflow state details returned to clients.
     *
     * @param key stable state key
     * @param label display label
     * @param semanticPhase operational meaning
     * @param initialState whether this is the initial state
     * @param terminalState whether this is a terminal state
     * @param active whether the state is currently available
     */
    public record StateResponse(
            String key,
            String label,
            SemanticPhase semanticPhase,
            boolean initialState,
            boolean terminalState,
            boolean active) { }

    /**
     * Updates the display label of a workflow state.
     */
    public record UpdateStateLabelRequest(@NotBlank String label) { }

    /**
     * Transition details returned to clients.
     *
     * @param key stable transition key
     * @param fromStateKey source state key
     * @param toStateKey destination state key
     * @param requiredPermission required backend capability
     */
    public record TransitionResponse(
            String key, String fromStateKey, String toStateKey, String requiredPermission) { }

    /**
     * Active workflow definition with its configured states and transitions.
     *
     * @param id workflow identifier
     * @param verticalKey business vertical key
     * @param name workflow display name
     * @param version optimistic-lock version
     * @param states configured workflow states
     * @param transitions configured workflow transitions
     */
    public record WorkflowResponse(
            UUID id,
            String verticalKey,
            String name,
            int version,
            List<StateResponse> states,
            List<TransitionResponse> transitions) {
        public WorkflowResponse {
            states = immutableList(states);
            transitions = immutableList(transitions);
        }
    }
}
