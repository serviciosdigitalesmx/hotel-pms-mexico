package com.hotelpms.auth.repository;

import com.hotelpms.auth.domain.PlatformAdminGrant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Platform-only authority grants, never derived from tenant role or tenant identifier. */
public interface PlatformAdminGrantRepository extends JpaRepository<PlatformAdminGrant, UUID> {
  /**
   * Tests whether the given account has an active platform grant.
   *
   * @param userId account identifier
   * @return whether the account has an active grant
   */
  @Query("select case when count(g) > 0 then true else false end"
      + " from PlatformAdminGrant g where g.user.id = :userId and g.active = true")
  boolean hasActiveGrantForUserId(@Param("userId") UUID userId);

  /**
   * Tests whether the username has an active platform grant.
   *
   * @param username globally unique account login
   * @return whether the account has an active grant
   */
  @Query("select case when count(g) > 0 then true else false end"
      + " from PlatformAdminGrant g where g.user.username = :username and g.active = true")
  boolean hasActiveGrantForUsername(@Param("username") String username);

  /**
   * Finds the grant for an account, whether active or revoked.
   *
   * @param userId account identifier
   * @return grant when one has been recorded
   */
  @Query("select g from PlatformAdminGrant g where g.user.id = :userId")
  Optional<PlatformAdminGrant> findGrantForUserId(@Param("userId") UUID userId);

  /**
   * Counts active platform grants.
   *
   * @return current active grant count
   */
  long countByActiveTrue();
}
