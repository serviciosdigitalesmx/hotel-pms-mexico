package com.hotelpms.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Explicit platform tenant lifecycle transition. */
public record UpdatePlatformTenantStatusRequest(@NotBlank String status) { }
