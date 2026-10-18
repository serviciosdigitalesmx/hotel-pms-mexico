package com.hotelpms.frontdesk.workflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "workflow_transitions")
public class WorkflowTransition {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private WorkflowDefinition workflow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_state_id", nullable = false)
    private WorkflowState fromState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_state_id", nullable = false)
    private WorkflowState toState;

    @Column(name = "transition_key", nullable = false)
    private String key;

    @Column(name = "required_permission", nullable = false)
    private String requiredPermission;

    /** Required by JPA. */
    protected WorkflowTransition() {
        /* Required by JPA. */
    }

    /**
     * Executes the operation.
     *
     * @param w parameter.
     * @param from parameter.
     * @param to parameter.
     * @param key parameter.
     * @param permission parameter.
     */
    public WorkflowTransition(
            final WorkflowDefinition w,
            final WorkflowState from,
            final WorkflowState to,
            final String key,
            final String permission) {
        this.workflow = w;
        this.fromState = from;
        this.toState = to;
        this.key = key;
        this.requiredPermission = permission;
    }

    /**
     * Executes the operation.
     *
     * @return operation result.
     */
    public WorkflowState getFromState() {
        return fromState;
    }

    /**
     * Executes the operation.
     *
     * @return operation result.
     */
    public WorkflowState getToState() {
        return toState;
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
    public String getRequiredPermission() {
        return requiredPermission;
    }
}
