package com.hotelpms.frontdesk.workflow.dto;

import com.hotelpms.frontdesk.workflow.domain.SemanticPhase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/** WorkflowDtos domain type. */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class WorkflowDtos {
    private WorkflowDtos() {}

    /**
     * DTO record.
     *
     * @param verticalKey value.
     * @param name value.
     */
    public record CreateWorkflowRequest(@NotBlank String verticalKey, @NotBlank String name) {}

    /**
     * DTO record.
     *
     * @param key value.
     * @param label value.
     * @param semanticPhase value.
     * @param initialState value.
     * @param terminalState value.
     */
    public record AddStateRequest(
            @NotBlank String key,
            @NotBlank String label,
            @NotNull SemanticPhase semanticPhase,
            boolean initialState,
            boolean terminalState) {}

    /**
     * DTO record.
     *
     * @param fromStateKey value.
     * @param toStateKey value.
     * @param transitionKey value.
     * @param requiredPermission value.
     */
    public record AddTransitionRequest(
            @NotBlank String fromStateKey,
            @NotBlank String toStateKey,
            @NotBlank String transitionKey,
            @NotBlank String requiredPermission) {}

    /**
     * DTO record.
     *
     * @param aggregateId value.
     * @param fromStateKey value.
     * @param transitionKey value.
     */
    public record TransitionRequest(
            @NotNull UUID aggregateId,
            @NotBlank String fromStateKey,
            @NotBlank String transitionKey) {}

    /**
     * DTO record.
     *
     * @param key value.
     * @param label value.
     * @param semanticPhase value.
     * @param initialState value.
     * @param terminalState value.
     */
    public record StateResponse(
            String key,
            String label,
            SemanticPhase semanticPhase,
            boolean initialState,
            boolean terminalState,
            boolean active) {}

    public record UpdateStateLabelRequest(@NotBlank String label) {}

    /**
     * DTO record.
     *
     * @param key value.
     * @param fromStateKey value.
     * @param toStateKey value.
     * @param requiredPermission value.
     */
    public record TransitionResponse(
            String key, String fromStateKey, String toStateKey, String requiredPermission) {}

    /**
     * DTO record.
     *
     * @param id value.
     * @param verticalKey value.
     * @param name value.
     * @param version value.
     * @param states value.
     * @param transitions value.
     */
    public record WorkflowResponse(
            UUID id,
            String verticalKey,
            String name,
            int version,
            List<StateResponse> states,
            List<TransitionResponse> transitions) {}
}
