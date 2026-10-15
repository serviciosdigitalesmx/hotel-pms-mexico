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

/** Tenant-scoped deposit commitment, distinct from the invoice balance it settles. */
@Entity
@Table(name = "deposits")
@Getter
@Setter
@NoArgsConstructor
public class Deposit {
  public enum Status { RECEIVED, APPLIED, REVERSED }

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  @Column(name = "hotel_id", nullable = false)
  private UUID hotelId;
  @Column(name = "invoice_id", nullable = false)
  private UUID invoiceId;
  @Column(nullable = false, precision = 19, scale = 4)
  private BigDecimal amount;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private Status status = Status.RECEIVED;
  @Column(name = "reference_key", nullable = false, length = 180)
  private String referenceKey;
  @Column(name = "received_at", nullable = false)
  private LocalDateTime receivedAt = LocalDateTime.now();
}
