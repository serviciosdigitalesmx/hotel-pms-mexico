package com.hotelpms.auth.service;

import com.hotelpms.auth.domain.TenantCapability;
import com.hotelpms.auth.domain.UserAccount;
import com.hotelpms.auth.domain.UserCapabilityGrant;
import com.hotelpms.auth.dto.UpdateUserCapabilityGrantRequest;
import com.hotelpms.auth.dto.UserCapabilityGrantResponse;
import com.hotelpms.auth.repository.TenantBranchRepository;
import com.hotelpms.auth.repository.TenantCapabilityRepository;
import com.hotelpms.auth.repository.UserAccountRepository;
import com.hotelpms.auth.repository.UserCapabilityGrantRepository;
import com.hotelpms.internalauth.contracts.Capability;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
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
  private final UserCapabilityGrantRepository userGrants;
  private final TenantBranchRepository branches;

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
    final UUID actualTenant =
        users
            .findByUsername(username)
            .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"))
            .getHotelId();
    if (!actualTenant.equals(requestedTenant)) {
      throw new AccessDeniedException("Tenant access denied");
    }
    final UUID userId = users.findByUsername(username).orElseThrow().getId();
    final var grant =
        branchId == null
            ? userGrants.findByTenantIdAndUserIdAndBranchIdIsNullAndCapabilityKey(
                actualTenant, userId, key)
            : userGrants.findByTenantIdAndUserIdAndBranchIdAndCapabilityKey(
                actualTenant, userId, branchId, key);
    if (grant.isPresent()) {
      return grant.get().isEnabled();
    }
    return capabilities
        .findByTenantIdAndCapabilityKey(actualTenant, key)
        .map(TenantCapability::isEnabled)
        .orElse(false);
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
   * Creates or updates a user grant after validating all tenant boundaries.
   *
   * @param tenantId tenant identifier
   * @param userId target user identifier
   * @param request grant payload
   * @return persisted grant response
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

  /** Lists explicit grants and revocations for one tenant user. */
  @Transactional(readOnly = true)
  public List<UserCapabilityGrantResponse> listUserGrants(
      final UUID tenantId, final UUID userId) {
    users.findByIdAndHotelId(userId, tenantId)
        .orElseThrow(() -> new AccessDeniedException("USER_TENANT_MISMATCH"));
    return userGrants.findAllByTenantIdAndUserIdOrderByCapabilityKeyAsc(tenantId, userId)
        .stream().map(UserCapabilityGrantResponse::from).toList();
  }
}
