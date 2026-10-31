package com.hotelpms.auth.service;

import com.hotelpms.auth.domain.TenantBranch;
import com.hotelpms.auth.domain.UserAccount;
import com.hotelpms.auth.domain.UserBranchMembership;
import com.hotelpms.auth.dto.CreateBranchRequest;
import com.hotelpms.auth.dto.TenantBranchResponse;
import com.hotelpms.auth.dto.UpdateBranchRequest;
import com.hotelpms.auth.exception.DuplicateResourceException;
import com.hotelpms.auth.exception.NotFoundException;
import com.hotelpms.auth.repository.TenantBranchRepository;
import com.hotelpms.auth.repository.UserAccountRepository;
import com.hotelpms.auth.repository.UserBranchMembershipRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Default implementation of {@link BranchAccessService}. */
@Slf4j
@Service
@RequiredArgsConstructor
public class BranchAccessServiceImpl implements BranchAccessService {

  private static final String BRANCH_NOT_FOUND = "BRANCH_NOT_FOUND";
  private static final String BRANCH_ACCESS_DENIED = "BRANCH_ACCESS_DENIED";

  private final TenantBranchRepository branchRepository;
  private final UserBranchMembershipRepository membershipRepository;
  private final UserAccountRepository userAccountRepository;

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public List<TenantBranchResponse> listBranches(final UUID hotelId) {
    return branchRepository.findAllByHotelIdAndActiveTrueOrderByNameAsc(hotelId).stream()
        .map(this::toResponse)
        .toList();
  }

  /**
   * Lists members only after confirming the branch is active for its tenant.
   *
   * @param hotelId tenant identifier
   * @param branchId branch identifier
   * @return member user identifiers
   */
  @Override
  @Transactional(readOnly = true)
  public List<UUID> listBranchMembers(final UUID hotelId, final UUID branchId) {
    getBranchForTenant(hotelId, branchId);
    return membershipRepository.findAllByBranchIdAndHotelId(branchId, hotelId).stream()
        .map(UserBranchMembership::getUserId)
        .toList();
  }

  /**
   * Creates a branch and records the requesting user's membership.
   *
   * @param hotelId tenant identifier
   * @param name branch display name
   * @param requestingUsername authenticated actor
   * @return created branch response
   */
  @Override
  @Transactional
  public TenantBranchResponse createBranch(
      final UUID hotelId, final String name, final String requestingUsername) {
    final String trimmedName = name.trim();
    if (branchRepository.existsByHotelIdAndNameIgnoreCase(hotelId, trimmedName)) {
      throw new DuplicateResourceException("BRANCH_NAME_EXISTS");
    }

    final TenantBranch branch =
        TenantBranch.builder().hotelId(hotelId).name(trimmedName).active(true).build();
    final TenantBranch savedBranch = branchRepository.save(branch);

    final UserAccount requestingUser =
        userAccountRepository
            .findByUsername(requestingUsername)
            .filter(user -> hotelId.equals(user.getHotelId()))
            .orElseThrow(() -> new AccessDeniedException(BRANCH_ACCESS_DENIED));

    membershipRepository.save(
        UserBranchMembership.builder()
            .userId(requestingUser.getId())
            .branchId(savedBranch.getId())
            .hotelId(hotelId)
            .build());

    log.info(
        "[AUTH] BRANCH_CREATED | branchId={} | hotelId={} | name={} | by={}",
        savedBranch.getId(),
        hotelId,
        trimmedName,
        requestingUsername);
    return toResponse(savedBranch);
  }

  /**
   * Applies the validated branch details after creating its tenant membership.
   *
   * @param hotelId tenant identifier
   * @param request validated branch details
   * @param requestingUsername authenticated actor
   * @return updated branch response
   */
  @Override
  @Transactional
  public TenantBranchResponse createBranch(
      final UUID hotelId, final CreateBranchRequest request, final String requestingUsername) {
    final TenantBranchResponse created = createBranch(hotelId, request.name(), requestingUsername);
    final TenantBranch branch =
        branchRepository
            .findByIdAndHotelId(created.id(), hotelId)
            .orElseThrow(() -> new NotFoundException(BRANCH_NOT_FOUND));
    applyBranchDetails(
        branch,
        request.phone(),
        request.extraField(),
        request.address(),
        request.timeZone(),
        request.dateFormat(),
        request.country(),
        request.currency());
    return toResponse(branchRepository.save(branch));
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public TenantBranch getBranchForTenant(final UUID tenantId, final UUID branchId) {
    final TenantBranch branch =
        branchRepository
            .findByIdAndHotelId(branchId, tenantId)
            .orElseThrow(() -> new NotFoundException(BRANCH_NOT_FOUND));
    if (!branch.isActive()) {
      throw new NotFoundException(BRANCH_NOT_FOUND);
    }
    return branch;
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public TenantBranch assertUserCanSelectBranch(
      final UUID userId, final UUID tenantId, final UUID branchId) {
    final TenantBranch branch = getBranchForTenant(tenantId, branchId);
    if (!membershipRepository.existsByUserIdAndBranchIdAndHotelId(userId, branchId, tenantId)) {
      throw new AccessDeniedException(BRANCH_ACCESS_DENIED);
    }
    return branch;
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public List<TenantBranchResponse> listBranchesForUser(final UUID userId, final UUID hotelId) {
    final List<UUID> branchIds =
        membershipRepository.findAllByUserIdAndHotelId(userId, hotelId).stream()
            .map(UserBranchMembership::getBranchId)
            .toList();
    if (branchIds.isEmpty()) {
      return List.of();
    }
    return branchRepository.findAllByHotelIdAndActiveTrueOrderByNameAsc(hotelId).stream()
        .filter(branch -> branchIds.contains(branch.getId()))
        .map(this::toResponse)
        .toList();
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public List<TenantBranchResponse> listBranchesForUser(final String username, final UUID hotelId) {
    final UserAccount user = userAccountRepository.findByUsername(username)
        .filter(account -> hotelId.equals(account.getHotelId()))
        .orElseThrow(() -> new AccessDeniedException("BRANCH_ACCESS_DENIED"));
    return listBranchesForUser(user.getId(), hotelId);
  }

  /**
   * Removes branch membership while preserving tenant-owned account history.
   *
   * @param hotelId tenant identifier
   * @param userId user identifier
   * @param branchId branch identifier
   */
  @Override
  @Transactional
  public void assignUserToBranch(final UUID hotelId, final UUID userId, final UUID branchId) {
    getBranchForTenant(hotelId, branchId);
    final UserAccount user =
        userAccountRepository
            .findById(userId)
            .filter(candidate -> hotelId.equals(candidate.getHotelId()))
            .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));
    if (!membershipRepository.existsByUserIdAndBranchIdAndHotelId(
        user.getId(), branchId, hotelId)) {
      membershipRepository.save(
          UserBranchMembership.builder()
              .userId(user.getId())
              .branchId(branchId)
              .hotelId(hotelId)
              .build());
    }
  }

  /**
   * Removes a user's branch membership without deleting the account or its history.
   *
   * @param hotelId tenant identifier
   * @param userId user identifier
   * @param branchId branch identifier
   */
  @Override
  @Transactional
  public void removeUserFromBranch(final UUID hotelId, final UUID userId, final UUID branchId) {
    getBranchForTenant(hotelId, branchId);
    userAccountRepository
        .findById(userId)
        .filter(candidate -> hotelId.equals(candidate.getHotelId()))
        .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));
    membershipRepository.deleteByUserIdAndBranchIdAndHotelId(userId, branchId, hotelId);
  }

  /**
   * Renames a branch after checking tenant ownership and duplicate names.
   *
   * @param hotelId tenant identifier
   * @param branchId branch identifier
   * @param name requested branch name
   * @return updated branch response
   */
  @Override
  @Transactional
  public TenantBranchResponse updateBranch(
      final UUID hotelId, final UUID branchId, final String name) {
    final var branch =
        branchRepository
            .findByIdAndHotelId(branchId, hotelId)
            .orElseThrow(() -> new NotFoundException(BRANCH_NOT_FOUND));
    final String trimmed = name.trim();
    if (!branch.getName().equalsIgnoreCase(trimmed)
        && branchRepository.existsByHotelIdAndNameIgnoreCase(hotelId, trimmed)) {
      throw new DuplicateResourceException("BRANCH_NAME_EXISTS");
    }
    branch.setName(trimmed);
    return toResponse(branchRepository.save(branch));
  }

  /**
   * Updates editable branch details within the tenant.
   *
   * @param hotelId tenant identifier
   * @param branchId branch identifier
   * @param request validated detail changes
   * @return updated branch response
   */
  @Override
  @Transactional
  public TenantBranchResponse updateBranch(
      final UUID hotelId, final UUID branchId, final UpdateBranchRequest request) {
    updateBranch(hotelId, branchId, request.name());
    final TenantBranch branch =
        branchRepository
            .findByIdAndHotelId(branchId, hotelId)
            .orElseThrow(() -> new NotFoundException(BRANCH_NOT_FOUND));
    applyBranchDetails(
        branch,
        request.phone(),
        request.extraField(),
        request.address(),
        request.timeZone(),
        request.dateFormat(),
        request.country(),
        request.currency());
    return toResponse(branchRepository.save(branch));
  }

  private void applyBranchDetails(
      final TenantBranch branch,
      final String phone,
      final String extraField,
      final String address,
      final String timeZone,
      final String dateFormat,
      final String country,
      final String currency) {
    if (phone != null) {
      branch.setPhone(trimToNull(phone));
    }
    if (extraField != null) {
      branch.setExtraField(trimToNull(extraField));
    }
    if (address != null) {
      branch.setAddress(trimToNull(address));
    }
    if (timeZone != null) {
      branch.setTimeZone(trimToNull(timeZone));
    }
    if (dateFormat != null) {
      branch.setDateFormat(trimToNull(dateFormat));
    }
    if (country != null) {
      branch.setCountry(trimToNull(country));
    }
    if (currency != null) {
      final String normalizedCurrency = trimToNull(currency);
      branch.setCurrency(
          normalizedCurrency == null
              ? null
              : normalizedCurrency.toUpperCase(java.util.Locale.ROOT));
    }
  }

  private String trimToNull(final String value) {
    final String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  /**
   * Changes a branch's active state without removing historical memberships.
   *
   * @param hotelId tenant identifier
   * @param branchId branch identifier
   * @param active requested active state
   */
  @Override
  @Transactional
  public void setBranchActive(final UUID hotelId, final UUID branchId, final boolean active) {
    final var branch =
        branchRepository
            .findByIdAndHotelIdIncludingInactive(branchId, hotelId)
            .orElseThrow(() -> new NotFoundException(BRANCH_NOT_FOUND));
    branch.setActive(active);
    branchRepository.save(branch);
  }

  private TenantBranchResponse toResponse(final TenantBranch branch) {
    return new TenantBranchResponse(
        branch.getId(),
        branch.getHotelId(),
        branch.getName(),
        branch.isActive(),
        branch.getCreatedAt(),
        branch.getPhone(),
        branch.getExtraField(),
        branch.getAddress(),
        branch.getTimeZone(),
        branch.getDateFormat(),
        branch.getCountry(),
        branch.getCurrency());
  }
}
