package com.hotelpms.billing.controller;

import com.hotelpms.billing.domain.Deposit;
import com.hotelpms.billing.dto.DepositRequest;
import com.hotelpms.billing.service.DepositService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Deposit endpoint with tenant-derived identity and reference idempotency. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/invoices")
public class DepositController {
  private final DepositService service;

  @PostMapping("/{invoiceId}/deposits")
  public ResponseEntity<Deposit> receive(@PathVariable final UUID invoiceId,
      @Valid @RequestBody final DepositRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.receive(hotel(), invoiceId, request));
  }

  private UUID hotel() {
    return UUID.fromString(String.valueOf(SecurityContextHolder.getContext().getAuthentication().getDetails()));
  }
}
