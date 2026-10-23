package com.hotelpms.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Administrative plan assignment without payment-provider semantics. */
public record AssignPlatformPlanRequest(
    @NotBlank @Size(max = 80) @Pattern(regexp = "[A-Za-z0-9._-]+") String planKey) { }
