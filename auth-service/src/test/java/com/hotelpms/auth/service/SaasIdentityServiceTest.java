package com.hotelpms.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hotelpms.auth.domain.AccountToken;
import com.hotelpms.auth.domain.HotelRegistry;
import com.hotelpms.auth.domain.TenantBranch;
import com.hotelpms.auth.domain.TenantIndustryProfile;
import com.hotelpms.auth.domain.UserAccount;
import com.hotelpms.auth.domain.Role;
import com.hotelpms.auth.dto.InvitationRequest;
import com.hotelpms.auth.dto.SignupRequest;
import com.hotelpms.auth.exception.NotFoundException;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

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
  void fixiInvitationRejectsHistoricalPmsRolesBeforeCreatingInvitation() {
    final UUID tenantId = UUID.randomUUID();
    when(industryProfiles.findById(tenantId)).thenReturn(java.util.Optional.of(
        TenantIndustryProfile.builder().industryKey("ELECTRONICS_REPAIR").build()));

    assertThatThrownBy(() -> service.invite(
        tenantId, "owner", new InvitationRequest("employee@example.test", Role.KITCHEN, null)))
        .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
        .hasMessage("ROLE_NOT_ASSIGNABLE_FOR_FIXI");
    verify(invitations, never()).save(any());
    verify(mail, never()).invitation(any(), any());
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
    assertThat(platformUser.getRole()).isEqualTo(Role.GUEST);
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
    when(
            tokens.consumeIfAvailable(
                any(),
                org.mockito.ArgumentMatchers.eq(VERIFY_EMAIL_PURPOSE),
                any()))
        .thenReturn(1);
    when(users.findByIdIncludingInactive(platformUserId)).thenReturn(java.util.Optional.of(platformUser));
    when(users.activateAfterEmailVerification(platformUserId)).thenReturn(1);

    final AccountVerificationResult result = service.verify("opaque-verification-token");

    assertThat(result).isEqualTo(AccountVerificationResult.VERIFIED);
    verify(users).activateAfterEmailVerification(platformUserId);
    verify(users, never()).save(any(UserAccount.class));
    verify(hotels, never()).findById(isNull());
  }

  @Test
  void invalidVerificationTokenIsRejectedWithoutChangingTheAccount() {
    when(
            tokens.findByTokenHashAndPurpose(
                any(), org.mockito.ArgumentMatchers.eq(VERIFY_EMAIL_PURPOSE)))
        .thenReturn(java.util.Optional.empty());

    assertThatThrownBy(() -> service.verify("unknown-verification-token"))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("TOKEN_INVALID");
    verify(users, never()).save(any(UserAccount.class));
  }

  @Test
  void expiredVerificationTokenReturnsGoneWithoutActivatingTheAccount() {
    final UUID userId = UUID.randomUUID();
    final AccountToken expired =
        verificationToken(userId, LocalDateTime.now().minusMinutes(1), null);
    final UserAccount user = unverifiedUser(userId);
    when(
            tokens.findByTokenHashAndPurpose(
                any(), org.mockito.ArgumentMatchers.eq(VERIFY_EMAIL_PURPOSE)))
        .thenReturn(java.util.Optional.of(expired));
    when(users.findByIdIncludingInactive(userId)).thenReturn(java.util.Optional.of(user));

    assertThatThrownBy(() -> service.verify("expired-verification-token"))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode().value())
                    .isEqualTo(HttpStatus.GONE.value()));

    assertThat(user.isActive()).isFalse();
    assertThat(user.isEmailVerified()).isFalse();
    verify(tokens, never()).consumeIfAvailable(any(), any(), any());
    verify(users, never()).save(any(UserAccount.class));
  }

  @Test
  void consumedVerificationTokenForVerifiedAccountReturnsAlreadyVerified() {
    final UUID userId = UUID.randomUUID();
    final AccountToken consumed =
        verificationToken(userId, LocalDateTime.now().plusHours(1), LocalDateTime.now().minusMinutes(1));
    final UserAccount user =
        UserAccount.builder().id(userId).active(true).emailVerified(true).build();
    when(
            tokens.findByTokenHashAndPurpose(
                any(), org.mockito.ArgumentMatchers.eq(VERIFY_EMAIL_PURPOSE)))
        .thenReturn(java.util.Optional.of(consumed));
    when(users.findByIdIncludingInactive(userId)).thenReturn(java.util.Optional.of(user));

    assertThat(service.verify("already-used-verification-token"))
        .isEqualTo(AccountVerificationResult.ALREADY_VERIFIED);
    verify(tokens, never()).consumeIfAvailable(any(), any(), any());
    verify(users, never()).save(any(UserAccount.class));
  }

  @Test
  void consumedTokenForUnverifiedAccountRemainsInvalid() {
    final UUID userId = UUID.randomUUID();
    final AccountToken consumed =
        verificationToken(userId, LocalDateTime.now().plusHours(1), LocalDateTime.now().minusMinutes(1));
    final UserAccount user = unverifiedUser(userId);
    when(
            tokens.findByTokenHashAndPurpose(
                any(), org.mockito.ArgumentMatchers.eq(VERIFY_EMAIL_PURPOSE)))
        .thenReturn(java.util.Optional.of(consumed));
    when(users.findByIdIncludingInactive(userId)).thenReturn(java.util.Optional.of(user));

    assertThatThrownBy(() -> service.verify("used-verification-token"))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("TOKEN_INVALID");
    verify(users, never()).save(any(UserAccount.class));
  }

  @Test
  void concurrentSecondVerificationRecognizesTheCommittedVerification() {
    final UUID userId = UUID.randomUUID();
    final AccountToken available = verificationToken(userId, LocalDateTime.now().plusHours(1), null);
    final AccountToken consumed =
        verificationToken(userId, LocalDateTime.now().plusHours(1), LocalDateTime.now());
    when(
            tokens.findByTokenHashAndPurpose(
                any(), org.mockito.ArgumentMatchers.eq(VERIFY_EMAIL_PURPOSE)))
        .thenReturn(java.util.Optional.of(available), java.util.Optional.of(consumed));
    when(users.findByIdIncludingInactive(userId))
        .thenReturn(
            java.util.Optional.of(unverifiedUser(userId)),
            java.util.Optional.of(
                UserAccount.builder().id(userId).active(true).emailVerified(true).build()));
    when(
            tokens.consumeIfAvailable(
                any(),
                org.mockito.ArgumentMatchers.eq(VERIFY_EMAIL_PURPOSE),
                any()))
        .thenReturn(0);

    assertThat(service.verify("concurrent-verification-token"))
        .isEqualTo(AccountVerificationResult.ALREADY_VERIFIED);
    verify(users, never()).save(any(UserAccount.class));
  }

  private static AccountToken verificationToken(
      final UUID userId, final LocalDateTime expiresAt, final LocalDateTime consumedAt) {
    return AccountToken.builder()
        .userId(userId)
        .purpose(VERIFY_EMAIL_PURPOSE)
        .tokenHash("unit-test-token-digest")
        .expiresAt(expiresAt)
        .consumedAt(consumedAt)
        .createdAt(LocalDateTime.now())
        .build();
  }

  private static UserAccount unverifiedUser(final UUID userId) {
    return UserAccount.builder()
        .id(userId)
        .username("pending-user")
        .email("pending@example.com")
        .active(false)
        .emailVerified(false)
        .build();
  }
}
