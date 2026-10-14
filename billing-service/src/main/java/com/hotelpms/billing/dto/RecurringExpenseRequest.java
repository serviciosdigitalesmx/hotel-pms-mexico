package com.hotelpms.billing.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Validated recurring expense template input. */
public record RecurringExpenseRequest(
    @NotBlank @Size(max = 120) String category,
    @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal amount,
    @Size(max = 255) String description,
    @Size(max = 100) String reference,
    @Size(max = 120) String subcategory,
    @NotBlank @Pattern(regexp = "MONTHLY|WEEKLY|YEARLY") String frequency,
    @NotNull LocalDate nextDueAt) {}
