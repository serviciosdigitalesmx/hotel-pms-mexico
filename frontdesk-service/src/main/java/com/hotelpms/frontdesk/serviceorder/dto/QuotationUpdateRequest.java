package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Validated quote amounts and proposed repair solution submitted for customer approval.
 *
 * @param proposedSolution explanation of the proposed work
 * @param estimatedMinutes expected repair duration
 * @param laborAmount quoted labor amount
 * @param partsAmount quoted parts amount
 * @param taxAmount quoted tax amount
 */
public record QuotationUpdateRequest(
        @NotBlank @Size(max = MAX_PROPOSED_SOLUTION_LENGTH) String proposedSolution,
        @Positive Integer estimatedMinutes,
        @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal laborAmount,
        @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal partsAmount,
        @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal taxAmount) {
    private static final int MAX_PROPOSED_SOLUTION_LENGTH = 2000;
}
