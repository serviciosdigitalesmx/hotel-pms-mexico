package com.hotelpms.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Idempotent, retryable tenant notification delivery record. */
@Entity
@Table(name = "notification_deliveries")
@Getter
@Setter
@NoArgsConstructor
public class NotificationDelivery {
  public enum Status { PENDING, SENT, FAILED }
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @Column(name = "hotel_id", nullable = false) private UUID hotelId;
  @Column(name = "event_key", nullable = false, unique = true, length = 180) private String eventKey;
  @Column(name = "event_type", nullable = false, length = 120) private String eventType;
  @Column(nullable = false, columnDefinition = "text") private String payload;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24) private Status status = Status.PENDING;
  @Column(nullable = false) private int attempts;
  @Column(name = "last_error", columnDefinition = "text") private String lastError;
  @Column(name = "created_at", nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
  @Column(name = "sent_at") private LocalDateTime sentAt;
}
