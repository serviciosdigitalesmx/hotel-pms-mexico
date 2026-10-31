package com.hotelpms.auth.service;

import com.hotelpms.auth.domain.TenantCapability;
import com.hotelpms.auth.domain.UserAccount;
import com.hotelpms.auth.domain.UserCapabilityGrant;
import com.hotelpms.auth.domain.TenantIndustryProfile;
import com.hotelpms.auth.dto.UpdateUserCapabilityGrantRequest;
import com.hotelpms.auth.dto.UserCapabilityGrantResponse;
import com.hotelpms.auth.exception.NotFoundException;
import com.hotelpms.auth.repository.TenantBranchRepository;
import com.hotelpms.auth.repository.TenantIndustryProfileRepository;
import com.hotelpms.auth.repository.UserBranchMembershipRepository;
import com.hotelpms.auth.repository.TenantCapabilityRepository;
import com.hotelpms.auth.repository.UserAccountRepository;
import com.hotelpms.auth.repository.UserCapabilityGrantRepository;
import com.hotelpms.internalauth.contracts.Capability;
import com.hotelpms.internalauth.contracts.FixiRoleCapabilities;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Evaluates tenant capabilities while enforcing tenant ownership. */
@Service
@RequiredArgsConstructor
public class CapabilityService {
  private static final String BRANCH_ACCESS_DENIED = "Branch access denied";
  private final TenantCapabilityRepository capabilities;
  private final UserAccountRepository users;
  private final UserCapabilityGrantRepository userGrants;
  private final TenantBranchRepository branches;
  private final TenantIndustryProfileRepository industryProfiles;
  private final UserBranchMembershipRepository branchMemberships;

  /**
   * Determines whether a tenant uses Fixi's workshop capability model.
   *
   * @param tenantId tenant identifier
   * @return whether the Fixi industry profile is configured
   */
  @Transactional(readOnly = true)
  public boolean isFixiTenant(final UUID tenantId) {
    return industryProfiles.findById(tenantId)
        .map(TenantIndustryProfile::getIndustryKey)
        .filter("ELECTRONICS_REPAIR"::equalsIgnoreCase)
        .isPresent();
  }

  /**
   * Determines whether the authenticated user is an OWNER/ADMIN of this exact tenant.
   *
   * @param username authenticated username
   * @param tenantId tenant identifier
   * @return whether the user administers this tenant
   */
  @Transactional(readOnly = true)
  public boolean isTenantAdministrator(final String username, final UUID tenantId) {
    return users.findByUsername(username)
        .filter(user -> tenantId.equals(user.getHotelId()))
        .map(user -> "OWNER".equals(user.getRole().name()) || "ADMIN".equals(user.getRole().name()))
        .orElse(false);
  }

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
    return isEnabled(username, requestedTenant, null, key);
  }

  /**
   * Evaluates a capability using an optional server-validated branch scope.
   *
   * @param username authenticated username
   * @param requestedTenant tenant identifier
   * @param branchId optional branch identifier
   * @param key capability key
   * @return whether the capability is enabled
   */
  @Transactional(readOnly = true)
  public boolean isEnabled(
      final String username, final UUID requestedTenant, final UUID branchId, final String key) {
    final UserAccount user =
        users
            .findByUsername(username)
            .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    final UUID actualTenant = user.getHotelId();
    if (actualTenant == null || !actualTenant.equals(requestedTenant)) {
      throw new AccessDeniedException("Tenant access denied");
    }
    if (branchId != null) {
      final var branch =
          branches
              .findByIdAndHotelId(branchId, actualTenant)
              .orElseThrow(() -> new NotFoundException("Branch not found"));
      if (!branch.isActive()) {
        throw new AccessDeniedException(BRANCH_ACCESS_DENIED);
      }
      if (!"OWNER".equals(user.getRole().name())
          && !"ADMIN".equals(user.getRole().name())
          && !branchMemberships.existsByUserIdAndBranchIdAndHotelId(
              user.getId(), branchId, actualTenant)) {
        throw new AccessDeniedException(BRANCH_ACCESS_DENIED);
      }
    }
    final UUID userId = user.getId();
    final Capability capability = Capability.fromCode(key).orElse(null);
    final boolean fixiTenant = isFixiTenant(actualTenant);
    if (fixiTenant
        && (capability == null
            || !FixiRoleCapabilities.isWorkshopRole(user.getRole().name())
            || !FixiRoleCapabilities.isFixiCapability(capability))) {
      return false;
    }
    // Tenant capability rows are the persisted module/entitlement gate. An individual
    // override can specialize a role, but may never bypass a disabled tenant capability.
    final boolean tenantEnabled =
        capabilities
            .findByTenantIdAndCapabilityKey(actualTenant, key)
            .map(TenantCapability::isEnabled)
            .orElse(false);
    if (!tenantEnabled) {
      return false;
    }
    final var grant =
        branchId == null
            ? userGrants.findByTenantIdAndUserIdAndBranchIdIsNullAndCapabilityKey(
                actualTenant, userId, key)
            : userGrants.findByTenantIdAndUserIdAndBranchIdAndCapabilityKey(
                actualTenant, userId, branchId, key);
    if (grant.isPresent()) {
      return grant.get().isEnabled();
    }
    return !fixiTenant
        || FixiRoleCapabilities.forRole(user.getRole().name()).contains(capability);
  }

  /**
   * Enforces a granular capability for a tenant-scoped operation.
   *
   * @param username authenticated username
   * @param requestedTenant tenant requested by the caller
   * @param capability capability to enforce
   * @throws AccessDeniedException if the capability is unavailable
   */
  @Transactional(readOnly = true)
  public void requireCapability(
      final String username, final UUID requestedTenant, final Capability capability) {
    if (!isEnabled(username, requestedTenant, capability.name())) {
      throw new AccessDeniedException("Capability access denied: " + capability.name());
    }
  }

  /**
   * Enforces a capability within a server-validated branch scope.
   *
   * @param username authenticated username
   * @param requestedTenant tenant identifier
   * @param branchId branch identifier
   * @param capability capability to enforce
   * @throws AccessDeniedException if the capability is unavailable
   */
  public void requireCapability(
      final String username,
      final UUID requestedTenant,
      final UUID branchId,
      final Capability capability) {
    if (!isEnabled(username, requestedTenant, branchId, capability.name())) {
      throw new AccessDeniedException("Capability access denied: " + capability.name());
    }
  }

  /**
   * Updates an explicit capability override for a user within the tenant.
   *
   * @param tenantId tenant identifier
   * @param userId user identifier
   * @param request requested capability and grant scope
   * @return saved capability grant
   */
  @Transactional
  public UserCapabilityGrantResponse updateUserGrant(
      final UUID tenantId, final UUID userId, final UpdateUserCapabilityGrantRequest request) {
    final UserAccount user =
        users
            .findByIdAndHotelId(userId, tenantId)
            .orElseThrow(() -> new AccessDeniedException("USER_TENANT_MISMATCH"));
    final String key =
        Capability.fromCode(request.capabilityKey())
            .orElseThrow(() -> new IllegalArgumentException("UNKNOWN_CAPABILITY"))
            .name();
    if (industryProfiles.findById(tenantId)
        .map(TenantIndustryProfile::getIndustryKey)
        .filter("ELECTRONICS_REPAIR"::equalsIgnoreCase)
        .isPresent()
        && (!FixiRoleCapabilities.isWorkshopRole(user.getRole().name())
            || !FixiRoleCapabilities.isFixiCapability(Capability.valueOf(key)))) {
      throw new AccessDeniedException("CAPABILITY_NOT_AVAILABLE_FOR_FIXI");
    }
    if (request.branchId() != null
        && branches.findByIdAndHotelId(request.branchId(), tenantId).isEmpty()) {
      throw new AccessDeniedException("BRANCH_TENANT_MISMATCH");
    }
    final UserCapabilityGrant grant =
        request.branchId() == null
            ? userGrants
                .findByTenantIdAndUserIdAndBranchIdIsNullAndCapabilityKey(
                    tenantId, user.getId(), key)
                .orElseGet(UserCapabilityGrant::new)
            : userGrants
                .findByTenantIdAndUserIdAndBranchIdAndCapabilityKey(
                    tenantId, user.getId(), request.branchId(), key)
                .orElseGet(UserCapabilityGrant::new);
    grant.setTenantId(tenantId);
    grant.setUserId(user.getId());
    grant.setBranchId(request.branchId());
    grant.setCapabilityKey(key);
    grant.setEnabled(request.enabled());
    grant.setUpdatedAt(LocalDateTime.now());
    return UserCapabilityGrantResponse.from(userGrants.save(grant));
  }

  /**
   * Lists explicit grants and revocations for one tenant user.
   *
   * @param tenantId tenant identifier
   * @param userId user identifier
   * @return saved tenant and branch overrides
   */
  @Transactional(readOnly = true)
  public List<UserCapabilityGrantResponse> listUserGrants(final UUID tenantId, final UUID userId) {
    users
        .findByIdAndHotelId(userId, tenantId)
        .orElseThrow(() -> new AccessDeniedException("USER_TENANT_MISMATCH"));
    return userGrants.findAllByTenantIdAndUserIdOrderByCapabilityKeyAsc(tenantId, userId).stream()
        .map(UserCapabilityGrantResponse::from)
        .toList();
  }

  /**
   * Returns the effective tenant capabilities for the authenticated user.
   *
   * @param username authenticated username
   * @param tenantId tenant identifier
   * @return enabled capability keys
   */
  @Transactional(readOnly = true)
  public List<String> effectiveCapabilities(final String username, final UUID tenantId) {
    return effectiveCapabilities(username, tenantId, null);
  }

  /**
   * Returns effective capabilities for a tenant or a validated branch scope.
   *
   * @param username authenticated username
   * @param tenantId tenant identifier
   * @param branchId optional branch scope
   * @return enabled capability keys
   */
  @Transactional(readOnly = true)
  public List<String> effectiveCapabilities(
      final String username, final UUID tenantId, final UUID branchId) {
    return java.util.Arrays.stream(Capability.values())
        .map(Capability::name)
        .filter(key -> isEnabled(username, tenantId, branchId, key))
        .toList();
  }
}
