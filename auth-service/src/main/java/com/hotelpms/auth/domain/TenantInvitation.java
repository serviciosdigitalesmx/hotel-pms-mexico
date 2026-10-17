package com.hotelpms.auth.domain;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One-time invitation carrying a tenant-scoped role and optional branch assignment. */
@Entity
@Table(name = "tenant_invitation")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantInvitation {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private UUID hotelId;

  @Column(nullable = false, length = 100)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private Role role;

  private UUID branchId;

  @Column(nullable = false, unique = true, length = 64)
  private String tokenHash;

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  private LocalDateTime acceptedAt;

  @Column(nullable = false)
  private UUID createdBy;

  @Column(nullable = false)
  private LocalDateTime createdAt;
}
