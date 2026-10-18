package com.hotelpms.frontdesk.workflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

/** Type domain type. */
@Entity
@Table(name = "workflow_states")
public class WorkflowState {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private WorkflowDefinition workflow;

    @Column(name = "state_key", nullable = false)
    private String key;

    @Column(nullable = false)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "semantic_phase", nullable = false)
    private SemanticPhase semanticPhase;

    @Column(name = "initial_state")
    private boolean initialState;

    @Column(name = "terminal_state")
    private boolean terminalState;

    @Column(nullable = false)
    private boolean active = true;

    /** Required by JPA. */
    protected WorkflowState() {
        /* Required by JPA. */
    }

    /**
     * Executes the operation.
     *
     * @param w parameter.
     * @param key parameter.
     * @param label parameter.
     * @param phase parameter.
     * @param initial parameter.
     * @param terminal parameter.
     */
    public WorkflowState(
            final WorkflowDefinition w,
            final String key,
            final String label,
            final SemanticPhase phase,
            final boolean initial,
            final boolean terminal) {
        this.workflow = w;
        this.key = key;
        this.label = label;
        this.semanticPhase = phase;
        this.initialState = initial;
        this.terminalState = terminal;
    }

    /**
     * Executes the operation.
     *
     * @return operation result.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Executes the operation.
     *
     * @return operation result.
     */
    public WorkflowDefinition getWorkflow() {
        return workflow;
    }

    /**
     * Executes the operation.
     *
     * @return operation result.
     */
    public String getKey() {
        return key;
    }

    /**
     * Executes the operation.
     *
     * @return operation result.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Executes the operation.
     *
     * @return operation result.
     */
    public SemanticPhase getSemanticPhase() {
        return semanticPhase;
    }

    /**
     * Executes the operation.
     *
     * @return operation result.
     */
    public boolean isInitialState() {
        return initialState;
    }

    /**
     * Executes the operation.
     *
     * @return operation result.
     */
    public boolean isTerminalState() {
        return terminalState;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public void setLabel(final String label) {
        this.label = label;
    }
}
