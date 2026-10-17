package com.hotelpms.billing.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Expense capture request.
 *
 * @param category expense category.
 * @param amount expense amount.
 * @param description optional description.
 * @param reference optional reference.
 * @param cashSessionId optional cash session identifier.
 * @param expenseType optional expense classification
 * @param subcategory optional expense subcategory
 * @param paid whether payment has already been made
 * @param spentAt business date of the expense
 * @param dueAt date an unpaid expense is due
 * @param paymentMethod method used to pay
 */
public record ExpenseRequest(
    @NotBlank @Size(max = CATEGORY_LENGTH) String category,
    @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal amount,
    @Size(max = DESCRIPTION_LENGTH) String description,
    @Size(max = 100) String reference,
    UUID cashSessionId,
    @jakarta.validation.constraints.Pattern(regexp = "FIXED|VARIABLE|INVENTORY") String expenseType,
    @Size(max = CATEGORY_LENGTH) String subcategory,
    Boolean paid,
    LocalDate spentAt,
    LocalDate dueAt,
    @jakarta.validation.constraints.Pattern(regexp = "CASH|BANK_TRANSFER|CARD|OTHER") String paymentMethod) {
  private static final int CATEGORY_LENGTH = 120;
  private static final int DESCRIPTION_LENGTH = 255;

  /** Preserves the original variable-expense request contract.
   *
   * @param category expense category
   * @param amount expense amount
   * @param description optional description
   * @param reference optional reference
   * @param cashSessionId optional cash session identifier
   */
  public ExpenseRequest(
      final String category,
      final BigDecimal amount,
      final String description,
      final String reference,
      final UUID cashSessionId) {
    this(category, amount, description, reference, cashSessionId, "VARIABLE", null, true, null, null, null);
  }
}
