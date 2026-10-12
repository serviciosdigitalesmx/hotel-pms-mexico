package com.hotelpms.billing.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Tenant-scoped cash register session. */
@Entity
@Table(name = "cash_sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashSession {
  private static final int MONEY_PRECISION = 12;
  private static final int MONEY_SCALE = 2;
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "hotel_id", nullable = false)
  private UUID hotelId;

  @Column(name = "opened_by", nullable = false)
  private UUID openedBy;

  @Column(nullable = false)
  private LocalDateTime openedAt;

  private LocalDateTime closedAt;

  @Column(nullable = false, precision = MONEY_PRECISION, scale = MONEY_SCALE)
  private BigDecimal openingBalance;

  @Column(precision = MONEY_PRECISION, scale = MONEY_SCALE)
  private BigDecimal closingBalance;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private CashSessionStatus status = CashSessionStatus.OPEN;

  @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<CashMovement> movements = new ArrayList<>();
}
