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
  private static final int PLAN_KEY_LENGTH = 80;
  private static final int STATUS_LENGTH = 24;
  private static final int USERNAME_LENGTH = 50;

  @Id private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "plan_key", nullable = false, length = PLAN_KEY_LENGTH)
  private String planKey;

  @Column(nullable = false, length = STATUS_LENGTH)
  private String status;

  @Column(name = "starts_at", nullable = false)
  private Instant startsAt;

  @Column(name = "ends_at")
  private Instant endsAt;

  @Column(name = "created_by", nullable = false, length = USERNAME_LENGTH)
  private String createdBy;
}
