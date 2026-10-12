package com.hotelpms.frontdesk.audit.domain;

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

/** Append-only tenant audit event for security, operations, finance and inventory. */
@Entity
@Table(name = "audit_events")
@Getter
@Setter
@NoArgsConstructor
public class AuditEvent {
  public enum Category { SECURITY, OPERATIONAL, FINANCIAL, INVENTORY }

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  @Column(name = "hotel_id", nullable = false)
  private UUID hotelId;
  @Column(name = "actor_id")
  private UUID actorId;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private Category category;
  @Column(name = "event_type", nullable = false, length = 120)
  private String eventType;
  @Column(name = "aggregate_id")
  private UUID aggregateId;
  @Column(name = "correlation_id", length = 120)
  private String correlationId;
  @Column(nullable = false, columnDefinition = "text")
  private String details;
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();
}
