package com.hotelpms.auth.dto;

/** Real aggregate counts for the platform dashboard. */
public record PlatformDashboardSummary(
    long tenantsTotal,
    long tenantsActive,
    long tenantsSuspended,
    long usersTotal,
    long ownersTotal,
    long serviceOrdersTotal) { }
