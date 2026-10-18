package com.hotelpms.frontdesk.automation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/** Tenant-scoped automation rule definition; execution remains event-driven and auditable. */
@Entity
@Table(name = "automation_rules")
@Getter
@Setter
@NoArgsConstructor
public class AutomationRule {
    private static final int DOMAIN_LENGTH = 24;
    private static final int EVENT_TYPE_LENGTH = 120;
    private static final int ACTION_TYPE_LENGTH = 120;

    /** Business area whose events can trigger an automation rule. */
    public enum Domain {
        ORDER,
        QUOTE,
        PROCUREMENT,
        PAYMENT,
        WARRANTY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "hotel_id", nullable = false)
    private UUID hotelId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = DOMAIN_LENGTH)
    private Domain domain;

    @Column(name = "event_type", nullable = false, length = EVENT_TYPE_LENGTH)
    private String eventType;

    @Column(name = "action_type", nullable = false, length = ACTION_TYPE_LENGTH)
    private String actionType;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
