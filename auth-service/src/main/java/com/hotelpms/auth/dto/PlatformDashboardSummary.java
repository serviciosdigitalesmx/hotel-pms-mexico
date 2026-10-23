package com.hotelpms.auth.dto;

/** Real aggregate counts for the platform dashboard.
 * @param tenantsTotal number of registered tenants
 * @param tenantsActive number of active tenants
 * @param tenantsSuspended number of suspended tenants
 * @param usersTotal number of accounts
 * @param ownersTotal number of tenant owner accounts
 * @param serviceOrdersTotal number of service orders
 */
public record PlatformDashboardSummary(
    long tenantsTotal,
    long tenantsActive,
    long tenantsSuspended,
    long usersTotal,
    long ownersTotal,
    long serviceOrdersTotal) { }
