package com.hotelpms.frontdesk.inventory.controller;

import com.hotelpms.frontdesk.inventory.domain.InventoryBalance;
import com.hotelpms.frontdesk.inventory.domain.InventoryProduct;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.BulkPriceRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.ImportResult;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.MovementRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.ProductRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.StockCountRequest;
import com.hotelpms.frontdesk.inventory.service.InventoryService;
import com.hotelpms.frontdesk.inventory.service.InventoryService.PosConsumptionLine;
import com.hotelpms.frontdesk.util.ImmutableCopies;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Tenant-scoped inventory catalog and stock endpoints.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inventory")
public class InventoryController {
    private static final String MANAGER_ROLES = "hasAnyRole('ADMIN', 'OWNER', 'MANAGER')";
    private final InventoryService service;

    /**
     * Creates a catalog product.
     *
     * @param request product data
     * @return persisted product
     */
    @PostMapping("/products")
    @PreAuthorize(MANAGER_ROLES)
    public InventoryProduct product(@Valid @RequestBody final ProductRequest request) {
        return service.product(hotel(), request);
    }

    /**
     * Updates catalog fields for one tenant-owned inventory product.
     *
     * @param id product identifier
     * @param request updated product data
     * @return persisted product
     * @implSpec Overrides must preserve tenant ownership and branch stock.
     */
    @PutMapping("/products/{id}")
    @PreAuthorize(MANAGER_ROLES)
    public InventoryProduct updateProduct(
            @PathVariable final UUID id, @Valid @RequestBody final ProductRequest request) {
        return service.updateProduct(hotel(), id, request);
    }

    /**
     * Applies a bounded sale-price increase to selected tenant-owned products.
     *
     * @param request selected products and percentage
     * @return updated products
     * @implSpec Overrides must preserve tenant scoping and request limits.
     */
    @PostMapping("/products/bulk-price")
    @PreAuthorize(MANAGER_ROLES)
    public List<InventoryProduct> increasePrices(
            @Valid @RequestBody final BulkPriceRequest request) {
        return service.increasePrices(hotel(), request.productIds(), request.percentage());
    }

    /**
     * Imports CSV catalog rows.
     *
     * @param csv CSV payload
     * @param branchId optional branch for initial quantities
     * @return import result
     */
    @PostMapping(value = "/products/import", consumes = "text/csv")
    @PreAuthorize(MANAGER_ROLES)
    public ImportResult importCsv(
            @RequestBody final String csv, @RequestParam(required = false) final UUID branchId) {
        return service.importCsv(hotel(), csv, branchId);
    }

    /**
     * Imports an XLSX supplier catalog.
     *
     * @param file uploaded workbook
     * @return import result
     */
    @PostMapping(value = "/products/import/xlsx", consumes = "multipart/form-data")
    @PreAuthorize(MANAGER_ROLES)
    public ImportResult importXlsx(@RequestPart("file") final MultipartFile file) {
        return service.importXlsx(hotel(), file);
    }

    /**
     * Imports a PDF supplier catalog using extracted text.
     *
     * @param file uploaded PDF
     * @return import result
     */
    @PostMapping(value = "/products/import/pdf", consumes = "multipart/form-data")
    @PreAuthorize(MANAGER_ROLES)
    public ImportResult importPdf(@RequestPart("file") final MultipartFile file) {
        return service.importPdf(hotel(), file);
    }

    /**
     * Lists products owned by the current tenant.
     *
     * @return tenant products
     */
    @GetMapping("/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'TECHNICIAN')")
    public List<InventoryProduct> products() {
        return service.products(hotel());
    }

    /**
     * Applies a stock movement.
     *
     * @param request movement data
     * @return updated branch balance
     */
    @PostMapping("/movements")
    @PreAuthorize(MANAGER_ROLES)
    public InventoryBalance move(@Valid @RequestBody final MovementRequest request) {
        return service.move(hotel(), request);
    }

    /**
     * Idempotently deducts linked products for a confirmed POS order.
     *
     * @param orderId POS order identifier
     * @param request stock branch and product quantities
     * @implSpec Overrides must preserve tenant/branch checks and replay idempotency.
     */
    @PostMapping("/pos/orders/{orderId}/consume")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST')")
    public void consumePosOrder(
            @PathVariable final UUID orderId, @RequestBody final PosConsumptionRequest request) {
        service.consumePosOrder(hotel(), orderId, request.branchId(), request.lines());
    }

    /**
     * Reconciles a branch balance with a physical stock count.
     *
     * @param request branch, product, and counted quantity
     * @return persisted branch balance
     */
    @PostMapping("/stock-counts")
    @PreAuthorize(MANAGER_ROLES)
    public InventoryBalance count(@Valid @RequestBody final StockCountRequest request) {
        return service.count(hotel(), request);
    }

    /**
     * Lists branch stock.
     *
     * @param branch branch identifier
     * @return branch balances
     */
    @GetMapping("/branches/{branch}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'TECHNICIAN')")
    public List<InventoryBalance> stock(@PathVariable final UUID branch) {
        return service.stock(hotel(), branch);
    }

    private UUID hotel() {
        return UUID.fromString(
                String.valueOf(
                        SecurityContextHolder.getContext().getAuthentication().getDetails()));
    }

    /**
     * Product quantities and stock branch for a confirmed POS order.
     *
     * @param branchId branch that owns the stock
     * @param lines tenant-owned product quantities to deduct
     */
    public record PosConsumptionRequest(UUID branchId, List<PosConsumptionLine> lines) {
        public PosConsumptionRequest {
            lines = ImmutableCopies.list(lines);
        }
    }
}
