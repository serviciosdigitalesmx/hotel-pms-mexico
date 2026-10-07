package com.hotelpms.auth.repository;

import com.hotelpms.auth.domain.TenantBranch;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for {@link TenantBranch}. Every method that looks up by branch identifier also filters
 * by {@code hotelId} — a branch UUID is meaningless outside its tenant.
 */
@Repository
public interface TenantBranchRepository extends JpaRepository<TenantBranch, UUID> {

  /**
   * Lists active branches for a tenant, ordered by name.
   *
   * @param hotelId the tenant UUID
   * @return active branches belonging to that tenant
   */
  List<TenantBranch> findAllByHotelIdAndActiveTrueOrderByNameAsc(UUID hotelId);

  /**
   * Finds a branch by id and tenant.
   *
   * @param id branch UUID
   * @param hotelId tenant UUID
   * @return the branch if it belongs to the tenant
   */
  Optional<TenantBranch> findByIdAndHotelId(UUID id, UUID hotelId);

  @Query(value="SELECT * FROM tenant_branch WHERE id=:id AND hotel_id=:hotelId",nativeQuery=true)
  Optional<TenantBranch> findByIdAndHotelIdIncludingInactive(@Param("id") UUID id,@Param("hotelId") UUID hotelId);

  /**
   * Checks whether a branch name is already used in a tenant.
   *
   * @param hotelId tenant UUID
   * @param name case-insensitive branch name
   * @return {@code true} if the name exists for this tenant
   */
  boolean existsByHotelIdAndNameIgnoreCase(UUID hotelId, String name);
}
