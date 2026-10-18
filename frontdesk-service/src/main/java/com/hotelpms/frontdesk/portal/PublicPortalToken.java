package com.hotelpms.frontdesk.portal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/** Revocable, expiring token scoped to one tenant and one customer-facing resource. */
@Entity
@Table(name = "public_portal_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:ExplicitInitialization"})
public class PublicPortalToken {
    private static final int RESOURCE_TYPE_LENGTH = 40;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "hotel_id", nullable = false)
    private UUID hotelId;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = RESOURCE_TYPE_LENGTH)
    private ResourceType resourceType;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Supported public resource types. */
    public enum ResourceType {
        SERVICE_ORDER,
        QUOTATION
    }
}
