package com.hotelpms.billing.service.impl;

import com.hotelpms.billing.domain.CashMovement;
import com.hotelpms.billing.domain.CashMovementType;
import com.hotelpms.billing.domain.CashSession;
import com.hotelpms.billing.domain.CashSessionStatus;
import com.hotelpms.billing.domain.Expense;
import com.hotelpms.billing.domain.RecurringExpenseTemplate;
import com.hotelpms.billing.dto.RecurringExpenseRequest;
import com.hotelpms.billing.dto.ExpenseRequest;
import com.hotelpms.billing.exception.BillingValidationException;
import com.hotelpms.billing.exception.NotFoundException;
import com.hotelpms.billing.repository.CashSessionRepository;
import com.hotelpms.billing.repository.ExpenseRepository;
import com.hotelpms.billing.repository.RecurringExpenseTemplateRepository;
import com.hotelpms.billing.service.ExpenseService;
import com.hotelpms.billing.service.LedgerService;
import com.hotelpms.billing.domain.LedgerEntry;
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
  private final RecurringExpenseTemplateRepository recurringTemplates;
  private final LedgerService ledgerService;

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
    final String paymentMethod = resolvePaymentMethod(paid, request.cashSessionId(), request.paymentMethod());
    final Expense saved = expenses.save(
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
            .paymentMethod(paymentMethod)
            .paidAt(paid ? LocalDateTime.now() : null)
            .build());
    if (paid) {
      ledgerService.record(hotelId, saved.getId(), LedgerEntry.EntryType.EXPENSE,
          saved.getAmount(), "expense:" + saved.getId());
    }
    return saved;
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
    expense.setPaymentMethod(null);
    expense.setPaidAt(null);
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
      final UUID hotelId, final UUID actor, final UUID expenseId, final UUID cashSessionId,
      final String requestedPaymentMethod) {
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
    expense.setPaymentMethod(resolvePaymentMethod(true, cashSessionId, requestedPaymentMethod));
    expense.setPaidAt(LocalDateTime.now());
    if (expense.getSpentAt() == null) {
      expense.setSpentAt(java.time.LocalDate.now());
    }
    final Expense saved = expenses.save(expense);
    ledgerService.record(hotelId, saved.getId(), LedgerEntry.EntryType.EXPENSE,
        saved.getAmount(), "expense:" + saved.getId());
    return saved;
  }

  private static String resolvePaymentMethod(
      final boolean paid, final UUID cashSessionId, final String requestedPaymentMethod) {
    if (!paid) return null;
    if (cashSessionId != null) return "CASH";
    final String method = requestedPaymentMethod == null ? "OTHER" : requestedPaymentMethod;
    if ("CASH".equals(method)) throw new BillingValidationException("CASH_PAYMENT_SESSION_REQUIRED");
    return method;
  }

  @Override
  @Transactional(readOnly = true)
  public List<RecurringExpenseTemplate> listRecurring(final UUID hotelId) {
    return recurringTemplates.findAllByHotelIdOrderByNextDueAtAsc(hotelId);
  }

  @Override
  @Transactional
  public RecurringExpenseTemplate createRecurring(
      final UUID hotelId, final UUID actor, final RecurringExpenseRequest request) {
    return recurringTemplates.save(RecurringExpenseTemplate.builder()
        .hotelId(hotelId).createdBy(actor).createdAt(LocalDateTime.now())
        .category(request.category().trim()).amount(request.amount()).description(request.description())
        .reference(request.reference()).subcategory(request.subcategory()).frequency(request.frequency())
        .nextDueAt(request.nextDueAt()).active(true).build());
  }

  @Override
  @Transactional
  public Expense generateRecurring(
      final UUID hotelId, final UUID actor, final UUID templateId, final java.time.LocalDate expectedDueAt) {
    final RecurringExpenseTemplate template = recurringTemplates.findForUpdateByIdAndHotelId(templateId, hotelId)
        .orElseThrow(() -> new NotFoundException("RECURRING_EXPENSE_NOT_FOUND"));
    if (!template.isActive()) throw new BillingValidationException("RECURRING_EXPENSE_INACTIVE");
    final var dueAt = template.getNextDueAt();
    if (!dueAt.equals(expectedDueAt)) {
      return expenses.findByHotelIdAndRecurringTemplateIdAndRecurringPeriodDueAt(hotelId, templateId, expectedDueAt)
          .orElseThrow(() -> new BillingValidationException("RECURRING_EXPENSE_PERIOD_CHANGED"));
    }
    final var existing = expenses.findByHotelIdAndRecurringTemplateIdAndRecurringPeriodDueAt(hotelId, templateId, dueAt);
    if (existing.isPresent()) return existing.get();
    final Expense expense = expenses.save(Expense.builder().hotelId(hotelId).createdBy(actor)
        .createdAt(LocalDateTime.now()).category(template.getCategory()).amount(template.getAmount())
        .description(template.getDescription()).reference(template.getReference()).expenseType("FIXED")
        .subcategory(template.getSubcategory()).paid(false).dueAt(dueAt)
        .recurringTemplateId(templateId).recurringPeriodDueAt(dueAt).build());
    template.setNextDueAt(switch (template.getFrequency()) {
      case "WEEKLY" -> dueAt.plusWeeks(1);
      case "YEARLY" -> dueAt.plusYears(1);
      default -> dueAt.plusMonths(1);
    });
    recurringTemplates.save(template);
    return expense;
  }

  @Override
  @Transactional
  public RecurringExpenseTemplate setRecurringActive(
      final UUID hotelId, final UUID templateId, final boolean active) {
    final RecurringExpenseTemplate template = recurringTemplates.findByIdAndHotelId(templateId, hotelId)
        .orElseThrow(() -> new NotFoundException("RECURRING_EXPENSE_NOT_FOUND"));
    template.setActive(active);
    return recurringTemplates.save(template);
  }
}
