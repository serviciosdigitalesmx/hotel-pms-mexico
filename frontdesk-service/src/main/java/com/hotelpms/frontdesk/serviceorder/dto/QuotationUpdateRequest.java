package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record QuotationUpdateRequest(
        @NotBlank @Size(max = 2000) String proposedSolution,
        @Positive Integer estimatedMinutes,
        @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal laborAmount,
        @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal partsAmount,
        @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal taxAmount) {}
