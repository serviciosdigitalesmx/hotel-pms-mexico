package com.hotelpms.auth.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Authenticated user's inactivity lock preference.
 *
 * @param idleTimeoutMinutes allowed idle timeout in minutes
 */
public record SessionSecurityPreferenceRequest(
    @Min(0) @Max(MAX_IDLE_TIMEOUT_MINUTES) int idleTimeoutMinutes) {
  private static final int MAX_IDLE_TIMEOUT_MINUTES = 60;
}
