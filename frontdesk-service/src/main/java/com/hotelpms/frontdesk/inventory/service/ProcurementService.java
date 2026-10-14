package com.hotelpms.frontdesk.inventory.service;

import com.hotelpms.frontdesk.inventory.domain.PurchaseOrder;
import com.hotelpms.frontdesk.inventory.domain.Supplier;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.PurchaseRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.SupplierRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.PurchaseReceiptRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.MovementRequest;
import com.hotelpms.frontdesk.inventory.repository.PurchaseOrderRepository;
import com.hotelpms.frontdesk.inventory.repository.SupplierRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Type domain type. */
@Service
@RequiredArgsConstructor
public class ProcurementService {
  private final SupplierRepository suppliers;
  private final PurchaseOrderRepository purchases;
  private final InventoryService inventory;
  /**
   * Executes the operation.
   *
   * @param hotel parameter.
   *
   * @param r parameter.
   *
   * @return operation result.
   */

  @Transactional
  public Supplier supplier(final UUID hotel, final SupplierRequest r) {
    final Supplier s = new Supplier();
    s.setHotelId(hotel);
    s.setName(r.name().trim());
    s.setTaxId(r.taxId());
    s.setContact(r.contact() != null ? r.contact() : r.email());
    s.setContactName(r.contactName());
    s.setEmail(r.email());
    s.setPhone(r.phone());
    s.setWebsite(r.website());
    s.setDescription(r.description());
    return suppliers.save(s);
  }

  @Transactional
  public Supplier updateSupplier(final UUID hotel, final UUID supplierId, final SupplierRequest r) {
    final Supplier supplier = suppliers.findByIdAndHotelId(supplierId, hotel)
        .orElseThrow(() -> new IllegalArgumentException("SUPPLIER_NOT_FOUND"));
    supplier.setName(r.name().trim());
    supplier.setTaxId(r.taxId());
    supplier.setContact(r.contact() != null ? r.contact() : r.email());
    supplier.setContactName(r.contactName());
    supplier.setEmail(r.email());
    supplier.setPhone(r.phone());
    supplier.setWebsite(r.website());
    supplier.setDescription(r.description());
    return suppliers.save(supplier);
  }
  /**
   * Executes the operation.
   *
   * @param hotel parameter.
   *
   * @param r parameter.
   *
   * @return operation result.
   */

  @Transactional
  public PurchaseOrder purchase(final UUID hotel, final PurchaseRequest r) {
    if (suppliers.findByIdAndHotelId(r.supplierId(), hotel).isEmpty()) {
      throw new IllegalArgumentException("SUPPLIER_NOT_FOUND");
    }
    return purchases.save(
        PurchaseOrder.builder()
            .hotelId(hotel)
            .branchId(r.branchId())
            .supplierId(r.supplierId())
            .serviceOrderId(r.serviceOrderId())
            .createdAt(LocalDateTime.now())
            .build());
  }
  /**
   * Executes the operation.
   *
   * @param hotel parameter.
   *
   * @return operation result.
   */

  @Transactional(readOnly = true)
  public List<Supplier> suppliers(final UUID hotel) {
    return suppliers.findAllByHotelId(hotel);
  }
  /**
   * Executes the operation.
   *
   * @param hotel parameter.
   *
   * @return operation result.
   */

  @Transactional(readOnly = true)
  public List<PurchaseOrder> purchases(final UUID hotel) {
    return purchases.findAllByHotelIdOrderByCreatedAtDesc(hotel);
  }
  @Transactional
  public PurchaseOrder receive(UUID hotel,UUID purchaseId,PurchaseReceiptRequest request){var purchase=purchases.findById(purchaseId).filter(p->hotel.equals(p.getHotelId())).orElseThrow(()->new IllegalArgumentException("PURCHASE_NOT_FOUND"));if("RECEIVED".equals(purchase.getStatus()))return purchase;request.lines().forEach(line->inventory.move(hotel,new MovementRequest(purchase.getBranchId(),line.productId(),line.quantity())));purchase.setStatus("RECEIVED");return purchases.save(purchase);}
}
