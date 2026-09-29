package com.hotelpms.internalauth.authorization;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Immutable, request-scoped authorization facts established by the backend. */
public record AuthorizationContext(UUID tenantId, UUID branchId, Set<Permission> permissions) {
    public AuthorizationContext {
        Objects.requireNonNull(tenantId, "tenantId");
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }

    public boolean can(final Permission permission) {
        return permission != null && permissions.contains(permission);
    }

    public void requireTenant(final UUID requestedTenantId) {
        if (!tenantId.equals(requestedTenantId)) {
            throw new AuthorizationDeniedException("TENANT_SCOPE_MISMATCH");
        }
    }

    /** A selected branch narrows scope; it can never broaden tenant scope. */
    public void requireBranch(final UUID requestedBranchId) {
        if (branchId == null || !branchId.equals(requestedBranchId)) {
            throw new AuthorizationDeniedException("BRANCH_SCOPE_MISMATCH");
        }
    }

    public void require(final Permission permission) {
        if (!can(permission)) {
            throw new AuthorizationDeniedException("PERMISSION_DENIED");
        }
    }
}
