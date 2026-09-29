package com.hotelpms.frontdesk.workflow.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="workflow_definitions")
public class WorkflowDefinition {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(name="hotel_id", nullable=false) private UUID hotelId;
    @Column(name="vertical_key", nullable=false) private String verticalKey;
    @Column(nullable=false) private String name;
    @Column(nullable=false) private boolean active = true;
    @Column(nullable=false) private int version = 1;
    @Column(name="created_at", nullable=false) private Instant createdAt;
    @Column(name="updated_at", nullable=false) private Instant updatedAt;
    protected WorkflowDefinition() {}
    public WorkflowDefinition(UUID hotelId, String verticalKey, String name) { this.hotelId=hotelId; this.verticalKey=verticalKey; this.name=name; this.createdAt=Instant.now(); this.updatedAt=this.createdAt; }
    public UUID getId(){return id;} public UUID getHotelId(){return hotelId;} public String getVerticalKey(){return verticalKey;} public String getName(){return name;} public boolean isActive(){return active;} public int getVersion(){return version;}
}
