package com.hotelpms.frontdesk.serviceorder.domain;

import jakarta.persistence.Index;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Type domain type. */
@Entity
@Table(
    name = "service_orders",
    indexes =
        @Index(name = "idx_service_orders_hotel_customer", columnList = "hotel_id,customer_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOrder {
  private static final int SUMMARY_LENGTH = 500;
  private static final int DIAGNOSIS_LENGTH = 4000;
  private static final int WORK_LOG_LENGTH = 8000;
  private static final int AUTHORIZATION_EVIDENCE_LENGTH = 2000;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "hotel_id", nullable = false)
  private UUID hotelId;

  @Column(name = "branch_id")
  private UUID branchId;

  @Column(name = "customer_id", nullable = false)
  private UUID customerId;

  @Column(name = "device_id")
  private UUID deviceId;

  @Column(name = "reported_failure", length = SUMMARY_LENGTH)
  private String reportedFailure;

  @Column(nullable = false, length = 16)
  @Builder.Default
  private String priority = "NORMAL";

  @Column(name = "service_name", length = 200)
  private String serviceName;

  @Column(name = "deposit_amount", precision = 12, scale = 2)
  @Builder.Default
  private BigDecimal depositAmount = BigDecimal.ZERO;

  @Column(name = "intake_checklist", length = 4000)
  private String intakeChecklist;

  @Column(name = "promised_at")
  private LocalDateTime promisedAt;

  @Column(name = "intake_evidence", length = 4000)
  private String intakeEvidence;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  @Builder.Default
  private ServiceOrderStatus status = ServiceOrderStatus.INTAKE;

  @Column(nullable = false, length = SUMMARY_LENGTH)
  private String summary;

  @Column(length = DIAGNOSIS_LENGTH)
  private String diagnosis;

  @Column(name = "work_log", length = WORK_LOG_LENGTH)
  private String workLog;

  @Column(name = "assigned_user_id")
  private UUID assignedUserId;

  @Column(name = "authorized_by")
  private UUID authorizedBy;

  @Column(name = "authorization_evidence", length = AUTHORIZATION_EVIDENCE_LENGTH)
  private String authorizationEvidence;

  @Column(name = "authorized_at")
  private LocalDateTime authorizedAt;

  @Column(length = DIAGNOSIS_LENGTH) private String proposedSolution;
  private Integer estimatedMinutes;
  @Column(precision=12,scale=2) private BigDecimal laborAmount;
  @Column(precision=12,scale=2) private BigDecimal partsAmount;
  @Column(precision=12,scale=2) private BigDecimal taxAmount;
  @Column(precision=12,scale=2) private BigDecimal quoteTotal;
  private LocalDateTime deliveredAt;
  private UUID deliveredBy;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;
}
