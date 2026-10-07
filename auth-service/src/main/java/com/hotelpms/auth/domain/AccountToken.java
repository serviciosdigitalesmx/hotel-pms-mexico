package com.hotelpms.auth.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "account_token") @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AccountToken {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @Column(nullable = false) private UUID userId;
  @Column(nullable = false, length = 32) private String purpose;
  @Column(nullable = false, unique = true, length = 64) private String tokenHash;
  @Column(nullable = false) private LocalDateTime expiresAt;
  private LocalDateTime consumedAt;
  @Column(nullable = false) private LocalDateTime createdAt;
}
