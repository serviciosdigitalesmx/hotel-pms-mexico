package com.hotelpms.auth.service;

import com.hotelpms.auth.domain.TenantCapability;
import com.hotelpms.auth.repository.TenantCapabilityRepository;
import com.hotelpms.auth.repository.UserAccountRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CapabilityService {
    private final TenantCapabilityRepository capabilities;
    private final UserAccountRepository users;

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
}
