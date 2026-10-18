package com.hotelpms.frontdesk.portal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/** One-time portal verification challenge. The code itself is never persisted. */
@Entity
@Table(name = "portal_otp_challenges")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalOtpChallenge {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "token_id", nullable = false)
    private UUID tokenId;

    @Column(name = "code_hash", nullable = false, length = 64)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private int attempts;

    @Column(nullable = false)
    private boolean consumed;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
