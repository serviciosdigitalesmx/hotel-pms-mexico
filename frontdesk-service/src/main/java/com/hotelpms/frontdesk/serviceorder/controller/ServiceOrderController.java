package com.hotelpms.frontdesk.serviceorder.controller;

import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrder;
import com.hotelpms.frontdesk.serviceorder.domain.RepairTest;
import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrderStatusHistory;
import com.hotelpms.frontdesk.serviceorder.dto.AuthorizationRequest;
import com.hotelpms.frontdesk.serviceorder.dto.QuotationUpdateRequest;
import com.hotelpms.frontdesk.serviceorder.dto.RepairTestRequest;
import com.hotelpms.frontdesk.serviceorder.dto.ServiceOrderDeliveryRequest;
import com.hotelpms.frontdesk.serviceorder.dto.ServiceOrderPaymentUpdateRequest;
import com.hotelpms.frontdesk.serviceorder.dto.ServiceOrderRequest;
import com.hotelpms.frontdesk.serviceorder.dto.ServiceOrderUpdateRequest;
import com.hotelpms.frontdesk.serviceorder.service.ServiceOrderService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/** Tenant-scoped API for service orders, quotations, payments, and delivery. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/service-orders")
public class ServiceOrderController {
    private static final String SERVICE_TEAM_ROLES =
            "hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST','TECHNICIAN')";
    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 200;
    private static final int INTERNAL_NOTE_MAX_LENGTH = 2000;

    private final ServiceOrderService service;

    /**
     * Reads the configured workflow for one tenant-scoped order.
     *
     * @implSpec Overrides must preserve tenant-scoped order lookup and role authorization.
     * @param id service order identifier
     * @return configured workflow state and transitions
     */
    @GetMapping("/{id}/configured-workflow")
    @PreAuthorize(SERVICE_TEAM_ROLES)
    public ServiceOrderService.ConfiguredWorkflowView configuredWorkflow(
            @PathVariable final UUID id) {
        return service.configuredWorkflow(hotel(), id);
    }

    /**
     * Advances the configured workflow after evaluating backend authorities.
     *
     * @implSpec Overrides must preserve tenant scoping and server-side permission evaluation.
     * @param id service order identifier
     * @param transitionKey configured transition key
     * @return updated workflow state
     */
    @PostMapping("/{id}/configured-workflow/transitions/{transitionKey}")
    @PreAuthorize(SERVICE_TEAM_ROLES)
    public ServiceOrderService.ConfiguredWorkflowView advanceConfiguredWorkflow(
            @PathVariable final UUID id, @PathVariable final String transitionKey) {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        return service.advanceConfiguredWorkflow(
                hotel(),
                id,
                transitionKey,
                authentication.getName(),
                authentication.getAuthorities().stream()
                        .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                        .toList());
    }

    /**
     * Creates a service order within the active tenant.
     *
     * @param r service order request
     * @return created service order response
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<ServiceOrder> create(@Valid @RequestBody final ServiceOrderRequest r) {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(hotel(), r, authentication.getName()));
    }

    /**
     * Gets one service order.
     *
     * @param id service order identifier
     * @return the service order
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'TECHNICIAN')")
    public ServiceOrder get(@PathVariable final UUID id) {
        return service.get(hotel(), id);
    }

    /**
     * Lists all service orders for the active tenant.
     *
     * @param page zero-based result page, optional
     * @param size maximum number of orders per page, optional
     * @return service orders
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'TECHNICIAN')")
    public ResponseEntity<?> listAll(
            @RequestParam(required = false) final Integer page,
            @RequestParam(required = false) final Integer size) {
        if (page == null && size == null) {
            return ResponseEntity.ok(service.listAll(hotel()));
        }
        final int safePage = Math.max(0, page == null ? 0 : page);
        final int safeSize =
                Math.min(MAX_PAGE_SIZE, Math.max(1, size == null ? DEFAULT_PAGE_SIZE : size));
        final Pageable pageable =
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(service.listAll(hotel(), pageable));
    }

    /**
     * Lists service orders for a customer.
     *
     * @param customerId customer identifier
     * @return matching service orders
     */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'TECHNICIAN')")
    public List<ServiceOrder> list(@PathVariable final UUID customerId) {
        return service.list(hotel(), customerId);
    }

    /**
     * Counts orders for the supplied tenant customers.
     *
     * @implSpec Overrides must not return counts for customers outside the authenticated tenant.
     * @param customerIds customer identifiers
     * @return order counts corresponding to the supplied customers
     */
    @GetMapping("/customer-counts")
    @PreAuthorize(SERVICE_TEAM_ROLES)
    public List<ServiceOrderService.CustomerOrderCount> customerOrderCounts(
            @RequestParam final List<UUID> customerIds) {
        return service.customerOrderCounts(hotel(), customerIds);
    }

    /**
     * Updates a service order.
     *
     * @param id service order identifier
     * @param r update request
     * @return the updated service order
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'TECHNICIAN')")
    public ServiceOrder update(
            @PathVariable final UUID id, @Valid @RequestBody final ServiceOrderUpdateRequest r) {
        return service.update(
                hotel(),
                id,
                r,
                SecurityContextHolder.getContext().getAuthentication().getName());
    }

    /**
     * Records customer authorization for a service order.
     *
     * @param id service order identifier
     * @param r authorization request
     * @return the authorized service order
     */
    @PostMapping("/{id}/authorize")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    public ServiceOrder authorize(
            @PathVariable final UUID id, @Valid @RequestBody final AuthorizationRequest r) {
        return service.authorize(
                hotel(),
                id,
                UUID.nameUUIDFromBytes(
                        SecurityContextHolder.getContext()
                                .getAuthentication()
                                .getName()
                                .getBytes(StandardCharsets.UTF_8)),
                r);
    }

    /**
     * Records a customer's rejection of a quotation.
     *
     * @implSpec Overrides must preserve tenant scoping and the quotation lifecycle transition.
     * @param id service order identifier
     * @param r authorization evidence
     * @return the rejected service order
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST')")
    public ServiceOrder reject(
            @PathVariable final UUID id, @Valid @RequestBody final AuthorizationRequest r) {
        return service.reject(
                hotel(),
                id,
                UUID.nameUUIDFromBytes(
                        SecurityContextHolder.getContext()
                                .getAuthentication()
                                .getName()
                                .getBytes(StandardCharsets.UTF_8)),
                r);
    }

    /**
     * Saves a quotation for a diagnosed service order.
     *
     * @implSpec Overrides must preserve tenant scoping and server-side quote validation.
     * @param id service order identifier
     * @param r quotation values
     * @return the updated service order
     */
    @PostMapping("/{id}/quotation")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','TECHNICIAN')")
    public ServiceOrder quote(
            @PathVariable final UUID id, @Valid @RequestBody final QuotationUpdateRequest r) {
        return service.quote(hotel(), id, r);
    }

    /**
     * Adds a repair verification result to an order.
     *
     * @implSpec Overrides must keep test outcomes attached to the tenant-scoped order.
     * @param id service order identifier
     * @param r repair verification data
     * @return the stored verification result
     */
    @PostMapping("/{id}/tests")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','TECHNICIAN')")
    public RepairTest addTest(
            @PathVariable final UUID id, @Valid @RequestBody final RepairTestRequest r) {
        return service.addTest(hotel(), id, r);
    }

    /**
     * Lists repair verification results for an order.
     *
     * @implSpec Overrides must preserve tenant-scoped order lookup.
     * @param id service order identifier
     * @return verification results in the order's history
     */
    @GetMapping("/{id}/tests")
    @PreAuthorize(SERVICE_TEAM_ROLES)
    public List<RepairTest> tests(
            @PathVariable final UUID id) {
        return service.tests(hotel(), id);
    }

    /**
     * Reads the status and note history for an order.
     *
     * @implSpec Overrides must preserve tenant-scoped order lookup.
     * @param id service order identifier
     * @return chronological order history
     */
    @GetMapping("/{id}/timeline")
    @PreAuthorize(SERVICE_TEAM_ROLES)
    public List<ServiceOrderStatusHistory>
            timeline(@PathVariable final UUID id) {
        return service.history(hotel(), id);
    }

    /**
     * Adds an internal note to an order's tenant-scoped history.
     *
     * @implSpec Overrides must preserve tenant scope and the authenticated actor identity.
     * @param id service order identifier
     * @param request validated note content
     * @return created history entry
     */
    @PostMapping("/{id}/notes")
    @PreAuthorize(SERVICE_TEAM_ROLES)
    public ResponseEntity<ServiceOrderStatusHistory>
            addInternalNote(
                    @PathVariable final UUID id,
                    @Valid @RequestBody final InternalNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        service.addInternalNote(
                                hotel(),
                                id,
                                SecurityContextHolder.getContext().getAuthentication().getName(),
                                request.note()));
    }

    /**
     * Delivers a service order and records any final payment.
     *
     * @implSpec Overrides must preserve the order's tenant scope and delivery/payment rules.
     * @param id service order identifier
     * @param request delivery and payment information
     * @return the delivered service order
     */
    @PostMapping("/{id}/deliver")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST')")
    public ServiceOrder deliver(
            @PathVariable final UUID id,
            @Valid @RequestBody final ServiceOrderDeliveryRequest request) {
        return service.deliver(
                hotel(),
                id,
                UUID.nameUUIDFromBytes(
                        SecurityContextHolder.getContext()
                                .getAuthentication()
                                .getName()
                                .getBytes(StandardCharsets.UTF_8)),
                request.paymentMethod());
    }

    /**
     * Records a payment against an order's outstanding balance.
     *
     * @implSpec Overrides must preserve tenant scope, idempotency, and outstanding-balance checks.
     * @param id service order identifier
     * @param request payment method, amount, and idempotency key
     * @return the order with its updated payment state
     */
    @PatchMapping("/{id}/payment")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST')")
    public ServiceOrder recordPendingPayment(
            @PathVariable final UUID id,
            @Valid @RequestBody final ServiceOrderPaymentUpdateRequest request) {
        return service.recordPendingPayment(
                hotel(),
                id,
                UUID.nameUUIDFromBytes(
                        SecurityContextHolder.getContext()
                                .getAuthentication()
                                .getName()
                                .getBytes(StandardCharsets.UTF_8)),
                request.paymentMethod(),
                request.amount(),
                request.requestId());
    }

    private UUID hotel() {
        return UUID.fromString(
                String.valueOf(
                        SecurityContextHolder.getContext().getAuthentication().getDetails()));
    }

    /**
     * Request body for adding an internal service-order note.
     *
     * @param note nonblank note text, limited to the supported note length
     */
    public record InternalNoteRequest(
            @jakarta.validation.constraints.NotBlank
                    @jakarta.validation.constraints.Size(max = INTERNAL_NOTE_MAX_LENGTH)
                    String note) { }
}
