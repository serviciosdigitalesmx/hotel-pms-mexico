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

/** Tenant-scoped template for explicitly generated recurring expenses. */
@Entity
@Table(name = "recurring_expense_templates")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringExpenseTemplate {
  private static final int CATEGORY_LENGTH = 120;
  private static final int AMOUNT_PRECISION = 12;
  private static final int AMOUNT_SCALE = 2;
  private static final int DESCRIPTION_LENGTH = 255;
  private static final int REFERENCE_LENGTH = 100;
  private static final int FREQUENCY_LENGTH = 16;
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "hotel_id", nullable = false)
  private UUID hotelId;

  @Column(nullable = false, length = CATEGORY_LENGTH)
  private String category;

  @Column(nullable = false, precision = AMOUNT_PRECISION, scale = AMOUNT_SCALE)
  private BigDecimal amount;

  @Column(length = DESCRIPTION_LENGTH)
  private String description;

  @Column(length = REFERENCE_LENGTH)
  private String reference;

  @Column(length = CATEGORY_LENGTH)
  private String subcategory;

  @Column(nullable = false, length = FREQUENCY_LENGTH)
  private String frequency;

  @Column(name = "next_due_at", nullable = false)
  private LocalDate nextDueAt;

  @Column(nullable = false)
  private boolean active;

  @Column(name = "created_by", nullable = false)
  private UUID createdBy;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
