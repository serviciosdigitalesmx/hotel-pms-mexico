package com.hotelpms.auth.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.hotelpms.auth.domain.UserAccount;
import com.hotelpms.auth.domain.Role;
import com.hotelpms.auth.domain.TenantBranch;
import com.hotelpms.auth.domain.TenantCapability;
import com.hotelpms.auth.domain.TenantIndustryProfile;
import com.hotelpms.auth.dto.UpdateUserCapabilityGrantRequest;
import com.hotelpms.auth.repository.TenantCapabilityRepository;
import com.hotelpms.auth.repository.TenantBranchRepository;
import com.hotelpms.auth.repository.TenantIndustryProfileRepository;
import com.hotelpms.auth.repository.UserBranchMembershipRepository;
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
    private static final String ORDERS_READ = "ORDERS_READ";
    private static final String FINANCE_READ = "FINANCE_READ";

    @Mock private TenantCapabilityRepository capabilities;
    @Mock private UserAccountRepository users;
    @Mock private UserCapabilityGrantRepository userGrants;
    @Mock private TenantBranchRepository branches;
    @Mock private TenantIndustryProfileRepository industryProfiles;
    @Mock private UserBranchMembershipRepository branchMemberships;
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

    @Test
    void fixiRoleTemplateDoesNotTurnAnEnabledModuleIntoPermissionForEveryRole() {
        final UUID tenant = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final UserAccount technician = UserAccount.builder()
                .id(userId).username(TEST_USERNAME).hotelId(tenant).role(Role.TECHNICIAN).build();
        when(users.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(technician));
        when(industryProfiles.findById(tenant)).thenReturn(Optional.of(
                TenantIndustryProfile.builder().industryKey("ELECTRONICS_REPAIR").build()));
        when(capabilities.findByTenantIdAndCapabilityKey(tenant, ORDERS_READ))
                .thenReturn(Optional.of(enabled(tenant, ORDERS_READ)));
        when(capabilities.findByTenantIdAndCapabilityKey(tenant, FINANCE_READ))
                .thenReturn(Optional.of(enabled(tenant, FINANCE_READ)));

        org.junit.jupiter.api.Assertions.assertTrue(
                service.isEnabled(TEST_USERNAME, tenant, ORDERS_READ));
        assertFalse(service.isEnabled(TEST_USERNAME, tenant, FINANCE_READ));
    }

    @Test
    void explicitOverrideCannotReenableCapabilityDisabledForTenant() {
        final UUID tenant = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final UserAccount technician = UserAccount.builder()
                .id(userId).username(TEST_USERNAME).hotelId(tenant).role(Role.TECHNICIAN).build();
        final var grant = com.hotelpms.auth.domain.UserCapabilityGrant.builder()
                .enabled(true).build();
        when(users.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(technician));
        when(industryProfiles.findById(tenant)).thenReturn(Optional.of(
                TenantIndustryProfile.builder().industryKey("ELECTRONICS_REPAIR").build()));
        when(capabilities.findByTenantIdAndCapabilityKey(tenant, ORDERS_READ))
                .thenReturn(Optional.of(new TenantCapability()));
        when(userGrants.findByTenantIdAndUserIdAndBranchIdIsNullAndCapabilityKey(
                tenant, userId, ORDERS_READ)).thenReturn(Optional.of(grant));

        assertFalse(service.isEnabled(TEST_USERNAME, tenant, ORDERS_READ));
    }

    @Test
    void branchCapabilitiesRequireMembershipForNonAdministrators() {
        final UUID tenant = UUID.randomUUID();
        final UUID branchId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final UserAccount technician = UserAccount.builder()
                .id(userId).username(TEST_USERNAME).hotelId(tenant).role(Role.TECHNICIAN).build();
        when(users.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(technician));
        when(branches.findByIdAndHotelId(branchId, tenant)).thenReturn(Optional.of(
                TenantBranch.builder().active(true).build()));
        when(branchMemberships.existsByUserIdAndBranchIdAndHotelId(userId, branchId, tenant))
                .thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> service.isEnabled(TEST_USERNAME, tenant, branchId, ORDERS_READ));
    }

    private TenantCapability enabled(final UUID tenant, final String key) {
        final TenantCapability capability = new TenantCapability();
        capability.setTenantId(tenant);
        capability.setCapabilityKey(key);
        capability.setEnabled(true);
        return capability;
    }
}
