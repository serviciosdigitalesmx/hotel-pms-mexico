package com.hotelpms.auth.service;

import com.hotelpms.auth.domain.TenantCapability;
import com.hotelpms.auth.repository.TenantCapabilityRepository;
import com.hotelpms.auth.repository.UserAccountRepository;
import com.hotelpms.internalauth.contracts.Capability;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Evaluates tenant capabilities while enforcing tenant ownership. */
@Service
@RequiredArgsConstructor
public class CapabilityService {
    private final TenantCapabilityRepository capabilities;
    private final UserAccountRepository users;

    /**
     * Checks a capability only after verifying the authenticated user's tenant.
     *
     * @param username authenticated username
     * @param requestedTenant tenant requested by the caller
     * @param key capability key
     * @return whether the capability is enabled
     * @throws AccessDeniedException if the user is missing or belongs to another tenant
     * @implSpec Overrides must preserve the tenant ownership check before capability lookup.
     */
    @Transactional(readOnly = true)
    public boolean isEnabled(final String username, final UUID requestedTenant, final String key) {
        final UUID actualTenant = users.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found")).getHotelId();
        if (!actualTenant.equals(requestedTenant)) {
            throw new AccessDeniedException("Tenant access denied");
        }
        return capabilities.findByTenantIdAndCapabilityKey(actualTenant, key)
                .map(TenantCapability::isEnabled).orElse(false);
    }

    /** Enforces a granular capability for a tenant-scoped operation. */
    @Transactional(readOnly = true)
    public void requireCapability(final String username, final UUID requestedTenant,
            final Capability capability) {
        if (!isEnabled(username, requestedTenant, capability.name())) {
            throw new AccessDeniedException("Capability access denied: " + capability.name());
        }
    }
}
