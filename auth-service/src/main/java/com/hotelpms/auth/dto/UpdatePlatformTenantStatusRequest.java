package com.hotelpms.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Explicit platform tenant lifecycle transition.
 *
 * @param status requested lifecycle status
 */
public record UpdatePlatformTenantStatusRequest(@NotBlank String status) { }
