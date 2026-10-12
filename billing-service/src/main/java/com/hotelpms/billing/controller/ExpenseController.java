package com.hotelpms.billing.controller;

import com.hotelpms.billing.domain.Expense;
import com.hotelpms.billing.dto.ExpenseRequest;
import com.hotelpms.billing.service.ExpenseService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Tenant-scoped expense API. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/expenses")
public class ExpenseController {
  private final ExpenseService service;

  /**
   * Creates a tenant-scoped expense.
   *
   * @param request expense request.
   *
   * @return created expense response.
   */
  @PostMapping
  @PreAuthorize("hasAuthority('finance.write')")
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
  @PreAuthorize("hasAuthority('finance.read')")
  public List<Expense> list() {
    return service.list(hotel());
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
}
