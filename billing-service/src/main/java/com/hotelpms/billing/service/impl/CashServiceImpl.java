package com.hotelpms.billing.service.impl;

import com.hotelpms.billing.domain.CashMovement;
import com.hotelpms.billing.domain.CashSession;
import com.hotelpms.billing.domain.CashSessionStatus;
import com.hotelpms.billing.dto.CashDtos.CloseSessionRequest;
import com.hotelpms.billing.dto.CashDtos.MovementRequest;
import com.hotelpms.billing.dto.CashDtos.OpenSessionRequest;
import com.hotelpms.billing.exception.BillingValidationException;
import com.hotelpms.billing.exception.NotFoundException;
import com.hotelpms.billing.repository.CashSessionRepository;
import com.hotelpms.billing.service.CashService;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Transactional cash register implementation. */
@Service
@RequiredArgsConstructor
public class CashServiceImpl implements CashService {
  private final CashSessionRepository sessions;

  /** {@inheritDoc} */
  @Override
  @Transactional
  public CashSession open(
      final UUID hotelId, final UUID actor, final OpenSessionRequest request) {
    sessions
        .findFirstByHotelIdAndStatusOrderByOpenedAtDesc(hotelId, CashSessionStatus.OPEN)
        .ifPresent(
            existing -> {
              throw new BillingValidationException("CASH_SESSION_ALREADY_OPEN");
            });
    return sessions.save(
        CashSession.builder()
            .hotelId(hotelId)
            .openedBy(actor)
            .openedAt(LocalDateTime.now())
            .openingBalance(request.openingBalance())
            .status(CashSessionStatus.OPEN)
            .build());
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public CashSession movement(
      final UUID hotelId,
      final UUID actor,
      final UUID sessionId,
      final MovementRequest request) {
    final CashSession session = getOpen(hotelId, sessionId);
    session
        .getMovements()
        .add(
            CashMovement.builder()
                .session(session)
                .type(request.type())
                .amount(request.amount())
                .reference(request.reference())
                .createdBy(actor)
                .createdAt(LocalDateTime.now())
                .build());
    return sessions.save(session);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public CashSession close(
      final UUID hotelId, final UUID sessionId, final CloseSessionRequest request) {
    final CashSession session = getOpen(hotelId, sessionId);
    session.setClosingBalance(request.closingBalance());
    session.setClosedAt(LocalDateTime.now());
    session.setStatus(CashSessionStatus.CLOSED);
    return sessions.save(session);
  }

  private CashSession getOpen(final UUID hotelId, final UUID id) {
    final CashSession session =
        sessions
            .findByIdAndHotelId(id, hotelId)
            .orElseThrow(() -> new NotFoundException("CASH_SESSION_NOT_FOUND"));
    if (session.getStatus() != CashSessionStatus.OPEN) {
      throw new BillingValidationException("CASH_SESSION_CLOSED");
    }
    return session;
  }
}
