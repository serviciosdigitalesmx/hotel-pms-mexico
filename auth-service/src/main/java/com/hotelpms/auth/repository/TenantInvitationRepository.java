package com.hotelpms.auth.repository;
import com.hotelpms.auth.domain.TenantInvitation;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TenantInvitationRepository extends JpaRepository<TenantInvitation, UUID> {
  Optional<TenantInvitation> findByTokenHash(String tokenHash);
  List<TenantInvitation> findAllByHotelIdOrderByCreatedAtDesc(UUID hotelId);
}
