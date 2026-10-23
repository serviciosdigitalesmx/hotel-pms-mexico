package com.hotelpms.auth.dto;

/** A plan key currently assigned to at least one tenant; no fictional catalog is implied.
 * @param planKey assigned plan key
 * @param tenantsAssigned number of tenants with this plan key
 */
public record PlatformPlanSummary(String planKey, long tenantsAssigned) { }
