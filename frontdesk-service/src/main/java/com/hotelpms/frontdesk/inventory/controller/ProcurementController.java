package com.hotelpms.frontdesk.inventory.controller;

import com.hotelpms.frontdesk.inventory.domain.PurchaseOrder;
import com.hotelpms.frontdesk.inventory.domain.Supplier;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.PurchaseReceiptCorrectionRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.PurchaseReceiptRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.PurchaseRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.PurchaseRequestWithLines;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.SupplierRequest;
import com.hotelpms.frontdesk.inventory.service.ProcurementService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Tenant-authorized supplier and purchase-order API for Fixi procurement.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/procurement")
public class ProcurementController {
    private static final String PROCUREMENT_WRITE_ROLES =
            "hasAnyRole('ADMIN','OWNER','MANAGER')";
    private final ProcurementService service;

    /**
     * Creates a supplier for the authenticated tenant.
     *
     * @param r validated supplier details
     * @return persisted supplier
     * @implSpec Overrides must retain the role check and authenticated tenant context.
     */
    @PostMapping("/suppliers")
    @PreAuthorize(PROCUREMENT_WRITE_ROLES)
    public Supplier supplier(@Valid @RequestBody final SupplierRequest r) {
        return service.supplier(hotel(), r);
    }

    /**
     * Updates a supplier without changing its purchase history.
     *
     * @param id tenant-owned supplier identifier
     * @param r updated supplier details
     * @return persisted supplier
     * @implSpec Overrides must retain backend role checks and tenant scoping.
     */
    @PutMapping("/suppliers/{id}")
    @PreAuthorize(PROCUREMENT_WRITE_ROLES)
    public Supplier updateSupplier(
            @PathVariable final UUID id, @Valid @RequestBody final SupplierRequest r) {
        return service.updateSupplier(hotel(), id, r);
    }

    /**
     * Activates or archives a supplier for future purchases.
     *
     * @param id tenant-owned supplier identifier
     * @param value requested availability
     * @return persisted supplier
     * @implSpec Overrides must retain backend role checks and tenant scoping.
     */
    @PatchMapping("/suppliers/{id}/active")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER')")
    public Supplier updateSupplierActive(
            @PathVariable final UUID id,
            @RequestParam final boolean value) {
        return service.updateSupplierActive(hotel(), id, value);
    }

    /**
     * Lists suppliers visible to the authenticated tenant.
     *
     * @return tenant-owned suppliers
     * @implSpec Overrides must retain authenticated tenant scoping.
     */
    @GetMapping("/suppliers")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST','TECHNICIAN')")
    public List<Supplier> suppliers() {
        return service.suppliers(hotel());
    }

    /**
     * Creates a purchase order for the authenticated tenant.
     *
     * @param r validated purchase details
     * @return persisted purchase order
     * @implSpec Overrides must retain role, tenant, and branch validation.
     */
    @PostMapping("/purchases")
    @PreAuthorize(PROCUREMENT_WRITE_ROLES)
    public PurchaseOrder purchase(@Valid @RequestBody final PurchaseRequest r) {
        return service.purchase(hotel(), r);
    }

    /**
     * Creates a purchase order with its requested lines in one request.
     *
     * @param r purchase header and lines
     * @return persisted purchase order
     * @implSpec Overrides must retain backend role checks and tenant/branch validation.
     */
    @PostMapping("/purchases/with-lines")
    @PreAuthorize(PROCUREMENT_WRITE_ROLES)
    public PurchaseOrder purchaseWithLines(@Valid @RequestBody final PurchaseRequestWithLines r) {
        return service.purchase(
                hotel(),
                new PurchaseRequest(r.branchId(), r.supplierId(), r.serviceOrderId(), r.lines()));
    }

    /**
     * Lists purchase orders visible to the authenticated tenant.
     *
     * @return tenant-owned purchase orders
     * @implSpec Overrides must retain authenticated tenant scoping.
     */
    @GetMapping("/purchases")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST','TECHNICIAN')")
    public List<PurchaseOrder> purchases() {
        return service.purchases(hotel());
    }

    /**
     * Records received stock idempotently for a purchase order.
     *
     * @param id purchase-order identifier
     * @param request received product quantities
     * @return updated purchase order
     * @implSpec Overrides must retain backend role checks and tenant-scoped inventory effects.
     */
    @PostMapping("/purchases/{id}/receive")
    @PreAuthorize(PROCUREMENT_WRITE_ROLES)
    public PurchaseOrder receive(
            @PathVariable final UUID id,
            @Valid @RequestBody final PurchaseReceiptRequest request) {
        return service.receive(hotel(), id, request);
    }

    /**
     * Corrects quantities recorded in a prior supplier receipt.
     *
     * @param id purchase-order identifier
     * @param request corrected cumulative quantities
     * @return updated purchase order
     * @implSpec Overrides must retain backend role checks and stock-reservation safeguards.
     */
    @PatchMapping("/purchases/{id}/receipt")
    @PreAuthorize(PROCUREMENT_WRITE_ROLES)
    public PurchaseOrder correctReceipt(
            @PathVariable final UUID id,
            @Valid @RequestBody final PurchaseReceiptCorrectionRequest request) {
        return service.correctReceipt(hotel(), id, request);
    }

    /**
     * Cancels a purchase order that has not received stock.
     *
     * @param id purchase-order identifier
     * @return cancelled purchase order
     * @implSpec Overrides must retain backend role checks and tenant-scoped lookup.
     */
    @PostMapping("/purchases/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER')")
    public PurchaseOrder cancel(@PathVariable final UUID id) {
        return service.cancel(hotel(), id);
    }

    private UUID hotel() {
        return UUID.fromString(
                String.valueOf(
                        SecurityContextHolder.getContext().getAuthentication().getDetails()));
    }
}
