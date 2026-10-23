package com.hotelpms.auth.repository;

import com.hotelpms.auth.domain.PlatformAdminGrant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Platform-only authority grants, never derived from tenant role or tenant identifier. */
public interface PlatformAdminGrantRepository extends JpaRepository<PlatformAdminGrant, UUID> {
  boolean existsByUser_IdAndActiveTrue(UUID userId);

  boolean existsByUser_UsernameAndActiveTrue(String username);

  Optional<PlatformAdminGrant> findByUser_Id(UUID userId);

  long countByActiveTrue();
}
