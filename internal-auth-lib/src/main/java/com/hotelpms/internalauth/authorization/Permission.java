package com.hotelpms.internalauth.authorization;

/**
 * Fine-grained capabilities. Roles may be used as templates to populate these
 * capabilities, but a role name is never an authorization decision by itself.
 */
public enum Permission {
    TENANT_READ,
    TENANT_ADMIN,
    BRANCH_READ,
    BRANCH_ADMIN,
    CUSTOMER_READ,
    CUSTOMER_WRITE,
    DEVICE_READ,
    DEVICE_WRITE,
    WORKFLOW_READ,
    WORKFLOW_WRITE,
    REQUEST_READ,
    REQUEST_WRITE,
    QUOTE_READ,
    QUOTE_WRITE,
    AUTHORIZATION_APPROVE,
    INVENTORY_READ,
    INVENTORY_WRITE,
    FINANCE_READ,
    FINANCE_WRITE,
    AUDIT_READ
}
