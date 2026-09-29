package com.hotelpms.frontdesk.workflow.domain;

import jakarta.persistence.*;
import java.util.UUID;

/** Persisted operational state; definitions alone cannot protect transitions. */
@Entity
@Table(name = "workflow_instances")
public class WorkflowInstance {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "hotel_id", nullable = false) private UUID hotelId;
    @Column(name = "workflow_id", nullable = false) private UUID workflowId;
    @Column(name = "aggregate_id", nullable = false) private UUID aggregateId;
    @Column(name = "state_key", nullable = false) private String stateKey;
    @Version private long version;

    protected WorkflowInstance() { }

    public WorkflowInstance(UUID hotelId, UUID workflowId, UUID aggregateId, String stateKey) {
        this.hotelId = hotelId;
        this.workflowId = workflowId;
        this.aggregateId = aggregateId;
        this.stateKey = stateKey;
    }

    public String getStateKey() { return stateKey; }
    public void moveTo(String key) { stateKey = key; }
}
