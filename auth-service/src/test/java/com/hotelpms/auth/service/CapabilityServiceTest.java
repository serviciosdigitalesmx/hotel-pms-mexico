package com.hotelpms.auth.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.hotelpms.auth.domain.UserAccount;
import com.hotelpms.auth.dto.UpdateUserCapabilityGrantRequest;
import com.hotelpms.auth.repository.TenantCapabilityRepository;
import com.hotelpms.auth.repository.TenantBranchRepository;
import com.hotelpms.auth.repository.UserAccountRepository;
import com.hotelpms.auth.repository.UserCapabilityGrantRepository;
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
    private static final String TEST_USERNAME = "alice";

    @Mock private TenantCapabilityRepository capabilities;
    @Mock private UserAccountRepository users;
    @Mock private UserCapabilityGrantRepository userGrants;
    @Mock private TenantBranchRepository branches;
    @InjectMocks private CapabilityService service;

    @Test
    void deniesCapabilityEvaluationForAnotherTenant() {
        final UUID actual = UUID.randomUUID();
        final UUID requested = UUID.randomUUID();
        final UserAccount user = UserAccount.builder().hotelId(actual).build();
        when(users.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class,
                () -> service.isEnabled(TEST_USERNAME, requested, "inventory.read"));
    }

    @Test
    void missingCapabilityIsDisabledByDefault() {
        final UUID tenant = UUID.randomUUID();
        when(users.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(UserAccount.builder().hotelId(tenant).build()));
        when(capabilities.findByTenantIdAndCapabilityKey(tenant, "ai")).thenReturn(Optional.empty());

        assertFalse(service.isEnabled(TEST_USERNAME, tenant, "ai"));
    }

    @Test
    void grantUpdateRejectsUserFromAnotherTenant() {
        final UUID tenant = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        when(users.findByIdAndHotelId(userId, tenant)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> service.updateUserGrant(
                tenant, userId, new UpdateUserCapabilityGrantRequest("orders.read", true, null)));
    }

    @Test
    void grantUpdateRejectsBranchFromAnotherTenant() {
        final UUID tenant = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final UUID branchId = UUID.randomUUID();
        when(users.findByIdAndHotelId(userId, tenant))
                .thenReturn(Optional.of(UserAccount.builder().id(userId).hotelId(tenant).build()));
        when(branches.findByIdAndHotelId(branchId, tenant)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> service.updateUserGrant(
                tenant, userId, new UpdateUserCapabilityGrantRequest("orders.read", true, branchId)));
    }
}
