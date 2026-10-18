package com.hotelpms.frontdesk.workflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.util.UUID;

/** Persisted operational state; definitions alone cannot protect transitions. */
@Entity
@Table(name = "workflow_instances")
public class WorkflowInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "hotel_id", nullable = false)
    private UUID hotelId;

    @Column(name = "workflow_id", nullable = false)
    private UUID workflowId;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "state_key", nullable = false)
    private String stateKey;

    @Version private long version;

    /** Required by JPA. */
    protected WorkflowInstance() {
        /* Required by JPA. */
    }

    /**
     * Executes the operation.
     *
     * @param hotelId parameter.
     * @param workflowId parameter.
     * @param aggregateId parameter.
     * @param stateKey parameter.
     */
    public WorkflowInstance(
            final UUID hotelId,
            final UUID workflowId,
            final UUID aggregateId,
            final String stateKey) {
        this.hotelId = hotelId;
        this.workflowId = workflowId;
        this.aggregateId = aggregateId;
        this.stateKey = stateKey;
    }

    /**
     * Executes the operation.
     *
     * @return operation result.
     */
    public String getStateKey() {
        return stateKey;
    }

    /**
     * Executes the operation.
     *
     * @param key parameter.
     */
    public void moveTo(final String key) {
        stateKey = key;
    }
}
