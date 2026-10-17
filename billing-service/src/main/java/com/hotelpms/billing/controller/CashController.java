package com.hotelpms.billing.controller;

import com.hotelpms.billing.domain.CashSession;
import com.hotelpms.billing.dto.CashDtos.CloseSessionRequest;
import com.hotelpms.billing.dto.CashDtos.MovementRequest;
import com.hotelpms.billing.dto.CashDtos.OpenSessionRequest;
import com.hotelpms.billing.service.CashService;
import jakarta.validation.Valid;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Tenant-scoped cash register API. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cash")
public class CashController {
  private final CashService service;

  /**
   * Returns only the current tenant's open session summary.
   *
   * @return open session summary, or no content when none exists
   */
  @GetMapping("/sessions/open")
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
  public ResponseEntity<com.hotelpms.billing.dto.CashDtos.SessionResponse> currentOpen() {
    return ResponseEntity.of(service.currentOpen(hotel()));
  }

  /**
   * Opens a cash session for the current tenant.
   *
   * @param request opening request.
   *
   * @return created session response.
   */
  @PostMapping("/sessions")
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
  public ResponseEntity<CashSession> open(@Valid @RequestBody final OpenSessionRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.open(hotel(), actor(), request));
  }

  /**
   * Records a movement in an open cash session.
   *
   * @param id session identifier.
   * @param request movement request.
   *
   * @return updated session.
   */
  @PostMapping("/sessions/{id}/movements")
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
  public CashSession movement(
      @PathVariable final UUID id, @Valid @RequestBody final MovementRequest request) {
    return service.movement(hotel(), actor(), id, request);
  }

  /**
   * Closes an open cash session.
   *
   * @param id session identifier.
   * @param request closing request.
   *
   * @return closed session.
   */
  @PostMapping("/sessions/{id}/close")
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
  public CashSession close(
      @PathVariable final UUID id, @Valid @RequestBody final CloseSessionRequest request) {
    return service.close(hotel(), id, request);
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
