package com.hotelpms.frontdesk.serviceorder.domain;

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

/**
 * Auditable status changes for tenant-owned repair orders.
 */
@Entity
@Table(name = "service_order_status_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOrderStatusHistory {
    private static final int MAX_ACTOR_LENGTH = 100;
    private static final int MAX_NOTES_LENGTH = 8000;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID hotelId;

    @Column(nullable = false)
    private UUID serviceOrderId;

    @Enumerated(EnumType.STRING)
    private ServiceOrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceOrderStatus toStatus;

    @Column(nullable = false, length = MAX_ACTOR_LENGTH)
    private String actor;

    @Column(length = MAX_NOTES_LENGTH)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
