package com.hotelpms.billing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Tenant-scoped operating expense. */
@Entity
@Table(name = "expenses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {
  private static final int MONEY_PRECISION = 12;
  private static final int MONEY_SCALE = 2;
  private static final int CATEGORY_LENGTH = 120;
  private static final int DESCRIPTION_LENGTH = 255;
  private static final int REFERENCE_LENGTH = 100;
  private static final int PAYMENT_METHOD_LENGTH = 20;
  private static final int EXPENSE_TYPE_LENGTH = 16;
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "hotel_id", nullable = false)
  private UUID hotelId;

  @Column(nullable = false, length = CATEGORY_LENGTH)
  private String category;

  @Column(nullable = false, precision = MONEY_PRECISION, scale = MONEY_SCALE)
  private BigDecimal amount;

  @Column(length = DESCRIPTION_LENGTH)
  private String description;

  @Column(length = REFERENCE_LENGTH)
  private String reference;

  @Column(name = "cash_session_id")
  private UUID cashSessionId;

  @Column(name = "created_by", nullable = false)
  private UUID createdBy;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Builder.Default
  @Column(name = "expense_type", nullable = false, length = EXPENSE_TYPE_LENGTH)
  private String expenseType = "VARIABLE";

  @Column(length = CATEGORY_LENGTH)
  private String subcategory;

  @Builder.Default
  @Column(nullable = false)
  private boolean paid = true;

  @Column(name = "spent_at")
  private LocalDate spentAt;

  @Column(name = "due_at")
  private LocalDate dueAt;

  @Column(name = "payment_method", length = PAYMENT_METHOD_LENGTH)
  private String paymentMethod;

  @Column(name = "paid_at")
  private LocalDateTime paidAt;

  @Column(name = "recurring_template_id")
  private UUID recurringTemplateId;

  @Column(name = "recurring_period_due_at")
  private LocalDate recurringPeriodDueAt;
}
