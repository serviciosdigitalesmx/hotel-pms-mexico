package com.hotelpms.frontdesk.notifications;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/** Transactional outbox entry for operational alerts awaiting notification-service acceptance. */
@Entity
@Table(
        name = "operational_notification_outbox",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uq_operational_notification_hotel_event",
                        columnNames = {"hotel_id", "event_key"}))
@Getter
@Setter
@NoArgsConstructor
public class OperationalNotificationOutbox {
    public enum Status {
        PENDING,
        SENT,
        FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "hotel_id", nullable = false)
    private UUID hotelId;

    @Column(name = "event_key", nullable = false, length = 180)
    private String eventKey;

    @Column(name = "source_key", nullable = false, length = 180)
    private String sourceKey;

    @Column(name = "event_type", nullable = false, length = 120)
    private String eventType;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private Status status = Status.PENDING;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "next_attempt_at", nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}
