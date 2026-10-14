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
    validateDates(request);
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
            .spentAt(request.spentAt() == null && paid ? java.time.LocalDate.now() : request.spentAt())
            .dueAt(request.dueAt())
            .build());
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public List<Expense> list(final UUID hotelId) {
    return expenses.findAllByHotelIdOrderByCreatedAtDesc(hotelId);
  }

  @Override
  @Transactional
  public Expense updateUnpaid(final UUID hotelId, final UUID expenseId, final ExpenseRequest request) {
    validateDates(request);
    final Expense expense = expenses.findByIdAndHotelId(expenseId, hotelId)
        .orElseThrow(() -> new NotFoundException("EXPENSE_NOT_FOUND"));
    if (expense.isPaid() || expense.getCashSessionId() != null) {
      throw new BillingValidationException("PAID_EXPENSE_CANNOT_BE_EDITED");
    }
    if (request.cashSessionId() != null || Boolean.TRUE.equals(request.paid())) {
      throw new BillingValidationException("UNPAID_EXPENSE_UPDATE_CANNOT_RECORD_PAYMENT");
    }
    expense.setCategory(request.category().trim());
    expense.setAmount(request.amount());
    expense.setDescription(request.description());
    expense.setReference(request.reference());
    expense.setExpenseType(request.expenseType() == null ? "VARIABLE" : request.expenseType());
    expense.setSubcategory(request.subcategory());
    expense.setSpentAt(request.spentAt());
    expense.setDueAt(request.dueAt());
    return expenses.save(expense);
  }

  private static void validateDates(final ExpenseRequest request) {
    if (request.spentAt() != null && request.dueAt() != null
        && request.dueAt().isBefore(request.spentAt())) {
      throw new BillingValidationException("EXPENSE_DUE_DATE_BEFORE_SPENT_DATE");
    }
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public Expense markPaid(
      final UUID hotelId, final UUID actor, final UUID expenseId, final UUID cashSessionId) {
    final Expense expense = expenses.findByIdAndHotelId(expenseId, hotelId)
        .orElseThrow(() -> new NotFoundException("EXPENSE_NOT_FOUND"));
    if (expense.isPaid()) {
      throw new BillingValidationException("EXPENSE_ALREADY_PAID");
    }
    if (cashSessionId != null) {
      final CashSession session = sessions.findByIdAndHotelId(cashSessionId, hotelId)
          .orElseThrow(() -> new NotFoundException("CASH_SESSION_NOT_FOUND"));
      if (session.getStatus() != CashSessionStatus.OPEN) {
        throw new BillingValidationException("CASH_SESSION_CLOSED");
      }
      session.getMovements().add(CashMovement.builder()
          .session(session)
          .type(CashMovementType.CASH_OUT)
          .amount(expense.getAmount())
          .reference(expense.getReference())
          .createdBy(actor)
          .createdAt(LocalDateTime.now())
          .build());
      sessions.save(session);
    }
    expense.setPaid(true);
    expense.setCashSessionId(cashSessionId);
    if (expense.getSpentAt() == null) {
      expense.setSpentAt(java.time.LocalDate.now());
    }
    return expenses.save(expense);
  }
}
