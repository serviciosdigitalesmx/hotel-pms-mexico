package com.hotelpms.frontdesk.inventory.service;

import com.hotelpms.frontdesk.inventory.domain.PurchaseOrder;
import com.hotelpms.frontdesk.inventory.domain.PurchaseOrderLine;
import com.hotelpms.frontdesk.inventory.domain.InventoryProduct;
import com.hotelpms.frontdesk.inventory.domain.Supplier;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.PurchaseRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.SupplierRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.PurchaseReceiptRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.PurchaseLineRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.MovementRequest;
import com.hotelpms.frontdesk.inventory.repository.PurchaseOrderRepository;
import com.hotelpms.frontdesk.inventory.repository.InventoryProductRepository;
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
  private final InventoryProductRepository products;
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
    final PurchaseOrder order = PurchaseOrder.builder()
            .hotelId(hotel)
            .branchId(r.branchId())
            .supplierId(r.supplierId())
            .serviceOrderId(r.serviceOrderId())
            .createdAt(LocalDateTime.now())
            .build();
    for (final PurchaseLineRequest requestedLine : r.lines()) {
      final InventoryProduct product = products.findById(requestedLine.productId())
          .filter(item -> hotel.equals(item.getHotelId()))
          .orElseThrow(() -> new IllegalArgumentException("PRODUCT_NOT_FOUND"));
      if (product.getSupplierId() != null && !r.supplierId().equals(product.getSupplierId())) {
        throw new IllegalArgumentException("PURCHASE_PRODUCT_SUPPLIER_MISMATCH");
      }
      final PurchaseOrderLine line = new PurchaseOrderLine();
      line.setPurchaseOrder(order);
      line.setProductId(product.getId());
      line.setQuantityOrdered(requestedLine.quantity());
      line.setQuantityReceived(0);
      line.setUnitCost(requestedLine.unitCost());
      order.getLines().add(line);
    }
    if (order.getLines().isEmpty()) {
      throw new IllegalArgumentException("PURCHASE_LINES_REQUIRED");
    }
    return purchases.save(order);
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
  public PurchaseOrder receive(UUID hotel,UUID purchaseId,PurchaseReceiptRequest request){
    var purchase=purchases.findByIdAndHotelId(purchaseId,hotel)
        .orElseThrow(()->new IllegalArgumentException("PURCHASE_NOT_FOUND"));
    if("RECEIVED".equals(purchase.getStatus()))throw new IllegalStateException("PURCHASE_ALREADY_RECEIVED");
    if (purchase.getLines() == null || purchase.getLines().isEmpty()) {
      throw new IllegalStateException("LEGACY_PURCHASE_HAS_NO_ORDER_LINES");
    }
    if (request.lines().isEmpty()) throw new IllegalArgumentException("RECEIPT_LINES_REQUIRED");
    var receivedProducts = new java.util.HashSet<UUID>();
    var receiptLines = new java.util.ArrayList<java.util.Map.Entry<PurchaseOrderLine, Long>>();
    for (var received : request.lines()) {
      if (!receivedProducts.add(received.productId())) {
        throw new IllegalArgumentException("DUPLICATE_PRODUCT_ON_RECEIPT");
      }
      var line = purchase.getLines().stream().filter(item -> item.getProductId().equals(received.productId()))
          .findFirst().orElseThrow(() -> new IllegalArgumentException("PRODUCT_NOT_ON_PURCHASE"));
      if (received.quantity() > line.getQuantityOrdered() - line.getQuantityReceived()) {
        throw new IllegalArgumentException("RECEIPT_EXCEEDS_ORDERED_QUANTITY");
      }
      receiptLines.add(java.util.Map.entry(line, received.quantity()));
    }
    for (var receipt : receiptLines) {
      var line = receipt.getKey();
      var product = products.findByIdAndHotelId(line.getProductId(), hotel)
          .orElseThrow(() -> new IllegalArgumentException("PRODUCT_NOT_FOUND"));
      inventory.move(hotel, new MovementRequest(purchase.getBranchId(), line.getProductId(), receipt.getValue()));
      product.setCost(line.getUnitCost());
      if (product.getSupplierId() == null) {
        product.setSupplierId(purchase.getSupplierId());
      }
      products.save(product);
      line.setQuantityReceived(line.getQuantityReceived() + receipt.getValue());
    }
    boolean complete = purchase.getLines().stream()
        .allMatch(line -> line.getQuantityReceived() >= line.getQuantityOrdered());
    boolean anyReceived = purchase.getLines().stream().anyMatch(line -> line.getQuantityReceived() > 0);
    purchase.setStatus(complete ? "RECEIVED" : anyReceived ? "PARTIALLY_RECEIVED" : "PENDING");
    return purchases.save(purchase);
  }
}
