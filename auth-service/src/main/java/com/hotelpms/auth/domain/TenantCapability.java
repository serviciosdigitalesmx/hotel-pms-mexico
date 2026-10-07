package com.hotelpms.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Stores a tenant-specific capability setting. */
@Entity
@Table(name = "tenant_capability")
@IdClass(TenantCapability.Key.class)
@Data
@NoArgsConstructor
public class TenantCapability {
  private static final int CAPABILITY_KEY_LENGTH = 120;

  @Id
  @Column(name = "tenant_id")
  private UUID tenantId;

  @Id
  @Column(name = "capability_key", length = CAPABILITY_KEY_LENGTH)
  private String capabilityKey;

  @Column(nullable = false)
  private boolean enabled;

  @Column(name = "limit_value")
  private Long limitValue;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /**
   * Composite identifier for a tenant capability.
   *
   * @param tenantId tenant identifier
   * @param capabilityKey capability key
   */
  public record Key(UUID tenantId, String capabilityKey) implements Serializable { }
}
