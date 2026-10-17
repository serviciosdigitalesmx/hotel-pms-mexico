package com.hotelpms.billing.controller;

import com.hotelpms.billing.domain.Expense;
import com.hotelpms.billing.dto.ExpenseRequest;
import com.hotelpms.billing.dto.RecurringExpenseRequest;
import com.hotelpms.billing.domain.RecurringExpenseTemplate;
import com.hotelpms.billing.service.ExpenseService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Tenant-scoped expense API. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/expenses")
public class ExpenseController {
  private static final String EXPENSE_ADMIN_ROLES = "hasAnyRole('ADMIN', 'OWNER', 'MANAGER')";
  private static final String EXPENSE_PAYMENT_METHOD_PATTERN = "CASH|BANK_TRANSFER|CARD|OTHER";
  private final ExpenseService service;

  /**
   * Creates a tenant-scoped expense.
   *
   * @param request expense request
   * @return created expense response
   */
  @PostMapping
  @PreAuthorize(EXPENSE_ADMIN_ROLES)
  public ResponseEntity<Expense> create(@Valid @RequestBody final ExpenseRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(service.create(hotel(), actor(), request));
  }

  /**
   * Lists expenses for the current tenant.
   *
   * @return tenant expenses.
   */
  @GetMapping
  @PreAuthorize(EXPENSE_ADMIN_ROLES)
  public List<Expense> list() {
    return service.list(hotel());
  }

  /**
   * Lists tenant expenses whose business date or pending due date falls in a report range.
   *
   * @param from first date in report range
   * @param to last date in report range
   * @return matching tenant expenses
   */
  @GetMapping("/report-range")
  @PreAuthorize(EXPENSE_ADMIN_ROLES)
  public List<Expense> listForReport(@RequestParam final LocalDate from,
      @RequestParam final LocalDate to) {
    return service.listForReport(hotel(), from, to);
  }

  /**
   * Lists recurring expense templates for the current tenant.
   *
   * @return templates ordered by due date
   */
  @GetMapping("/recurring")
  @PreAuthorize(EXPENSE_ADMIN_ROLES)
  public List<RecurringExpenseTemplate> listRecurring() {
    return service.listRecurring(hotel());
  }

  /**
   * Creates a recurring expense template.
   *
   * @param request validated template details
   * @return created template
   */
  @PostMapping("/recurring")
  @PreAuthorize(EXPENSE_ADMIN_ROLES)
  public ResponseEntity<RecurringExpenseTemplate> createRecurring(
      @Valid @RequestBody final RecurringExpenseRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(service.createRecurring(hotel(), actor(), request));
  }

  /**
   * Generates a pending expense for a recurring template's due period.
   *
   * @param id recurring template identifier
   * @param request expected due date
   * @return generated expense
   */
  @PostMapping("/recurring/{id}/generate")
  @PreAuthorize(EXPENSE_ADMIN_ROLES)
  public ResponseEntity<Expense> generateRecurring(
      @PathVariable final UUID id, @Valid @RequestBody final GenerateRecurringRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(service.generateRecurring(hotel(), actor(), id, request.expectedDueAt()));
  }

  /**
   * Activates or pauses a tenant-owned recurring expense template.
   *
   * @param id recurring template identifier
   * @param request desired active state
   * @return updated template
   */
  @PatchMapping("/recurring/{id}/active")
  @PreAuthorize(EXPENSE_ADMIN_ROLES)
  public RecurringExpenseTemplate changeRecurringActive(
      @PathVariable final UUID id, @Valid @RequestBody final RecurringActiveRequest request) {
    return service.changeRecurringActive(hotel(), id, request.active());
  }

  /**
   * Updates fields of a tenant-owned unpaid expense.
   *
   * @param id expense identifier
   * @param request updated expense details
   * @return updated expense
   */
  @PutMapping("/{id}")
  @PreAuthorize(EXPENSE_ADMIN_ROLES)
  public Expense update(@PathVariable final UUID id, @Valid @RequestBody final ExpenseRequest request) {
    return service.updateUnpaid(hotel(), id, request);
  }

  /**
   * Marks an unpaid expense as paid and records the selected method.
   *
   * @param id expense identifier
   * @param request payment method and optional cash session
   * @return paid expense
   */
  @PatchMapping("/{id}/paid")
  @PreAuthorize(EXPENSE_ADMIN_ROLES)
  public Expense markPaid(
      @PathVariable final UUID id, @Valid @RequestBody(required = false) final PayExpenseRequest request) {
    return service.markPaid(hotel(), actor(), id, request == null ? null : request.cashSessionId(),
        request == null ? null : request.paymentMethod());
  }

  private UUID hotel() {
    return UUID.fromString(
        String.valueOf(SecurityContextHolder.getContext().getAuthentication().getDetails()));
  }

  private UUID actor() {
    return UUID.nameUUIDFromBytes(
        SecurityContextHolder.getContext().getAuthentication().getName()
            .getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Request for generating a single recurring expense period.
   *
   * @param expectedDueAt expected scheduled date
   */
  public record GenerateRecurringRequest(
      @jakarta.validation.constraints.NotNull LocalDate expectedDueAt) { }

  /**
   * Request for activating or pausing a recurring template.
   *
   * @param active whether the template remains active
   */
  public record RecurringActiveRequest(
      @jakarta.validation.constraints.NotNull Boolean active) { }

  /**
   * Optional cash and method details for paying an expense.
   *
   * @param cashSessionId optional open cash session
   * @param paymentMethod selected payment method
   */
  public record PayExpenseRequest(UUID cashSessionId,
      @jakarta.validation.constraints.Pattern(regexp = EXPENSE_PAYMENT_METHOD_PATTERN)
      String paymentMethod) { }
}
