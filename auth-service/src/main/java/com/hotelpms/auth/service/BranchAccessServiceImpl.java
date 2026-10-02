package com.hotelpms.auth.service;

import com.hotelpms.auth.domain.TenantBranch;
import com.hotelpms.auth.domain.UserAccount;
import com.hotelpms.auth.domain.UserBranchMembership;
import com.hotelpms.auth.dto.TenantBranchResponse;
import com.hotelpms.auth.exception.DuplicateResourceException;
import com.hotelpms.auth.exception.NotFoundException;
import com.hotelpms.auth.repository.TenantBranchRepository;
import com.hotelpms.auth.repository.UserAccountRepository;
import com.hotelpms.auth.repository.UserBranchMembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Default implementation of {@link BranchAccessService}.
 */
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
        return branchRepository.findAllByHotelIdAndActiveTrueOrderByNameAsc(hotelId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public TenantBranchResponse createBranch(final UUID hotelId, final String name,
            final String requestingUsername) {
        final String trimmedName = name.trim();
        if (branchRepository.existsByHotelIdAndNameIgnoreCase(hotelId, trimmedName)) {
            throw new DuplicateResourceException("BRANCH_NAME_EXISTS");
        }

        final TenantBranch branch = TenantBranch.builder()
                .hotelId(hotelId)
                .name(trimmedName)
                .active(true)
                .build();
        final TenantBranch savedBranch = branchRepository.save(branch);

        final UserAccount requestingUser = userAccountRepository.findByUsername(requestingUsername)
                .filter(user -> hotelId.equals(user.getHotelId()))
                .orElseThrow(() -> new AccessDeniedException(BRANCH_ACCESS_DENIED));

        membershipRepository.save(UserBranchMembership.builder()
                .userId(requestingUser.getId())
                .branchId(savedBranch.getId())
                .hotelId(hotelId)
                .build());

        log.info("[AUTH] BRANCH_CREATED | branchId={} | hotelId={} | name={} | by={}",
                savedBranch.getId(), hotelId, trimmedName, requestingUsername);
        return toResponse(savedBranch);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public TenantBranch getBranchForTenant(final UUID tenantId, final UUID branchId) {
        final TenantBranch branch = branchRepository.findByIdAndHotelId(branchId, tenantId)
                .orElseThrow(() -> new NotFoundException(BRANCH_NOT_FOUND));
        if (!branch.isActive()) {
            throw new NotFoundException(BRANCH_NOT_FOUND);
        }
        return branch;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public TenantBranch assertUserCanSelectBranch(final UUID userId, final UUID tenantId,
            final UUID branchId) {
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
        final List<UUID> branchIds = membershipRepository.findAllByUserIdAndHotelId(userId, hotelId)
                .stream()
                .map(UserBranchMembership::getBranchId)
                .toList();
        if (branchIds.isEmpty()) {
            return List.of();
        }
        return branchRepository.findAllByHotelIdAndActiveTrueOrderByNameAsc(hotelId)
                .stream()
                .filter(branch -> branchIds.contains(branch.getId()))
                .map(this::toResponse)
                .toList();
    }

    private TenantBranchResponse toResponse(final TenantBranch branch) {
        return new TenantBranchResponse(
                branch.getId(),
                branch.getHotelId(),
                branch.getName(),
                branch.isActive(),
                branch.getCreatedAt());
    }
}
