package com.hotelpms.frontdesk.serviceorder.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record QuotationUpdateRequest(@NotBlank String proposedSolution,@Positive Integer estimatedMinutes,@NotNull @PositiveOrZero BigDecimal laborAmount,@NotNull @PositiveOrZero BigDecimal partsAmount,@NotNull @PositiveOrZero BigDecimal taxAmount){}
