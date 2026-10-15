package com.hotelpms.billing.repository;

import com.hotelpms.billing.domain.LedgerEntry;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Tenant-scoped immutable ledger access. */
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
  boolean existsByHotelIdAndReferenceKey(UUID hotelId, String referenceKey);
  java.util.Optional<LedgerEntry> findByHotelIdAndReferenceKey(UUID hotelId, String referenceKey);
  List<LedgerEntry> findAllByHotelIdOrderByCreatedAtAsc(UUID hotelId);
}
