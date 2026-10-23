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

/** Administrative history of plan assignments; it is not a payment-provider subscription. */
@Entity
@Table(name = "platform_tenant_subscription")
@Getter
@Setter
@NoArgsConstructor
public class PlatformTenantSubscription {
  @Id private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "plan_key", nullable = false, length = 80)
  private String planKey;

  @Column(nullable = false, length = 24)
  private String status;

  @Column(name = "starts_at", nullable = false)
  private Instant startsAt;

  @Column(name = "ends_at")
  private Instant endsAt;

  @Column(name = "created_by", nullable = false, length = 50)
  private String createdBy;
}
