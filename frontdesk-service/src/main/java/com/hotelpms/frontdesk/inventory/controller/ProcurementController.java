package com.hotelpms.frontdesk.inventory.controller;

import com.hotelpms.frontdesk.inventory.domain.PurchaseOrder;
import com.hotelpms.frontdesk.inventory.domain.Supplier;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.PurchaseRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.PurchaseRequestWithLines;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.SupplierRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.PurchaseReceiptRequest;
import com.hotelpms.frontdesk.inventory.service.ProcurementService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

/** Type domain type. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/procurement")
public class ProcurementController {
  private final ProcurementService service;
  /**
   * Executes the operation.
   *
   * @param r parameter.
   *
   * @return operation result.
   */

  @PostMapping("/suppliers")
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER')")
  public Supplier supplier(@Valid @RequestBody final SupplierRequest r) {
    return service.supplier(hotel(), r);
  }

  @PutMapping("/suppliers/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER')")
  public Supplier updateSupplier(@PathVariable final UUID id, @Valid @RequestBody final SupplierRequest r) {
    return service.updateSupplier(hotel(), id, r);
  }
  /**
   * Executes the operation.
   *
   * @return operation result.
   */

  @GetMapping("/suppliers")
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST','TECHNICIAN')")
  public List<Supplier> suppliers() {
    return service.suppliers(hotel());
  }
  /**
   * Executes the operation.
   *
   * @param r parameter.
   *
   * @return operation result.
   */

  @PostMapping("/purchases")
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER')")
  public PurchaseOrder purchase(@Valid @RequestBody final PurchaseRequest r) {
    return service.purchase(hotel(), r);
  }

  @PostMapping("/purchases/with-lines")
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER')")
  public PurchaseOrder purchaseWithLines(@Valid @RequestBody final PurchaseRequestWithLines r) {
    return service.purchase(hotel(), new PurchaseRequest(r.branchId(), r.supplierId(), r.serviceOrderId(), r.lines()));
  }
  /**
   * Executes the operation.
   *
   * @return operation result.
   */

  @GetMapping("/purchases")
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST','TECHNICIAN')")
  public List<PurchaseOrder> purchases() {
    return service.purchases(hotel());
  }
  @PostMapping("/purchases/{id}/receive") @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER')")
  public PurchaseOrder receive(@PathVariable UUID id,@Valid @RequestBody PurchaseReceiptRequest request){return service.receive(hotel(),id,request);}

  private UUID hotel() {
    return UUID.fromString(
        String.valueOf(SecurityContextHolder.getContext().getAuthentication().getDetails()));
  }
}
