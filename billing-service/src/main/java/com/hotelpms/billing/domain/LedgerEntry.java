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
  /** Supported financial fact categories. */
  public enum EntryType { CHARGE, PAYMENT, REFUND, DEPOSIT, EXPENSE, POS_SALE }

  private static final int AMOUNT_PRECISION = 19;
  private static final int AMOUNT_SCALE = 4;
  private static final int ENTRY_TYPE_LENGTH = 32;
  private static final int REFERENCE_KEY_LENGTH = 180;
  private static final int CURRENCY_LENGTH = 3;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  @Column(name = "hotel_id", nullable = false)
  private UUID hotelId;
  @Column(name = "aggregate_id")
  private UUID aggregateId;
  @Enumerated(EnumType.STRING)
  @Column(name = "entry_type", nullable = false, length = ENTRY_TYPE_LENGTH)
  private EntryType entryType;
  @Column(nullable = false, precision = AMOUNT_PRECISION, scale = AMOUNT_SCALE)
  private BigDecimal amount;
  @Column(name = "reference_key", nullable = false, length = REFERENCE_KEY_LENGTH)
  private String referenceKey;
  @Column(nullable = false, length = CURRENCY_LENGTH)
  private String currency = "MXN";
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();
}
