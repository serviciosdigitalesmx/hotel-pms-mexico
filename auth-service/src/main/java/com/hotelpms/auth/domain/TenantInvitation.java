package com.hotelpms.auth.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "tenant_invitation") @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TenantInvitation {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @Column(nullable = false) private UUID hotelId;
  @Column(nullable = false, length = 100) private String email;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private Role role;
  private UUID branchId;
  @Column(nullable = false, unique = true, length = 64) private String tokenHash;
  @Column(nullable = false) private LocalDateTime expiresAt;
  private LocalDateTime acceptedAt;
  @Column(nullable = false) private UUID createdBy;
  @Column(nullable = false) private LocalDateTime createdAt;
}
