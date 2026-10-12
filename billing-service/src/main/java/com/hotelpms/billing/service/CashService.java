package com.hotelpms.billing.service;

import com.hotelpms.billing.domain.CashSession;
import com.hotelpms.billing.dto.CashDtos.CloseSessionRequest;
import com.hotelpms.billing.dto.CashDtos.MovementRequest;
import com.hotelpms.billing.dto.CashDtos.OpenSessionRequest;
import java.util.UUID;

/** Cash register operations scoped to one tenant. */
public interface CashService {
  /**
   * Opens a cash session.
   *
   * @param hotelId tenant identifier.
   *
   * @param actor actor identifier.
   *
   * @param request opening request.
   * @return opened session.
   */
  CashSession open(UUID hotelId, UUID actor, OpenSessionRequest request);

  /**
   * Records a cash movement.
   *
   * @param hotelId tenant identifier.
   *
   * @param actor actor identifier.
   *
   * @param sessionId session identifier.
   *
   * @param request movement request.
   * @return updated session.
   */
  CashSession movement(UUID hotelId, UUID actor, UUID sessionId, MovementRequest request);

  /**
   * Closes a cash session.
   *
   * @param hotelId tenant identifier.
   *
   * @param sessionId session identifier.
   *
   * @param request closing request.
   * @return closed session.
   */
  CashSession close(UUID hotelId, UUID sessionId, CloseSessionRequest request);
}
