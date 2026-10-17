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

  /**
   * Updates descriptive and amount fields on an unpaid tenant expense.
   *
   * @param hotelId tenant identifier
   * @param expenseId expense identifier
   * @param request validated expense fields
   * @return updated expense
   */
  Expense updateUnpaid(UUID hotelId, UUID expenseId, ExpenseRequest request);

  /**
   * Lists tenant expenses.
   *
   * @param hotelId tenant identifier.
   * @return expenses ordered by creation time.
   */
  List<Expense> list(UUID hotelId);

  /**
   * Lists expenses by business date and pending due date for a report range.
   *
   * @param hotelId tenant identifier
   * @param fromDate first report date
   * @param toDate last report date
   * @return expenses included in the requested period
   */
  List<Expense> listForReport(UUID hotelId, java.time.LocalDate fromDate, java.time.LocalDate toDate);

  /**
   * Marks an outstanding expense paid, optionally recording its cash outflow.
   *
   * @param hotelId tenant identifier
   * @param actor user recording payment
   * @param expenseId expense identifier
   * @param cashSessionId open cash session, if used
   * @param paymentMethod payment method
   * @return paid expense
   */
  Expense markPaid(UUID hotelId, UUID actor, UUID expenseId, UUID cashSessionId, String paymentMethod);

  /**
   * Lists recurring expense templates for a tenant.
   *
   * @param hotelId tenant identifier
   * @return templates ordered by due date
   */
  List<RecurringExpenseTemplate> listRecurring(UUID hotelId);

  /**
   * Creates a recurring expense template without generating an expense immediately.
   *
   * @param hotelId tenant identifier
   * @param actor user creating the template
   * @param request validated recurring template
   * @return persisted template
   */
  RecurringExpenseTemplate createRecurring(UUID hotelId, UUID actor, RecurringExpenseRequest request);

  /**
   * Generates the expense scheduled for the supplied due date.
   *
   * @param hotelId tenant identifier
   * @param actor user generating the expense
   * @param templateId recurring template identifier
   * @param expectedDueAt expected period date
   * @return generated pending expense
   */
  Expense generateRecurring(UUID hotelId, UUID actor, UUID templateId, java.time.LocalDate expectedDueAt);

  /**
   * Activates or pauses a tenant's recurring expense template.
   *
   * @param hotelId tenant identifier
   * @param templateId recurring template identifier
   * @param active whether generation should remain enabled
   * @return updated template
   */
  RecurringExpenseTemplate changeRecurringActive(UUID hotelId, UUID templateId, boolean active);
}
