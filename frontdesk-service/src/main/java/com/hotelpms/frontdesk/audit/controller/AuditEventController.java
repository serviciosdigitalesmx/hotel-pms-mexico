package com.hotelpms.frontdesk.audit.controller;

import com.hotelpms.frontdesk.audit.domain.AuditEvent;
import com.hotelpms.frontdesk.audit.service.AuditEventService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Tenant-scoped audit history endpoint. Writes remain service-owned. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/audit-events")
public class AuditEventController {
  private final AuditEventService service;

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER')")
  public List<AuditEvent> recent() {
    return service.recent(hotel());
  }

  private UUID hotel() {
    return UUID.fromString(String.valueOf(SecurityContextHolder.getContext().getAuthentication().getDetails()));
  }
}
