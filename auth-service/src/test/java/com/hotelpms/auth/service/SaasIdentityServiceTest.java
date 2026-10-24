package com.hotelpms.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hotelpms.auth.domain.AccountToken;
import com.hotelpms.auth.domain.HotelRegistry;
import com.hotelpms.auth.domain.TenantBranch;
import com.hotelpms.auth.domain.UserAccount;
import com.hotelpms.auth.dto.SignupRequest;
import com.hotelpms.auth.repository.AccountTokenRepository;
import com.hotelpms.auth.repository.HotelRegistryRepository;
import com.hotelpms.auth.repository.TenantBranchRepository;
import com.hotelpms.auth.repository.TenantCapabilityRepository;
import com.hotelpms.auth.repository.TenantIndustryProfileRepository;
import com.hotelpms.auth.repository.TenantInvitationRepository;
import com.hotelpms.auth.repository.UserAccountRepository;
import com.hotelpms.auth.repository.UserBranchMembershipRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class SaasIdentityServiceTest {

  private static final String OWNER_EMAIL = "owner@example.com";
  private static final String BOOTSTRAP_PASSWORD = "StrongPass1";
  private static final String VERIFY_EMAIL_PURPOSE = "VERIFY_EMAIL";
  private static final String PLATFORM_USERNAME = "platform-operator";
  private static final String PLATFORM_EMAIL = "operator@example.com";

  @Mock private HotelRegistryRepository hotels;
  @Mock private UserAccountRepository users;
  @Mock private TenantBranchRepository branches;
  @Mock private UserBranchMembershipRepository memberships;
  @Mock private TenantCapabilityRepository capabilities;
  @Mock private AccountTokenRepository tokens;
  @Mock private TenantInvitationRepository invitations;
  @Mock private PasswordEncoder passwords;
  @Mock private TenantIndustryProfileRepository industryProfiles;
  @Mock private AccountMailService mail;

  @InjectMocks private SaasIdentityService service;

  @Captor private ArgumentCaptor<UserAccount> ownerCaptor;
  @Captor private ArgumentCaptor<AccountToken> tokenCaptor;

  @Test
  void signupCreatesAnUnverifiedOwnerAndSendsOneTimeVerification() {
    final UUID ownerId = UUID.randomUUID();
    final SignupRequest request =
        new SignupRequest(
            "Fixi Owner",
            " OWNER@EXAMPLE.COM ",
            null,
            BOOTSTRAP_PASSWORD,
            "Reparación Ñandú",
            null,
            "Sucursal Centro");
    when(users.existsByEmailIncludingInactive(OWNER_EMAIL)).thenReturn(false);
    when(hotels.existsBySlug("reparacion-nandu")).thenReturn(false);
    when(passwords.encode(BOOTSTRAP_PASSWORD)).thenReturn("encoded-password");
    when(hotels.save(any(HotelRegistry.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(users.save(any(UserAccount.class)))
        .thenAnswer(
            invocation -> {
              final UserAccount saved = invocation.getArgument(0);
              saved.setId(ownerId);
              return saved;
            });
    when(branches.save(any(TenantBranch.class)))
        .thenAnswer(
            invocation -> {
              final TenantBranch saved = invocation.getArgument(0);
              saved.setId(UUID.randomUUID());
              return saved;
            });
    when(tokens.save(any(AccountToken.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    service.signup(request);

    verify(users).save(ownerCaptor.capture());
    assertThat(ownerCaptor.getValue().getEmail()).isEqualTo(OWNER_EMAIL);
    assertThat(ownerCaptor.getValue().isEmailVerified()).isFalse();
    assertThat(ownerCaptor.getValue().isActive()).isFalse();
    verify(tokens).save(tokenCaptor.capture());
    assertThat(tokenCaptor.getValue().getUserId()).isEqualTo(ownerId);
    assertThat(tokenCaptor.getValue().getPurpose()).isEqualTo(VERIFY_EMAIL_PURPOSE);
    verify(mail).verification(org.mockito.ArgumentMatchers.eq(OWNER_EMAIL), any());
  }

  @Test
  void platformBootstrapCreatesInactiveTenantlessIdentityAndSendsVerification() {
    final UUID platformUserId = UUID.randomUUID();
    when(users.existsByUsernameIncludingInactive(PLATFORM_USERNAME)).thenReturn(false);
    when(users.existsByEmailIncludingInactive(PLATFORM_EMAIL)).thenReturn(false);
    when(passwords.encode(BOOTSTRAP_PASSWORD)).thenReturn("encoded-password");
    when(users.save(any(UserAccount.class)))
        .thenAnswer(
            invocation -> {
              final UserAccount saved = invocation.getArgument(0);
              saved.setId(platformUserId);
              return saved;
            });
    when(tokens.save(any(AccountToken.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    service.createPlatformBootstrapIdentity(
        PLATFORM_USERNAME, " OPERATOR@EXAMPLE.COM ", "Platform Operator", BOOTSTRAP_PASSWORD);

    verify(users).save(ownerCaptor.capture());
    final UserAccount platformUser = ownerCaptor.getValue();
    assertThat(platformUser.getUsername()).isEqualTo(PLATFORM_USERNAME);
    assertThat(platformUser.getEmail()).isEqualTo(PLATFORM_EMAIL);
    assertThat(platformUser.getHotelId()).isNull();
    assertThat(platformUser.getRole()).isEqualTo(com.hotelpms.auth.domain.Role.GUEST);
    assertThat(platformUser.isActive()).isFalse();
    assertThat(platformUser.isEmailVerified()).isFalse();
    verify(tokens).save(tokenCaptor.capture());
    assertThat(tokenCaptor.getValue().getUserId()).isEqualTo(platformUserId);
    assertThat(tokenCaptor.getValue().getPurpose()).isEqualTo(VERIFY_EMAIL_PURPOSE);
    verify(mail).verification(org.mockito.ArgumentMatchers.eq(PLATFORM_EMAIL), any());
  }

  @Test
  void verificationActivatesTenantlessIdentityWithoutLookingUpATenant() {
    final UUID platformUserId = UUID.randomUUID();
    final AccountToken verification =
        AccountToken.builder()
            .userId(platformUserId)
            .purpose(VERIFY_EMAIL_PURPOSE)
            .expiresAt(LocalDateTime.now().plusHours(1))
            .createdAt(LocalDateTime.now())
            .build();
    final UserAccount platformUser =
        UserAccount.builder()
            .id(platformUserId)
            .username(PLATFORM_USERNAME)
            .hotelId(null)
            .active(false)
            .emailVerified(false)
            .build();
    when(
            tokens.findByTokenHashAndPurpose(
                any(), org.mockito.ArgumentMatchers.eq(VERIFY_EMAIL_PURPOSE)))
        .thenReturn(java.util.Optional.of(verification));
    when(tokens.save(any(AccountToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(users.findByIdIncludingInactive(platformUserId)).thenReturn(java.util.Optional.of(platformUser));

    service.verify("opaque-verification-token");

    assertThat(platformUser.isActive()).isTrue();
    assertThat(platformUser.isEmailVerified()).isTrue();
    verify(users).save(platformUser);
    verify(hotels, never()).findById(isNull());
  }
}
