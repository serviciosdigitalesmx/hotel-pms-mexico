package com.hotelpms.billing.service;

import com.hotelpms.billing.domain.Expense;
import com.hotelpms.billing.dto.ExpenseRequest;
import com.hotelpms.billing.domain.RecurringExpenseTemplate;
import com.hotelpms.billing.dto.RecurringExpenseRequest;
import java.util.List;
import java.util.UUID;

/** Expense operations. */
public interface ExpenseService {
  /**
   * Creates an expense.
   *
   * @param hotelId tenant identifier.
   *
   * @param actor actor identifier.
   *
   * @param request expense request.
   * @return persisted expense.
   */
  Expense create(UUID hotelId, UUID actor, ExpenseRequest request);

  /** Updates descriptive and amount fields on an unpaid tenant expense. */
  Expense updateUnpaid(UUID hotelId, UUID expenseId, ExpenseRequest request);

  /**
   * Lists tenant expenses.
   *
   * @param hotelId tenant identifier.
   * @return expenses ordered by creation time.
   */
  List<Expense> list(UUID hotelId);

  /** Lists expenses by business date and pending due date for a report range. */
  List<Expense> listForReport(UUID hotelId, java.time.LocalDate fromDate, java.time.LocalDate toDate);

  /** Marks an outstanding expense paid, optionally recording its cash outflow. */
  Expense markPaid(UUID hotelId, UUID actor, UUID expenseId, UUID cashSessionId, String paymentMethod);

  List<RecurringExpenseTemplate> listRecurring(UUID hotelId);

  RecurringExpenseTemplate createRecurring(UUID hotelId, UUID actor, RecurringExpenseRequest request);

  Expense generateRecurring(UUID hotelId, UUID actor, UUID templateId, java.time.LocalDate expectedDueAt);

  RecurringExpenseTemplate setRecurringActive(UUID hotelId, UUID templateId, boolean active);
}
