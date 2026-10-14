package com.hotelpms.auth.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** Authenticated user's inactivity lock preference. */
public record SessionSecurityPreferenceRequest(@Min(0) @Max(60) int idleTimeoutMinutes) { }
