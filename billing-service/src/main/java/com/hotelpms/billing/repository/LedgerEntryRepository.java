package com.hotelpms.billing.repository;

import com.hotelpms.billing.domain.LedgerEntry;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Tenant-scoped immutable ledger access. */
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
  /**
   * Checks whether an idempotency reference has been recorded for a tenant.
   *
   * @param hotelId tenant identifier
   * @param referenceKey financial fact reference
   * @return true if that reference exists
   */
  boolean existsByHotelIdAndReferenceKey(UUID hotelId, String referenceKey);

  /**
   * Finds a tenant ledger entry by its idempotency reference.
   *
   * @param hotelId tenant identifier
   * @param referenceKey financial fact reference
   * @return matching entry, if present
   */
  java.util.Optional<LedgerEntry> findByHotelIdAndReferenceKey(UUID hotelId, String referenceKey);

  /**
   * Lists tenant ledger entries in creation order.
   *
   * @param hotelId tenant identifier
   * @return ordered tenant ledger entries
   */
  List<LedgerEntry> findAllByHotelIdOrderByCreatedAtAsc(UUID hotelId);
}
