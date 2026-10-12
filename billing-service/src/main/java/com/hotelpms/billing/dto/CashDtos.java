package com.hotelpms.billing.dto;

import com.hotelpms.billing.domain.CashMovementType;
import com.hotelpms.billing.domain.CashSessionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Requests and responses for cash operations. */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class CashDtos {
  private CashDtos() { }

  /**
   * Cash session opening request.
   *
   * @param openingBalance opening amount.
   */
  public record OpenSessionRequest(@NotNull @PositiveOrZero BigDecimal openingBalance) { }

  /**
   * Cash movement request.
   *
   * @param type movement type.
   * @param amount movement amount.
   * @param reference external reference.
   */
  public record MovementRequest(
      @NotNull CashMovementType type,
      @NotNull @PositiveOrZero BigDecimal amount,
      String reference) { }

  /**
   * Cash session closing request.
   *
   * @param closingBalance counted closing amount.
   */
  public record CloseSessionRequest(@NotNull @PositiveOrZero BigDecimal closingBalance) { }

  /**
   * Cash session response.
   *
   * @param id session identifier.
   * @param status session status.
   * @param openingBalance opening amount.
   * @param closingBalance closing amount.
   * @param openedAt opening timestamp.
   * @param closedAt closing timestamp.
   */
  public record SessionResponse(
      UUID id,
      CashSessionStatus status,
      BigDecimal openingBalance,
      BigDecimal closingBalance,
      LocalDateTime openedAt,
      LocalDateTime closedAt) { }
}
