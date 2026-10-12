package com.hotelpms.frontdesk.whatsapp;

import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Type domain type. */
@Entity
@Table(
    name = "whatsapp_webhook_events",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_whatsapp_webhook_event",
            columnNames = {"hotel_id", "event_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookEvent {
  private static final int EVENT_ID_LENGTH = 150;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "hotel_id", nullable = false)
  private UUID hotelId;

  @Column(name = "event_id", nullable = false, length = EVENT_ID_LENGTH)
  private String eventId;

  @Column(nullable = false, updatable = false)
  private LocalDateTime receivedAt;
}
