package com.hotelpms.frontdesk.inventory.controller;

import com.hotelpms.frontdesk.inventory.domain.StockReservation;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.ProductConsumption;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.ReservationRequest;
import com.hotelpms.frontdesk.inventory.service.InventoryService;
import com.hotelpms.frontdesk.serviceorder.security.FixiAssignedOrderAccess;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Tenant-authorized stock reservation and consumption reporting endpoints. */
@RestController("inventoryReservationController")
@RequiredArgsConstructor
@RequestMapping("/api/v1/inventory/reservations")
public class ReservationController {
    private final InventoryService service;
    private final FixiAssignedOrderAccess orderAccess;
    @Value("${fixi.authorization.assigned-orders.enabled:false}")
    private boolean assignedOrderScopeEnabled;

    /**
     * Reserves stock for a service order.
     *
     * @param r reservation request
     * @return stock reservation
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','TECHNICIAN')")
    public StockReservation reserve(@Valid @RequestBody final ReservationRequest r) {
        requireOrderAccess(r.serviceOrderId());
        return service.reserve(hotel(), r);
    }

    /**
     * Lists reservations for a tenant-owned service order.
     *
     * @param order service order identifier
     * @return reservations for the order
     * @implSpec Overrides must preserve tenant scoping and role authorization.
     */
    @GetMapping("/order/{order}")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','TECHNICIAN')")
    public List<StockReservation> listForOrder(@PathVariable final UUID order) {
        requireOrderAccess(order);
        return service.reservationsForOrder(hotel(), order);
    }

    /**
     * Reports the most-consumed products for the selected tenant and date range.
     *
     * @param branchId optional branch filter
     * @param from first date in the range
     * @param to last date in the range
     * @return product consumption totals
     * @implSpec Overrides must preserve tenant scoping and branch authorization.
     */
    @GetMapping("/consumption/top")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER')")
    public List<ProductConsumption> topConsumedProducts(
            @RequestParam(required = false) final UUID branchId,
            @RequestParam final LocalDate from,
            @RequestParam final LocalDate to) {
        return service.topConsumedProducts(hotel(), branchId, from, to);
    }

    /**
     * Releases a stock reservation.
     *
     * @param order service order identifier
     * @param product product identifier
     * @return released reservation
     */
    @PostMapping("/{order}/{product}/release")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','TECHNICIAN')")
    public StockReservation release(
            @PathVariable final UUID order, @PathVariable final UUID product) {
        requireOrderAccess(order);
        return service.release(hotel(), order, product);
    }

    private void requireOrderAccess(final UUID orderId) {
        if (!assignedOrderScopeEnabled) {
            return;
        }
        final Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        final Object requestAttributes = RequestContextHolder.getRequestAttributes();
        final Object selected = requestAttributes instanceof ServletRequestAttributes attributes
                ? attributes.getRequest().getAttribute("fixi.authenticatedBranchId")
                : null;
        final UUID branchId = selected == null ? null : UUID.fromString(selected.toString());
        if (authentication == null
                || !orderAccess.canAccessOrder(
                        authentication.getName(), hotel(), orderId, branchId)) {
            throw new AccessDeniedException("SERVICE_ORDER_NOT_FOUND");
        }
    }

    private UUID hotel() {
        return UUID.fromString(
                String.valueOf(
                        SecurityContextHolder.getContext().getAuthentication().getDetails()));
    }
}
