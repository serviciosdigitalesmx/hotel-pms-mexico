package com.hotelpms.frontdesk.serviceorder.controller;

import com.hotelpms.frontdesk.serviceorder.domain.WarrantyClaim;
import com.hotelpms.frontdesk.serviceorder.dto.WarrantyClaimRequest;
import com.hotelpms.frontdesk.serviceorder.service.WarrantyClaimService;
import com.hotelpms.frontdesk.serviceorder.security.FixiAssignedOrderAccess;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Type domain type. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/warranty-claims")
public class WarrantyClaimController {
    private static final String SERVICE_TEAM_ROLES =
            "hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST')";
    private final WarrantyClaimService service;
    private final FixiAssignedOrderAccess orderAccess;
    @Value("${fixi.authorization.assigned-orders.enabled:false}")
    private boolean branchScopeEnabled;

    /**
     * Opens a warranty claim.
     *
     * @param r warranty claim request
     * @return created warranty claim
     */
    @PostMapping
    @PreAuthorize(SERVICE_TEAM_ROLES)
    public ResponseEntity<WarrantyClaim> open(@Valid @RequestBody final WarrantyClaimRequest r) {
        requireOrderBranchAccess(r.serviceOrderId());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.open(hotel(), r));
    }

    /**
     * Lists warranty claims for a customer.
     *
     * @param customer customer identifier
     * @return matching warranty claims
     */
    @GetMapping("/customer/{customer}")
    @PreAuthorize(SERVICE_TEAM_ROLES)
    public List<WarrantyClaim> list(@PathVariable final UUID customer) {
        return service.listForBranches(hotel(), customer, branchIds());
    }

    /**
     * Lists claims attached to one tenant-owned repair order.
     *
     * @param serviceOrderId repair order identifier
     * @return warranty claims associated with the order
     * @implSpec Overrides must preserve the active tenant scope.
     */
    @GetMapping("/service-order/{serviceOrderId}")
    @PreAuthorize(SERVICE_TEAM_ROLES)
    public List<WarrantyClaim> listByServiceOrder(@PathVariable final UUID serviceOrderId) {
        return service.listByServiceOrder(hotel(), serviceOrderId);
    }

    /**
     * Closes a warranty claim.
     *
     * @param id warranty claim identifier
     * @return closed warranty claim
     */
    @PostMapping("/{id}/close")
    @PreAuthorize(SERVICE_TEAM_ROLES)
    public WarrantyClaim close(@PathVariable final UUID id) {
        return service.close(hotel(), id);
    }

    private UUID hotel() {
        return UUID.fromString(
                String.valueOf(
                        SecurityContextHolder.getContext().getAuthentication().getDetails()));
    }

    private Optional<List<UUID>> branchIds() {
        if (!branchScopeEnabled) {
            return Optional.empty();
        }
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_OWNER".equals(authority.getAuthority())
                        || "ROLE_ADMIN".equals(authority.getAuthority()))) {
            return Optional.empty();
        }
        final Object selected = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()
                instanceof org.springframework.web.context.request.ServletRequestAttributes attributes
                ? attributes.getRequest().getAttribute("fixi.authenticatedBranchId") : null;
        if (selected != null) {
            final UUID branchId = UUID.fromString(selected.toString());
            return Optional.of(orderAccess.canAccessBranch(authentication.getName(), hotel(), branchId)
                    ? List.of(branchId) : List.of());
        }
        return Optional.of(orderAccess.branchIds(authentication.getName(), hotel()));
    }

    private void requireOrderBranchAccess(final UUID orderId) {
        if (!branchScopeEnabled) {
            return;
        }
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final Object selected = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()
                instanceof org.springframework.web.context.request.ServletRequestAttributes attributes
                ? attributes.getRequest().getAttribute("fixi.authenticatedBranchId") : null;
        final UUID branchId = selected == null ? null : UUID.fromString(selected.toString());
        if (authentication == null || !orderAccess.canAccessOrder(
                authentication.getName(), hotel(), orderId, branchId)) {
            throw new org.springframework.security.access.AccessDeniedException("SERVICE_ORDER_NOT_FOUND");
        }
    }
}
