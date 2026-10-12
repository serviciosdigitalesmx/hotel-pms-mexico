package com.hotelpms.billing.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Audited cash movement attached to one cash session. */
@Entity
@Table(name = "cash_movements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashMovement {
  private static final int MONEY_PRECISION = 12;
  private static final int MONEY_SCALE = 2;
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "session_id")
  private CashSession session;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CashMovementType type;

  @Column(nullable = false, precision = MONEY_PRECISION, scale = MONEY_SCALE)
  private BigDecimal amount;

  @Column(length = 100)
  private String reference;

  @Column(name = "created_by", nullable = false)
  private UUID createdBy;

  @Column(nullable = false)
  private LocalDateTime createdAt;
}
