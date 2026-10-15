package com.hotelpms.billing.service;

import com.hotelpms.billing.domain.LedgerEntry;
import com.hotelpms.billing.repository.LedgerEntryRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Single write boundary for financial facts; duplicate references are idempotent. */
@Service
@RequiredArgsConstructor
public class LedgerService {
  private final LedgerEntryRepository repository;

  @Transactional
  public LedgerEntry record(final UUID hotelId, final UUID aggregateId,
      final LedgerEntry.EntryType type, final BigDecimal amount, final String referenceKey) {
    if (amount == null || amount.signum() <= 0 || referenceKey == null || referenceKey.isBlank()) {
      throw new IllegalArgumentException("INVALID_LEDGER_ENTRY");
    }
    final LedgerEntry existing = repository.findByHotelIdAndReferenceKey(hotelId, referenceKey)
        .orElse(null);
    if (existing != null) {
      if (!java.util.Objects.equals(existing.getAggregateId(), aggregateId)
          || existing.getEntryType() != type
          || existing.getAmount().compareTo(amount) != 0) {
        throw new IllegalStateException("LEDGER_REFERENCE_CONFLICT");
      }
      return existing;
    }
    return repository.save(createEntry(hotelId, aggregateId, type, amount, referenceKey));
  }

  private static LedgerEntry createEntry(final UUID hotelId, final UUID aggregateId,
      final LedgerEntry.EntryType type, final BigDecimal amount, final String referenceKey) {
          final LedgerEntry entry = new LedgerEntry();
          entry.setHotelId(hotelId);
          entry.setAggregateId(aggregateId);
          entry.setEntryType(type);
          entry.setAmount(amount);
          entry.setReferenceKey(referenceKey);
          return entry;
  }

  @Transactional(readOnly = true)
  public List<LedgerEntry> list(final UUID hotelId) {
    return repository.findAllByHotelIdOrderByCreatedAtAsc(hotelId);
  }
}
