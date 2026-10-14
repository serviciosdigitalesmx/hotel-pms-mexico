package com.hotelpms.billing.service.impl;

import com.hotelpms.billing.domain.CashMovement;
import com.hotelpms.billing.domain.CashMovementType;
import com.hotelpms.billing.domain.CashSession;
import com.hotelpms.billing.domain.CashSessionStatus;
import com.hotelpms.billing.domain.Expense;
import com.hotelpms.billing.dto.ExpenseRequest;
import com.hotelpms.billing.exception.BillingValidationException;
import com.hotelpms.billing.exception.NotFoundException;
import com.hotelpms.billing.repository.CashSessionRepository;
import com.hotelpms.billing.repository.ExpenseRepository;
import com.hotelpms.billing.service.ExpenseService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Expense implementation with optional cash outflow correlation. */
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {
  private final ExpenseRepository expenses;
  private final CashSessionRepository sessions;

  /** {@inheritDoc} */
  @Override
  @Transactional
  public Expense create(
      final UUID hotelId, final UUID actor, final ExpenseRequest request) {
    final boolean paid = request.paid() == null || request.paid();
    if (!paid && request.cashSessionId() != null) {
      throw new BillingValidationException("PENDING_EXPENSE_CANNOT_USE_CASH_SESSION");
    }
    if (request.cashSessionId() != null) {
      final CashSession session =
          sessions
              .findByIdAndHotelId(request.cashSessionId(), hotelId)
              .orElseThrow(() -> new NotFoundException("CASH_SESSION_NOT_FOUND"));
      if (session.getStatus() != CashSessionStatus.OPEN) {
        throw new BillingValidationException("CASH_SESSION_CLOSED");
      }
      session
          .getMovements()
          .add(
              CashMovement.builder()
                  .session(session)
                  .type(CashMovementType.CASH_OUT)
                  .amount(request.amount())
                  .reference(request.reference())
                  .createdBy(actor)
                  .createdAt(LocalDateTime.now())
                  .build());
      sessions.save(session);
    }
    return expenses.save(
        Expense.builder()
            .hotelId(hotelId)
            .category(request.category().trim())
            .amount(request.amount())
            .description(request.description())
            .reference(request.reference())
            .cashSessionId(request.cashSessionId())
            .createdBy(actor)
            .createdAt(LocalDateTime.now())
            .expenseType(request.expenseType() == null ? "VARIABLE" : request.expenseType())
            .subcategory(request.subcategory())
            .paid(paid)
            .spentAt(request.spentAt())
            .dueAt(request.dueAt())
            .build());
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public List<Expense> list(final UUID hotelId) {
    return expenses.findAllByHotelIdOrderByCreatedAtDesc(hotelId);
  }
}
