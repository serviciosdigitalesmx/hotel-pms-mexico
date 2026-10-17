package com.hotelpms.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
            "StrongPass1",
            "Reparación Ñandú",
            null,
            "Sucursal Centro");
    when(users.existsByEmailIncludingInactive(OWNER_EMAIL)).thenReturn(false);
    when(hotels.existsBySlug("reparacion-nandu")).thenReturn(false);
    when(passwords.encode("StrongPass1")).thenReturn("encoded-password");
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
    assertThat(tokenCaptor.getValue().getPurpose()).isEqualTo("VERIFY_EMAIL");
    verify(mail).verification(org.mockito.ArgumentMatchers.eq(OWNER_EMAIL), any());
  }
}
