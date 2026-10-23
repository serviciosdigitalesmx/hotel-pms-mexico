package com.hotelpms.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Explicit platform authority grant linked to an existing Fixi login identity. */
@Entity
@Table(name = "platform_admin_grant")
@Getter
@Setter
@NoArgsConstructor
public class PlatformAdminGrant {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private UserAccount user;

  @Enumerated(EnumType.STRING)
  @Column(name = "platform_role", nullable = false, length = 32)
  private PlatformRole platformRole;

  @Column(nullable = false)
  private boolean active = true;

  @Column(name = "granted_at", nullable = false, updatable = false)
  private Instant grantedAt = Instant.now();

  @Column(name = "granted_by", nullable = false, length = 80)
  private String grantedBy;
}
