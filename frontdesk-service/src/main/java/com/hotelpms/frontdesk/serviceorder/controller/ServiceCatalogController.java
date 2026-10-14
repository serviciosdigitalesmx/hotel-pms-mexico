package com.hotelpms.frontdesk.serviceorder.controller;

import com.hotelpms.frontdesk.serviceorder.domain.ServiceCatalogEntry;
import com.hotelpms.frontdesk.serviceorder.dto.ServiceCatalogRequest;
import com.hotelpms.frontdesk.serviceorder.service.ServiceCatalogService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/services")
public class ServiceCatalogController {
  private final ServiceCatalogService service;

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST','TECHNICIAN')")
  public List<ServiceCatalogEntry> list() { return service.list(tenantId()); }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER')")
  public ServiceCatalogEntry create(@Valid @RequestBody ServiceCatalogRequest request) {
    return service.create(tenantId(), request);
  }

  @PatchMapping("/{id}/active")
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER')")
  public ServiceCatalogEntry setActive(@PathVariable UUID id, @RequestParam boolean value) {
    return service.setActive(tenantId(), id, value);
  }

  private UUID tenantId() {
    return UUID.fromString(String.valueOf(
        SecurityContextHolder.getContext().getAuthentication().getDetails()));
  }
}
