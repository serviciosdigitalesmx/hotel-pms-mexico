package com.hotelpms.frontdesk.serviceorder.controller;

import com.hotelpms.frontdesk.serviceorder.domain.ServiceRequest;
import com.hotelpms.frontdesk.serviceorder.dto.ServiceRequestDto;
import com.hotelpms.frontdesk.serviceorder.service.ServiceRequestService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Type domain type. */
@RestController
@RequiredArgsConstructor
public class ServiceRequestController {
    private final ServiceRequestService service;

    /**
     * Creates a public service request.
     *
     * @param hotel tenant identifier
     * @param r request payload
     * @return created request
     */
    @PostMapping("/api/v1/public/requests/{hotel}")
    public ResponseEntity<ServiceRequest> create(
            @PathVariable final UUID hotel, @Valid @RequestBody final ServiceRequestDto r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(hotel, r));
    }

    /**
     * Lists service requests for the active tenant.
     *
     * @return service requests
     */
    @GetMapping("/api/v1/service-requests")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST','TECHNICIAN')")
    public List<ServiceRequest> list() {
        return service.list(hotel());
    }

    /**
     * Moves a request into review.
     *
     * @param id request identifier
     * @return reviewed request
     */
    @PostMapping("/api/v1/service-requests/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST')")
    public ServiceRequest review(@PathVariable final UUID id) {
        return service.review(hotel(), id);
    }

    private UUID hotel() {
        return UUID.fromString(
                String.valueOf(
                        SecurityContextHolder.getContext().getAuthentication().getDetails()));
    }
}
