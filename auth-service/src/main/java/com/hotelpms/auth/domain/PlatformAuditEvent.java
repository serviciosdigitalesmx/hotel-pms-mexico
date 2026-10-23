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
  private static final int USERNAME_LENGTH = 50;
  private static final int ACTION_LENGTH = 80;

  @Id private UUID id;

  @Column(name = "actor_username", nullable = false, length = USERNAME_LENGTH)
  private String actorUsername;

  @Column(nullable = false, length = ACTION_LENGTH)
  private String action;

  @Column(name = "target_tenant_id")
  private UUID targetTenantId;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(name = "metadata_json", nullable = false, columnDefinition = "text")
  private String metadataJson;
}
