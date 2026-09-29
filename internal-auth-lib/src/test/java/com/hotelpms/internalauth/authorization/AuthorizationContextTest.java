package com.hotelpms.internalauth.authorization;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthorizationContextTest {
    private static final UUID TENANT = UUID.randomUUID();
    private static final UUID OTHER_TENANT = UUID.randomUUID();
    private static final UUID BRANCH = UUID.randomUUID();
    private static final UUID OTHER_BRANCH = UUID.randomUUID();

    @Test
    void tenantIsolationRejectsCrossTenantAccess() {
        final AuthorizationContext context = new AuthorizationContext(TENANT, BRANCH,
                Set.of(Permission.CUSTOMER_READ));

        assertThatThrownBy(() -> context.requireTenant(OTHER_TENANT))
                .isInstanceOf(AuthorizationDeniedException.class)
                .hasMessage("TENANT_SCOPE_MISMATCH");
    }

    @Test
    void branchIsolationRejectsUnselectedOrDifferentBranch() {
        final AuthorizationContext context = new AuthorizationContext(TENANT, BRANCH, Set.of());

        assertThatThrownBy(() -> context.requireBranch(OTHER_BRANCH))
                .isInstanceOf(AuthorizationDeniedException.class);
        assertThatThrownBy(() -> new AuthorizationContext(TENANT, null, Set.of())
                .requireBranch(BRANCH)).isInstanceOf(AuthorizationDeniedException.class);
    }

    @Test
    void permissionEnforcementIsDenyByDefaultAndImmutable() {
        final AuthorizationContext context = new AuthorizationContext(TENANT, BRANCH,
                Set.of(Permission.CUSTOMER_READ));

        assertThat(context.can(Permission.CUSTOMER_READ)).isTrue();
        assertThat(context.can(Permission.CUSTOMER_WRITE)).isFalse();
        assertThatThrownBy(() -> context.require(Permission.CUSTOMER_WRITE))
                .isInstanceOf(AuthorizationDeniedException.class);
        assertThat(context.permissions()).containsExactly(Permission.CUSTOMER_READ);
    }
}
