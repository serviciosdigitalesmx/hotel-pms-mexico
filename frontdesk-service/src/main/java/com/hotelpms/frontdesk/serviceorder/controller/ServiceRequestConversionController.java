package com.hotelpms.frontdesk.serviceorder.controller;

import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrder;
import com.hotelpms.frontdesk.serviceorder.dto.ServiceRequestConversion;
import com.hotelpms.frontdesk.serviceorder.service.ServiceRequestConversionService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Type domain type. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/service-requests")
public class ServiceRequestConversionController {
    private final ServiceRequestConversionService service;

    /**
     * Executes the operation.
     *
     * @param id parameter.
     * @param c parameter.
     * @return operation result.
     */
    @PostMapping("/{id}/convert")
    @PreAuthorize("hasAuthority('orders.write')")
    public ServiceOrder convert(
            @PathVariable final UUID id, @Valid @RequestBody final ServiceRequestConversion c) {
        return service.convert(hotel(), id, c);
    }

    private UUID hotel() {
        return UUID.fromString(
                String.valueOf(
                        SecurityContextHolder.getContext().getAuthentication().getDetails()));
    }
}
