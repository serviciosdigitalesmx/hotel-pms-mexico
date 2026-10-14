package com.hotelpms.billing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
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

  @Column(length = 100)
  private String reference;

  @Column(name = "cash_session_id")
  private UUID cashSessionId;

  @Column(name = "created_by", nullable = false)
  private UUID createdBy;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Builder.Default
  @Column(name = "expense_type", nullable = false, length = 16)
  private String expenseType = "VARIABLE";

  @Column(length = 120)
  private String subcategory;

  @Builder.Default
  @Column(nullable = false)
  private boolean paid = true;

  @Column(name = "spent_at")
  private LocalDate spentAt;

  @Column(name = "due_at")
  private LocalDate dueAt;
}
