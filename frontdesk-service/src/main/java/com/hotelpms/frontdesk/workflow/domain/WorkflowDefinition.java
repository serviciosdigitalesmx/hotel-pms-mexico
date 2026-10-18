package com.hotelpms.frontdesk.workflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Type domain type. */
@Entity
@Table(name = "workflow_definitions")
@SuppressWarnings({"PMD.ImmutableField", "PMD.SingularField"})
public class WorkflowDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "hotel_id", nullable = false)
    private UUID hotelId;

    @Column(name = "vertical_key", nullable = false)
    private String verticalKey;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private int version = 1;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Required by JPA. */
    protected WorkflowDefinition() {
        /* Required by JPA. */
    }

    /**
     * Executes the operation.
     *
     * @param hotelId parameter.
     * @param verticalKey parameter.
     * @param name parameter.
     */
    public WorkflowDefinition(final UUID hotelId, final String verticalKey, final String name) {
        this.hotelId = hotelId;
        this.verticalKey = verticalKey;
        this.name = name;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
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
    public UUID getHotelId() {
        return hotelId;
    }

    /**
     * Executes the operation.
     *
     * @return operation result.
     */
    public String getVerticalKey() {
        return verticalKey;
    }

    /**
     * Executes the operation.
     *
     * @return operation result.
     */
    public String getName() {
        return name;
    }

    /**
     * Executes the operation.
     *
     * @return operation result.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Executes the operation.
     *
     * @return operation result.
     */
    public int getVersion() {
        return version;
    }
}
