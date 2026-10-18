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

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stores a technician's diagnostic or quality-control result for a repair order.
 */
@Entity
@Table(name = "service_order_tests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepairTest {
    private static final int MAX_TEST_NAME_LENGTH = 160;
    private static final int MAX_NOTES_LENGTH = 2000;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID hotelId;

    @Column(nullable = false)
    private UUID serviceOrderId;

    @Column(nullable = false, length = MAX_TEST_NAME_LENGTH)
    private String testName;

    @Column(nullable = false)
    private boolean passed;

    @Column(length = MAX_NOTES_LENGTH)
    private String notes;

    private UUID technicianId;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
