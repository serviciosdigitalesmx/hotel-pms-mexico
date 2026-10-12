package com.hotelpms.billing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Immutable double-entry-compatible financial fact. */
@Entity
@Table(name = "ledger_entries")
@Getter
@Setter
@NoArgsConstructor
public class LedgerEntry {
  public enum EntryType { CHARGE, PAYMENT, REFUND, DEPOSIT, EXPENSE, POS_SALE }

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  @Column(name = "hotel_id", nullable = false)
  private UUID hotelId;
  @Column(name = "aggregate_id")
  private UUID aggregateId;
  @Enumerated(EnumType.STRING)
  @Column(name = "entry_type", nullable = false, length = 32)
  private EntryType entryType;
  @Column(nullable = false, precision = 19, scale = 4)
  private BigDecimal amount;
  @Column(name = "reference_key", nullable = false, unique = true, length = 180)
  private String referenceKey;
  @Column(nullable = false, length = 3)
  private String currency = "MXN";
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();
}
