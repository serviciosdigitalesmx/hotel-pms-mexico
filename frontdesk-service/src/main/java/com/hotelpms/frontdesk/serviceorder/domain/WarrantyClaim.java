package com.hotelpms.frontdesk.serviceorder.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Type domain type. */
@Entity
@Table(
        name = "warranty_claims",
        indexes =
                @Index(
                        name = "idx_warranty_claims_hotel_order",
                        columnList = "hotel_id,service_order_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarrantyClaim {
    private static final int DESCRIPTION_LENGTH = 2000;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "hotel_id", nullable = false)
    private UUID hotelId;

    @Column(name = "service_order_id", nullable = false)
    private UUID serviceOrderId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(nullable = false, length = 32)
    @Builder.Default
    private String status = "OPEN";

    @Column(nullable = false)
    private LocalDate expiresAt;

    @Column(nullable = false, length = DESCRIPTION_LENGTH)
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
