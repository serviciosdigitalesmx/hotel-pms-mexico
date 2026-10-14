package com.hotelpms.frontdesk.inventory.controller;

import com.hotelpms.frontdesk.inventory.domain.InventoryBalance;
import com.hotelpms.frontdesk.inventory.domain.InventoryProduct;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.ImportResult;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.MovementRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.ProductRequest;
import com.hotelpms.frontdesk.inventory.service.InventoryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/** Tenant-scoped inventory catalog and stock endpoints. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inventory")
public class InventoryController {
  private final InventoryService service;

  /**
   * Creates a catalog product.
   *
   * @param request product data
   *
   * @return persisted product
   */
  @PostMapping("/products")
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
  public InventoryProduct product(@Valid @RequestBody final ProductRequest request) {
    return service.product(hotel(), request);
  }

  @PutMapping("/products/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
  public InventoryProduct updateProduct(
      @PathVariable final UUID id, @Valid @RequestBody final ProductRequest request) {
    return service.updateProduct(hotel(), id, request);
  }

  /**
   * Imports CSV catalog rows.
   *
   * @param csv CSV payload
   *
   * @return import result
   */
  @PostMapping(value = "/products/import", consumes = "text/csv")
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
  public ImportResult importCsv(@RequestBody final String csv) {
    return service.importCsv(hotel(), csv);
  }

  /** Imports an XLSX supplier catalog. */
  @PostMapping(value = "/products/import/xlsx", consumes = "multipart/form-data")
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
  public ImportResult importXlsx(@RequestPart("file") final MultipartFile file) {
    return service.importXlsx(hotel(), file);
  }

  /** Imports a PDF supplier catalog using extracted text. */
  @PostMapping(value = "/products/import/pdf", consumes = "multipart/form-data")
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
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
   *
   * @return updated branch balance
   */
  @PostMapping("/movements")
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
  public InventoryBalance move(@Valid @RequestBody final MovementRequest request) {
    return service.move(hotel(), request);
  }

  /**
   * Lists branch stock.
   *
   * @param branch branch identifier
   *
   * @return branch balances
   */
  @GetMapping("/branches/{branch}/stock")
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'RECEPTIONIST', 'TECHNICIAN')")
  public List<InventoryBalance> stock(@PathVariable final UUID branch) {
    return service.stock(hotel(), branch);
  }

  private UUID hotel() {
    return UUID.fromString(
        String.valueOf(SecurityContextHolder.getContext().getAuthentication().getDetails()));
  }
}
