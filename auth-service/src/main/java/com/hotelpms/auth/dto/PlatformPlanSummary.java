package com.hotelpms.auth.dto;

/** A plan key currently assigned to at least one tenant; no fictional catalog is implied. */
public record PlatformPlanSummary(String planKey, long tenantsAssigned) { }
