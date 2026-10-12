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
    return repository.findAllByHotelIdOrderByCreatedAtAsc(hotelId).stream()
        .filter(entry -> referenceKey.equals(entry.getReferenceKey())).findFirst()
        .orElseGet(() -> {
          final LedgerEntry entry = new LedgerEntry();
          entry.setHotelId(hotelId);
          entry.setAggregateId(aggregateId);
          entry.setEntryType(type);
          entry.setAmount(amount);
          entry.setReferenceKey(referenceKey);
          return repository.save(entry);
        });
  }

  @Transactional(readOnly = true)
  public List<LedgerEntry> list(final UUID hotelId) {
    return repository.findAllByHotelIdOrderByCreatedAtAsc(hotelId);
  }
}
