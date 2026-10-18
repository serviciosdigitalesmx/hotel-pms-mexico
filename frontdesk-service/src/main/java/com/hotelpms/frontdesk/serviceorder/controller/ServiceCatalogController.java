package com.hotelpms.frontdesk.serviceorder.controller;

import com.hotelpms.frontdesk.serviceorder.domain.ServiceCatalogEntry;
import com.hotelpms.frontdesk.serviceorder.dto.ServiceCatalogRequest;
import com.hotelpms.frontdesk.serviceorder.service.ServiceCatalogService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Tenant-scoped API for the service catalog used by Fixi orders. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/services")
public class ServiceCatalogController {
    private final ServiceCatalogService service;

    /**
     * Lists active catalog entries for the authenticated tenant.
     *
     * @implSpec Overrides must preserve tenant scope and role authorization.
     * @return service catalog entries
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST','TECHNICIAN')")
    public List<ServiceCatalogEntry> list() {
        return service.list(tenantId());
    }

    /**
     * Creates a service catalog entry for the authenticated tenant.
     *
     * @implSpec Overrides must preserve tenant scope and role authorization.
     * @param request catalog values to create
     * @return the created catalog entry
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER')")
    public ServiceCatalogEntry create(@Valid @RequestBody final ServiceCatalogRequest request) {
        return service.create(tenantId(), request);
    }

    /**
     * Changes whether a service is available to new orders.
     *
     * @implSpec Overrides must preserve tenant-scoped service lookup.
     * @param id catalog entry identifier
     * @param value whether the entry should be active
     * @return the updated catalog entry
     */
    @PatchMapping("/{id}/active")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER')")
    public ServiceCatalogEntry setActive(
            @PathVariable final UUID id, @RequestParam final boolean value) {
        return service.setActive(tenantId(), id, value);
    }

    /**
     * Updates a service catalog entry for the authenticated tenant.
     *
     * @implSpec Overrides must preserve tenant scope and role authorization.
     * @param id catalog entry identifier
     * @param request replacement catalog values
     * @return the updated catalog entry
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER')")
    public ServiceCatalogEntry update(
            @PathVariable final UUID id, @Valid @RequestBody final ServiceCatalogRequest request) {
        return service.update(tenantId(), id, request);
    }

    private UUID tenantId() {
        return UUID.fromString(
                String.valueOf(
                        SecurityContextHolder.getContext().getAuthentication().getDetails()));
    }
}
