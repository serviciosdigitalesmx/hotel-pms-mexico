package com.hotelpms.auth.service;

import com.hotelpms.auth.domain.AccountToken;
import com.hotelpms.auth.domain.HotelRegistry;
import com.hotelpms.auth.domain.Role;
import com.hotelpms.auth.domain.TenantBranch;
import com.hotelpms.auth.domain.TenantCapability;
import com.hotelpms.auth.domain.TenantIndustryProfile;
import com.hotelpms.auth.domain.TenantInvitation;
import com.hotelpms.auth.domain.UserAccount;
import com.hotelpms.auth.domain.UserBranchMembership;
import com.hotelpms.auth.dto.AcceptInvitationRequest;
import com.hotelpms.auth.dto.AccountProfileResponse;
import com.hotelpms.auth.dto.InvitationRequest;
import com.hotelpms.auth.dto.OnboardingRequest;
import com.hotelpms.auth.dto.PublicResetPasswordRequest;
import com.hotelpms.auth.dto.SignupRequest;
import com.hotelpms.auth.dto.UpdateAccountProfileRequest;
import com.hotelpms.auth.exception.DuplicateResourceException;
import com.hotelpms.auth.exception.NotFoundException;
import com.hotelpms.auth.repository.AccountTokenRepository;
import com.hotelpms.auth.repository.HotelRegistryRepository;
import com.hotelpms.auth.repository.TenantBranchRepository;
import com.hotelpms.auth.repository.TenantCapabilityRepository;
import com.hotelpms.auth.repository.TenantIndustryProfileRepository;
import com.hotelpms.auth.repository.TenantInvitationRepository;
import com.hotelpms.auth.repository.UserAccountRepository;
import com.hotelpms.auth.repository.UserBranchMembershipRepository;
import com.hotelpms.internalauth.contracts.Capability;
import com.hotelpms.internalauth.contracts.FixiRoleCapabilities;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

/** Handles self-service tenant identity, onboarding, and invitation workflows. */
@Service
@RequiredArgsConstructor
public class SaasIdentityService {
  private static final String VERIFY = "VERIFY_EMAIL";
  private static final String RESET = "RESET_PASSWORD";
  private static final String TOKEN_INVALID = "TOKEN_INVALID";
  private static final int MAX_PLATFORM_USERNAME_LENGTH = 50;
  private static final int MAX_PLATFORM_EMAIL_LENGTH = 100;
  private static final int MAX_PLATFORM_FULL_NAME_LENGTH = 160;
  private static final int MIN_PLATFORM_PASSWORD_LENGTH = 6;
  private static final int TOKEN_EXPIRY_HOURS = 24;
  private static final int MAX_SLUG_LENGTH = 80;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final HotelRegistryRepository hotels;
  private final UserAccountRepository users;
  private final TenantBranchRepository branches;
  private final UserBranchMembershipRepository memberships;
  private final TenantCapabilityRepository capabilities;
  private final AccountTokenRepository tokens;
  private final TenantInvitationRepository invitations;
  private final PasswordEncoder passwords;
  private final TenantIndustryProfileRepository industryProfiles;
  private final AccountMailService mail;

  /**
   * Updates only the authenticated account's profile and verifies a changed contact email.
   *
   * @param userId authenticated account identifier
   * @param request validated display name and contact email
   * @return updated safe account profile
   */
  @Transactional
  public AccountProfileResponse updateAccountProfile(
      final UUID userId, final UpdateAccountProfileRequest request) {
    final UserAccount user = users.findById(userId)
        .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));
    final String email = request.email().trim().toLowerCase(Locale.ROOT);
    final boolean emailChanged = !email.equalsIgnoreCase(user.getEmail());
    if (emailChanged && users.existsByEmailIncludingInactiveAndIdNot(email, userId)) {
      throw new DuplicateResourceException("EMAIL_ALREADY_EXISTS");
    }
    user.setFullName(request.fullName().trim());
    if (emailChanged) {
      user.setEmail(email);
      user.setEmailVerified(false);
    }
    final UserAccount saved = users.save(user);
    if (emailChanged) {
      mail.verification(saved.getEmail(), issue(saved.getId(), VERIFY, TOKEN_EXPIRY_HOURS));
    }
    return AccountProfileResponse.from(saved);
  }

  /**
   * Creates a tenant with its owner, initial branch, capabilities, and industry defaults.
   *
   * @param request validated signup details
   */
  @Transactional
  public void signup(final SignupRequest request) {
    final String email = request.email().trim().toLowerCase(Locale.ROOT);
    final String businessName = request.businessName().trim();
    final String slug = uniqueSlug(businessName);
    if (users.existsByEmailIncludingInactive(email)) {
      throw new DuplicateResourceException("EMAIL_ALREADY_EXISTS");
    }

    final HotelRegistry tenant = new HotelRegistry();
    tenant.setId(UUID.randomUUID());
    tenant.setName(businessName);
    tenant.setSlug(slug);
    tenant.setStatus("ONBOARDING");
    tenant.setCreatedAt(LocalDateTime.now());
    hotels.save(tenant);

    final UserAccount owner =
        users.save(
            UserAccount.builder()
                .username(email)
                .email(email)
                .fullName(request.ownerName().trim())
                .passwordHash(passwords.encode(request.password()))
                .role(Role.OWNER)
                .hotelId(tenant.getId())
                .active(false)
                .emailVerified(false)
                .mustChangePassword(false)
                .build());
    final TenantBranch branch =
        branches.save(
            TenantBranch.builder()
                .hotelId(tenant.getId())
                .name(request.branchName().trim())
                .active(true)
                .build());
    memberships.save(
        UserBranchMembership.builder()
            .userId(owner.getId())
            .hotelId(tenant.getId())
            .branchId(branch.getId())
            .build());

    final LocalDateTime now = LocalDateTime.now();
    capabilities.saveAll(
        Arrays.stream(Capability.values())
            .map(
                capability -> {
                  final TenantCapability tenantCapability = new TenantCapability();
                  tenantCapability.setTenantId(tenant.getId());
                  tenantCapability.setCapabilityKey(capability.name());
                  tenantCapability.setEnabled(true);
                  tenantCapability.setUpdatedAt(now);
                  return tenantCapability;
                })
            .toList());
    industryProfiles.save(
        TenantIndustryProfile.builder()
            .tenantId(tenant.getId())
            .industryKey("ELECTRONICS_REPAIR")
            .enabledModulesJson(
                "[\"customers\",\"devices\",\"serviceOrders\",\"inventory\",\"whatsapp\",\"ai\"]")
            .dynamicFieldsJson("{}")
            .formsLabelsJson("{\"customer\":\"Cliente\",\"device\":\"Equipo\"}")
            .catalogsTemplatesJson("{}")
            .updatedAt(now)
            .build());
    mail.verification(owner.getEmail(), issue(owner.getId(), VERIFY, TOKEN_EXPIRY_HOURS));
  }

  /**
   * Creates the isolated, tenantless identity used only by the operator-enabled platform bootstrap.
   * The account remains inactive until the normal one-time email verification flow is completed.
   *
   * @param username globally unique platform login
   * @param email verified contact address
   * @param fullName operator display name
   * @param password operator-provided initial password
   */
  @Transactional
  public void createPlatformBootstrapIdentity(
      final String username,
      final String email,
      final String fullName,
      final String password) {
    final String normalizedUsername = username.trim();
    final String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
    if (!normalizedEmail.contains("@")
        || normalizedUsername.length() > MAX_PLATFORM_USERNAME_LENGTH
        || normalizedEmail.length() > MAX_PLATFORM_EMAIL_LENGTH
        || !StringUtils.hasText(fullName)
        || fullName.trim().length() > MAX_PLATFORM_FULL_NAME_LENGTH
        || password.length() < MIN_PLATFORM_PASSWORD_LENGTH
        || password.chars().noneMatch(Character::isUpperCase)) {
      throw new IllegalArgumentException("Platform bootstrap identity configuration is invalid.");
    }
    if (users.existsByUsernameIncludingInactive(normalizedUsername)
        || users.existsByEmailIncludingInactive(normalizedEmail)) {
      throw new DuplicateResourceException("PLATFORM_BOOTSTRAP_IDENTITY_ALREADY_EXISTS");
    }
    final UserAccount account =
        users.save(
            UserAccount.builder()
                .username(normalizedUsername)
                .email(normalizedEmail)
                .fullName(fullName.trim())
                .passwordHash(passwords.encode(password))
                .role(Role.GUEST)
                .hotelId(null)
                .active(false)
                .emailVerified(false)
                .mustChangePassword(false)
                .build());
    mail.verification(account.getEmail(), issue(account.getId(), VERIFY, TOKEN_EXPIRY_HOURS));
  }

  /**
   * Verifies a signup token and activates its owner account.
   *
   * @param rawToken single-use verification token
   * @return whether this request verified the account or found it already verified
   */
  @Transactional
  public AccountVerificationResult verify(final String rawToken) {
    final String tokenHash = hash(rawToken);
    final AccountToken token =
        tokens
            .findByTokenHashAndPurpose(tokenHash, VERIFY)
            .orElseThrow(() -> new NotFoundException(TOKEN_INVALID));
    final UserAccount user =
        users
            .findByIdIncludingInactive(token.getUserId())
            .orElseThrow(() -> new NotFoundException(TOKEN_INVALID));
    final LocalDateTime now = LocalDateTime.now();

    if (token.getConsumedAt() != null) {
      if (user.isEmailVerified()) {
        return AccountVerificationResult.ALREADY_VERIFIED;
      }
      throw new NotFoundException(TOKEN_INVALID);
    }
    if (!token.getExpiresAt().isAfter(now)) {
      if (user.isEmailVerified()) {
        return AccountVerificationResult.ALREADY_VERIFIED;
      }
      throw new ResponseStatusException(HttpStatus.GONE, "VERIFICATION_TOKEN_EXPIRED");
    }
    if (tokens.consumeIfAvailable(tokenHash, VERIFY, now) != 1) {
      final AccountToken current =
          tokens
              .findByTokenHashAndPurpose(tokenHash, VERIFY)
              .orElseThrow(() -> new NotFoundException(TOKEN_INVALID));
      final UserAccount refreshedUser =
          users
              .findByIdIncludingInactive(token.getUserId())
              .orElseThrow(() -> new NotFoundException(TOKEN_INVALID));
      if (current.getConsumedAt() != null && refreshedUser.isEmailVerified()) {
        return AccountVerificationResult.ALREADY_VERIFIED;
      }
      if (!current.getExpiresAt().isAfter(LocalDateTime.now())) {
        throw new ResponseStatusException(HttpStatus.GONE, "VERIFICATION_TOKEN_EXPIRED");
      }
      throw new NotFoundException(TOKEN_INVALID);
    }
    if (user.isEmailVerified()) {
      return AccountVerificationResult.ALREADY_VERIFIED;
    }
    if (users.activateAfterEmailVerification(user.getId()) != 1) {
      throw new NotFoundException(TOKEN_INVALID);
    }
    if (user.getHotelId() != null) {
      final HotelRegistry tenant = hotels.findById(user.getHotelId()).orElseThrow();
      tenant.setStatus("ONBOARDING");
      hotels.save(tenant);
    }
    return AccountVerificationResult.VERIFIED;
  }

  /**
   * Sends a password reset message only when the supplied address belongs to an account.
   *
   * @param email account email address
   */
  @Transactional
  public void forgot(final String email) {
    users
        .findByEmailIgnoreCase(email.trim())
        .ifPresent(user -> mail.reset(user.getEmail(), issue(user.getId(), RESET, 1)));
  }

  /**
   * Replaces an account password using a valid, single-use reset token.
   *
   * @param request validated reset token and replacement password
   */
  @Transactional
  public void reset(final PublicResetPasswordRequest request) {
    final AccountToken token = consume(request.token(), RESET);
    final UserAccount user =
        users
            .findByIdIncludingInactive(token.getUserId())
            .orElseThrow(() -> new NotFoundException(TOKEN_INVALID));
    user.setPasswordHash(passwords.encode(request.password()));
    user.setTokenVersion(user.getTokenVersion() + 1);
    user.setFailedAttempts(0);
    user.setLockedUntil(null);
    users.save(user);
  }

  /**
   * Completes tenant contact setup and activates the tenant.
   *
   * @param tenantId tenant to activate
   * @param request validated onboarding details
   */
  @Transactional
  public void completeOnboarding(final UUID tenantId, final OnboardingRequest request) {
    final HotelRegistry tenant =
        hotels.findById(tenantId).orElseThrow(() -> new NotFoundException("TENANT_NOT_FOUND"));
    tenant.setContactPhone(request.contactPhone());
    tenant.setBusinessHours(request.businessHours());
    tenant.setOnboardingCompleted(true);
    tenant.setStatus("ACTIVE");
    hotels.save(tenant);
  }

  /**
   * Creates a tenant-scoped invitation for a non-privileged role.
   *
   * @param tenantId tenant receiving the invitation
   * @param username authenticated actor
   * @param request invitation role, email, and optional branch
   */
  @Transactional
  public void invite(final UUID tenantId, final String username, final InvitationRequest request) {
    if (request.role() == Role.OWNER || request.role() == Role.ADMIN) {
      throw new AccessDeniedException("PRIVILEGED_ROLE_NOT_INVITABLE");
    }
    requireFixiEmployeeRole(tenantId, request.role());
    final UserAccount actor =
        users
            .findByUsername(username)
            .filter(user -> tenantId.equals(user.getHotelId()))
            .orElseThrow(() -> new AccessDeniedException("TENANT_MISMATCH"));
    if (request.branchId() != null) {
      branches
          .findByIdAndHotelId(request.branchId(), tenantId)
          .orElseThrow(() -> new NotFoundException("BRANCH_NOT_FOUND"));
    }
    final String rawToken = random();
    final LocalDateTime now = LocalDateTime.now();
    invitations.save(
        TenantInvitation.builder()
            .hotelId(tenantId)
            .email(request.email().trim().toLowerCase(Locale.ROOT))
            .role(request.role())
            .branchId(request.branchId())
            .tokenHash(hash(rawToken))
            .expiresAt(now.plusDays(3))
            .createdBy(actor.getId())
            .createdAt(now)
            .build());
    mail.invitation(request.email(), rawToken);
  }

  /**
   * Accepts a valid invitation and assigns the invited user to its tenant and branch.
   *
   * @param request invitation token and new account details
   */
  @Transactional
  public void accept(final AcceptInvitationRequest request) {
    final LocalDateTime now = LocalDateTime.now();
    final TenantInvitation invitation =
        invitations
            .findByTokenHash(hash(request.token()))
            .filter(item -> item.getAcceptedAt() == null && item.getExpiresAt().isAfter(now))
            .orElseThrow(() -> new NotFoundException("INVITATION_INVALID"));
    requireFixiEmployeeRole(invitation.getHotelId(), invitation.getRole());
    if (users.existsByEmailIncludingInactive(invitation.getEmail())
        || users.existsByUsernameIncludingInactive(request.username())) {
      throw new DuplicateResourceException("ACCOUNT_ALREADY_EXISTS");
    }
    final UserAccount user =
        users.save(
            UserAccount.builder()
                .username(request.username().trim())
                .email(invitation.getEmail())
                .fullName(request.fullName().trim())
                .passwordHash(passwords.encode(request.password()))
                .role(invitation.getRole())
                .hotelId(invitation.getHotelId())
                .active(true)
                .emailVerified(true)
                .mustChangePassword(false)
                .build());
    if (invitation.getBranchId() != null) {
      memberships.save(
          UserBranchMembership.builder()
              .userId(user.getId())
              .hotelId(invitation.getHotelId())
              .branchId(invitation.getBranchId())
              .build());
    }
    invitation.setAcceptedAt(now);
    invitations.save(invitation);
  }

  private void requireFixiEmployeeRole(final UUID tenantId, final Role role) {
    final boolean fixiTenant = industryProfiles.findById(tenantId)
        .map(TenantIndustryProfile::getIndustryKey)
        .filter("ELECTRONICS_REPAIR"::equalsIgnoreCase)
        .isPresent();
    if (fixiTenant && !FixiRoleCapabilities.isEmployeeRole(role.name())) {
      throw new AccessDeniedException("ROLE_NOT_ASSIGNABLE_FOR_FIXI");
    }
  }

  /**
   * Resends email verification when an unverified account exists.
   *
   * @param email account email address
   */
  @Transactional
  public void resendVerification(final String email) {
    users
        .findByEmailIncludingInactive(email.trim())
        .filter(user -> !user.isEmailVerified())
        .ifPresent(
            user ->
                mail.verification(
                    user.getEmail(), issue(user.getId(), VERIFY, TOKEN_EXPIRY_HOURS)));
  }

  private String issue(final UUID userId, final String purpose, final int hours) {
    final String rawToken = random();
    final LocalDateTime now = LocalDateTime.now();
    tokens.save(
        AccountToken.builder()
            .userId(userId)
            .purpose(purpose)
            .tokenHash(hash(rawToken))
            .expiresAt(now.plusHours(hours))
            .createdAt(now)
            .build());
    return rawToken;
  }

  private AccountToken consume(final String rawToken, final String purpose) {
    final LocalDateTime now = LocalDateTime.now();
    final AccountToken token =
        tokens
            .findByTokenHashAndPurpose(hash(rawToken), purpose)
            .filter(item -> item.getConsumedAt() == null && item.getExpiresAt().isAfter(now))
            .orElseThrow(() -> new NotFoundException(TOKEN_INVALID));
    token.setConsumedAt(now);
    return tokens.save(token);
  }

  private static String random() {
    final byte[] bytes = new byte[32];
    SECURE_RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private static String hash(final String raw) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8)));
    } catch (final NoSuchAlgorithmException exception) {
      throw new IllegalStateException(exception);
    }
  }

  private String uniqueSlug(final String businessName) {
    String base =
        Normalizer.normalize(businessName, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-+|-+$", "");
    if (base.isBlank()) {
      base = "negocio";
    }
    base = base.substring(0, Math.min(MAX_SLUG_LENGTH, base.length())).replaceAll("-+$", "");
    String candidate = base;
    int suffix = 2;
    while (hotels.existsBySlug(candidate)) {
      final String suffixText = "-" + suffix;
      suffix++;
      final int maxLength = MAX_SLUG_LENGTH - suffixText.length();
      candidate =
          base.substring(0, Math.min(base.length(), maxLength)).replaceAll("-+$", "") + suffixText;
    }
    return candidate;
  }
}
