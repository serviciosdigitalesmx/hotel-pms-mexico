package com.hotelpms.internalauth.authorization;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable, request-scoped authorization facts established by the backend.
 *
 * @param tenantId tenant identifier.
 * @param branchId selected branch identifier, when scoped.
 * @param permissions granted permissions.
 */
public record AuthorizationContext(UUID tenantId, UUID branchId, Set<Permission> permissions) {
    /** Normalizes and validates authorization facts. */
    public AuthorizationContext {
        Objects.requireNonNull(tenantId, "tenantId");
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }

    /**
     * Checks whether a permission is granted.
     *
     * @param permission permission to check.
     * @return whether the permission is granted.
     */
    public boolean can(final Permission permission) {
        return permission != null && permissions.contains(permission);
    }

    /**
     * Requires the requested tenant to match the authenticated tenant.
     *
     * @param requestedTenantId requested tenant identifier.
     */
    public void requireTenant(final UUID requestedTenantId) {
        if (!tenantId.equals(requestedTenantId)) {
            throw new AuthorizationDeniedException("TENANT_SCOPE_MISMATCH");
        }
    }

    /**
     * A selected branch narrows scope; it can never broaden tenant scope.
     *
     * @param requestedBranchId requested branch identifier.
     */
    public void requireBranch(final UUID requestedBranchId) {
        if (branchId == null || !branchId.equals(requestedBranchId)) {
            throw new AuthorizationDeniedException("BRANCH_SCOPE_MISMATCH");
        }
    }

    /**
     * Requires a granted permission.
     *
     * @param permission required permission.
     */
    public void require(final Permission permission) {
        if (!can(permission)) {
            throw new AuthorizationDeniedException("PERMISSION_DENIED");
        }
    }
}
