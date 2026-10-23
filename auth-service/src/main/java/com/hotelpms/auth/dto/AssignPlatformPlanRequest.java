package com.hotelpms.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Administrative plan assignment without payment-provider semantics.
 *
 * @param planKey existing plan key to assign
 */
public record AssignPlatformPlanRequest(
    @NotBlank @Size(max = MAX_PLAN_KEY_LENGTH) @Pattern(regexp = "[A-Za-z0-9._-]+") String planKey) {
  private static final int MAX_PLAN_KEY_LENGTH = 80;
}
