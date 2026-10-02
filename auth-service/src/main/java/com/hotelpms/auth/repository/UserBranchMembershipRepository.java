package com.hotelpms.auth.repository;

import com.hotelpms.auth.domain.UserBranchMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for user-to-branch memberships. Lookups are always tenant-scoped.
 */
@Repository
public interface UserBranchMembershipRepository extends JpaRepository<UserBranchMembership, UUID> {

    /**
     * Returns all branch memberships for a user within a tenant.
     *
     * @param userId  the user UUID
     * @param hotelId the tenant UUID
     * @return memberships in that tenant
     */
    List<UserBranchMembership> findAllByUserIdAndHotelId(UUID userId, UUID hotelId);

    /**
     * Checks whether the user is a member of the given branch in the given tenant.
     *
     * @param userId   the user UUID
     * @param branchId the branch UUID
     * @param hotelId  the tenant UUID
     * @return {@code true} if membership exists
     */
    boolean existsByUserIdAndBranchIdAndHotelId(UUID userId, UUID branchId, UUID hotelId);
}
