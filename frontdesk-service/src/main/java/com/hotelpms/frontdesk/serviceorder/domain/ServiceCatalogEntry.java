package com.hotelpms.frontdesk.serviceorder.domain;

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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tenant-owned catalog entry for a repair service offered by Fixi.
 */
@Entity
@Table(name = "service_catalog")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCatalogEntry {
    private static final int SERVICE_NAME_LENGTH = 200;
    private static final int MONEY_PRECISION = 12;
    private static final int MONEY_SCALE = 2;
    private static final int DEFAULT_ESTIMATED_MINUTES = 60;
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "hotel_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = SERVICE_NAME_LENGTH)
    private String name;

    @Column(
            name = "base_price",
            nullable = false,
            precision = MONEY_PRECISION,
            scale = MONEY_SCALE)
    @Builder.Default
    private BigDecimal basePrice = BigDecimal.ZERO;

    @Column(name = "estimated_minutes", nullable = false)
    @Builder.Default
    private Integer estimatedMinutes = DEFAULT_ESTIMATED_MINUTES;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
