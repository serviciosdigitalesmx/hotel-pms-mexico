package com.hotelpms.auth.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.hotelpms.auth.domain.TenantCapability;
import com.hotelpms.auth.domain.UserAccount;
import com.hotelpms.auth.repository.TenantCapabilityRepository;
import com.hotelpms.auth.repository.UserAccountRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class CapabilityServiceTest {
    @Mock private TenantCapabilityRepository capabilities;
    @Mock private UserAccountRepository users;
    @InjectMocks private CapabilityService service;

    @Test
    void deniesCapabilityEvaluationForAnotherTenant() {
        final UUID actual = UUID.randomUUID();
        final UUID requested = UUID.randomUUID();
        final UserAccount user = UserAccount.builder().hotelId(actual).build();
        when(users.findByUsername("alice")).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class,
                () -> service.isEnabled("alice", requested, "inventory.read"));
    }

    @Test
    void missingCapabilityIsDisabledByDefault() {
        final UUID tenant = UUID.randomUUID();
        when(users.findByUsername("alice")).thenReturn(Optional.of(UserAccount.builder().hotelId(tenant).build()));
        when(capabilities.findByTenantIdAndCapabilityKey(tenant, "ai")).thenReturn(Optional.empty());

        assertFalse(service.isEnabled("alice", tenant, "ai"));
    }
}
