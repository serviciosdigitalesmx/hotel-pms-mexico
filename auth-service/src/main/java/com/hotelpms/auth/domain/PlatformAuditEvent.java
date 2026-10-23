package com.hotelpms.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Immutable record of a sensitive platform-administration operation. */
@Entity
@Table(name = "platform_audit_event")
@Getter
@Setter
@NoArgsConstructor
public class PlatformAuditEvent {
  @Id private UUID id;

  @Column(name = "actor_username", nullable = false, length = 50)
  private String actorUsername;

  @Column(nullable = false, length = 80)
  private String action;

  @Column(name = "target_tenant_id")
  private UUID targetTenantId;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(name = "metadata_json", nullable = false, columnDefinition = "text")
  private String metadataJson;
}
