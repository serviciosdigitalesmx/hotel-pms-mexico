package com.hotelpms.billing.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Validated recurring expense template input.
 *
 * @param category expense category
 * @param amount amount per period
 * @param description optional expense description
 * @param reference optional reference
 * @param subcategory optional expense subcategory
 * @param frequency supported recurrence interval
 * @param nextDueAt first scheduled due date
 */
public record RecurringExpenseRequest(
    @NotBlank @Size(max = CATEGORY_LENGTH) String category,
    @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal amount,
    @Size(max = DESCRIPTION_LENGTH) String description,
    @Size(max = REFERENCE_LENGTH) String reference,
    @Size(max = CATEGORY_LENGTH) String subcategory,
    @NotBlank @Pattern(regexp = "MONTHLY|WEEKLY|YEARLY") String frequency,
    @NotNull LocalDate nextDueAt) {
  static final int CATEGORY_LENGTH = 120;
  static final int DESCRIPTION_LENGTH = 255;
  static final int REFERENCE_LENGTH = 100;
}
