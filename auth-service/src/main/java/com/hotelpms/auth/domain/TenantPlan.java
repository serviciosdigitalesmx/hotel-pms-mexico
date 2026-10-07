package com.hotelpms.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Stores the active subscription plan assigned to a tenant. */
@Entity
@Table(name = "tenant_plan")
@Data
@NoArgsConstructor
public class TenantPlan {
  private static final int PLAN_KEY_LENGTH = 80;

  @Id
  @Column(name = "tenant_id")
  private UUID tenantId;

  @Column(name = "plan_key", nullable = false, length = PLAN_KEY_LENGTH)
  private String planKey;

  @Column(nullable = false)
  private boolean active;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
