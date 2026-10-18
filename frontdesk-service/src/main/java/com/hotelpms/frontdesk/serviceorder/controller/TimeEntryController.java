package com.hotelpms.frontdesk.serviceorder.controller;

import com.hotelpms.frontdesk.serviceorder.domain.TimeEntry;
import com.hotelpms.frontdesk.serviceorder.dto.TimeEntryRequest;
import com.hotelpms.frontdesk.serviceorder.service.TimeEntryService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Type domain type. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/service-orders")
public class TimeEntryController {
    private final TimeEntryService service;

    /**
     * Adds time to a service order.
     *
     * @param order service order identifier
     * @param r time entry request
     * @return created time entry
     */
    @PostMapping("/{order}/time")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','TECHNICIAN')")
    public TimeEntry add(
            @PathVariable final UUID order, @Valid @RequestBody final TimeEntryRequest r) {
        return service.add(hotel(), order, r);
    }

    /**
     * Lists time entries for a service order.
     *
     * @param order service order identifier
     * @return time entries
     */
    @GetMapping("/{order}/time")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST','TECHNICIAN')")
    public List<TimeEntry> list(@PathVariable final UUID order) {
        return service.list(hotel(), order);
    }

    private UUID hotel() {
        return UUID.fromString(
                String.valueOf(
                        SecurityContextHolder.getContext().getAuthentication().getDetails()));
    }
}
