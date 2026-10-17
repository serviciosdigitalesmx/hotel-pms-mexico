package com.hotelpms.auth.repository;

import com.hotelpms.auth.domain.TenantInvitation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence access for tenant-scoped user invitations. */
public interface TenantInvitationRepository extends JpaRepository<TenantInvitation, UUID> {
  /**
   * Finds an invitation by the digest of its one-time token.
   *
   * @param tokenHash SHA-256 digest
   * @return matching invitation, if any
   */
  Optional<TenantInvitation> findByTokenHash(String tokenHash);

  /**
   * Lists invitations for a tenant from newest to oldest.
   *
   * @param hotelId tenant identifier
   * @return tenant invitations
   */
  List<TenantInvitation> findAllByHotelIdOrderByCreatedAtDesc(UUID hotelId);
}
