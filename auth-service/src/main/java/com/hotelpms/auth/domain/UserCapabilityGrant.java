package com.hotelpms.auth.domain;

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
import lombok.Data;
import lombok.NoArgsConstructor;

/** Tenant and optional branch-scoped capability grant for one user. */
@Entity
@Table(name = "user_capability_grant")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCapabilityGrant {

  private static final int CAPABILITY_KEY_LENGTH = 120;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private UUID userId;

  private UUID branchId;

  @Column(nullable = false, length = CAPABILITY_KEY_LENGTH)
  private String capabilityKey;

  @Column(nullable = false)
  private boolean enabled;

  @Column(nullable = false)
  private LocalDateTime updatedAt;
}
