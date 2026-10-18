package com.hotelpms.frontdesk.inventory.service;

import com.hotelpms.frontdesk.client.BranchValidationService;
import com.hotelpms.frontdesk.inventory.domain.InventoryBalance;
import com.hotelpms.frontdesk.inventory.domain.InventoryProduct;
import com.hotelpms.frontdesk.inventory.domain.PurchaseOrder;
import com.hotelpms.frontdesk.inventory.domain.PurchaseOrderLine;
import com.hotelpms.frontdesk.inventory.domain.Supplier;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.MovementRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.PurchaseLineRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.PurchaseReceiptCorrectionRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.PurchaseReceiptRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.PurchaseRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.SupplierRequest;
import com.hotelpms.frontdesk.inventory.repository.InventoryBalanceRepository;
import com.hotelpms.frontdesk.inventory.repository.InventoryProductRepository;
import com.hotelpms.frontdesk.inventory.repository.PurchaseOrderRepository;
import com.hotelpms.frontdesk.inventory.repository.StockReservationRepository;
import com.hotelpms.frontdesk.inventory.repository.SupplierRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Enforces tenant-scoped supplier procurement, receipt accounting, and inventory reconciliation.
 */
@Service
@RequiredArgsConstructor
public class ProcurementService {
    private static final String SUPPLIER_NOT_FOUND = "SUPPLIER_NOT_FOUND";
    private static final String PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND";
    private static final String PURCHASE_NOT_FOUND = "PURCHASE_NOT_FOUND";
    private static final String PURCHASE_CANCELLED = "CANCELLED";
    private static final String PURCHASE_RECEIVED = "RECEIVED";
    private static final String PURCHASE_PENDING = "PENDING";
    private static final String PURCHASE_PARTIALLY_RECEIVED = "PARTIALLY_RECEIVED";
    private final SupplierRepository suppliers;
    private final PurchaseOrderRepository purchases;
    private final InventoryService inventory;
    private final InventoryProductRepository products;
    private final InventoryBalanceRepository balances;
    private final StockReservationRepository reservations;
    private final BranchValidationService branchValidation;

    /**
     * Creates a supplier scoped to the tenant and normalizes its contact details.
     *
     * @param hotel tenant identifier
     * @param r validated supplier details
     * @return persisted supplier
     * @implSpec Overrides must preserve tenant-scoped tax-ID uniqueness.
     */
    @Transactional
    public Supplier supplier(final UUID hotel, final SupplierRequest r) {
        final String taxId = normalizeTaxId(r.taxId());
        if (taxId != null && suppliers.existsByHotelIdAndTaxIdIgnoreCase(hotel, taxId)) {
            throw new IllegalArgumentException("SUPPLIER_TAX_ID_ALREADY_EXISTS");
        }
        final Supplier s = new Supplier();
        s.setHotelId(hotel);
        s.setName(r.name().trim());
        s.setTaxId(taxId);
        s.setContact(normalize(r.contact() != null ? r.contact() : r.email()));
        s.setContactName(normalize(r.contactName()));
        s.setEmail(normalize(r.email()));
        s.setPhone(normalize(r.phone()));
        s.setWebsite(normalize(r.website()));
        s.setDescription(normalize(r.description()));
        return suppliers.save(s);
    }

    /**
     * Updates supplier metadata after verifying tenant ownership and tax-ID uniqueness.
     *
     * @param hotel tenant identifier
     * @param supplierId tenant-owned supplier identifier
     * @param r updated supplier fields
     * @return persisted supplier
     * @implSpec Overrides must preserve tenant-scoped lookups and tax-ID uniqueness.
     */
    @Transactional
    public Supplier updateSupplier(
            final UUID hotel, final UUID supplierId, final SupplierRequest r) {
        final Supplier supplier =
                suppliers
                        .findByIdAndHotelId(supplierId, hotel)
                        .orElseThrow(() -> new IllegalArgumentException(SUPPLIER_NOT_FOUND));
        final String taxId = normalizeTaxId(r.taxId());
        if (taxId != null
                && suppliers.existsByHotelIdAndTaxIdIgnoreCaseAndIdNot(hotel, taxId, supplierId)) {
            throw new IllegalArgumentException("SUPPLIER_TAX_ID_ALREADY_EXISTS");
        }
        supplier.setName(r.name().trim());
        supplier.setTaxId(taxId);
        supplier.setContact(normalize(r.contact() != null ? r.contact() : r.email()));
        supplier.setContactName(normalize(r.contactName()));
        supplier.setEmail(normalize(r.email()));
        supplier.setPhone(normalize(r.phone()));
        supplier.setWebsite(normalize(r.website()));
        supplier.setDescription(normalize(r.description()));
        return suppliers.save(supplier);
    }

    /**
     * Changes supplier availability while retaining the supplier record for purchase history.
     *
     * @param hotel tenant identifier
     * @param supplierId tenant-owned supplier identifier
     * @param active new availability state
     * @return persisted supplier
     * @implSpec Overrides must preserve tenant ownership and purchase history.
     */
    @Transactional
    public Supplier updateSupplierActive(
            final UUID hotel, final UUID supplierId, final boolean active) {
        final Supplier supplier =
                suppliers
                        .findByIdAndHotelId(supplierId, hotel)
                        .orElseThrow(() -> new IllegalArgumentException(SUPPLIER_NOT_FOUND));
        supplier.setActive(active);
        return suppliers.save(supplier);
    }

    /**
     * Creates a tenant-owned purchase order after validating its supplier, branch, and products.
     *
     * @param hotel tenant identifier
     * @param r purchase header and requested lines
     * @return persisted purchase order
     * @implSpec Overrides must preserve tenant and branch validation before persistence.
     */
    @Transactional
    public PurchaseOrder purchase(final UUID hotel, final PurchaseRequest r) {
        branchValidation.requireActiveBranch(hotel, r.branchId());
        final Supplier supplier =
                suppliers
                        .findByIdAndHotelId(r.supplierId(), hotel)
                        .orElseThrow(() -> new IllegalArgumentException(SUPPLIER_NOT_FOUND));
        if (!supplier.isActive()) {
            throw new IllegalArgumentException("SUPPLIER_INACTIVE");
        }
        final PurchaseOrder order =
                PurchaseOrder.builder()
                        .hotelId(hotel)
                        .branchId(r.branchId())
                        .supplierId(r.supplierId())
                        .serviceOrderId(r.serviceOrderId())
                        .createdAt(LocalDateTime.now())
                        .build();
        final var lineProducts = new HashSet<UUID>();
        for (final PurchaseLineRequest requestedLine : r.lines()) {
            if (!lineProducts.add(requestedLine.productId())) {
                throw new IllegalArgumentException("DUPLICATE_PRODUCT_ON_PURCHASE");
            }
            final InventoryProduct product =
                    products.findById(requestedLine.productId())
                            .filter(item -> hotel.equals(item.getHotelId()))
                            .orElseThrow(() -> new IllegalArgumentException(PRODUCT_NOT_FOUND));
            if (product.getSupplierId() != null
                    && !r.supplierId().equals(product.getSupplierId())) {
                throw new IllegalArgumentException("PURCHASE_PRODUCT_SUPPLIER_MISMATCH");
            }
            final PurchaseOrderLine line = new PurchaseOrderLine();
            line.setProductId(product.getId());
            line.setQuantityOrdered(requestedLine.quantity());
            line.setQuantityReceived(0);
            line.setUnitCost(requestedLine.unitCost());
            order.addLine(line);
        }
        if (order.getLines().isEmpty()) {
            throw new IllegalArgumentException("PURCHASE_LINES_REQUIRED");
        }
        return purchases.save(order);
    }

    /**
     * Lists the suppliers belonging to a tenant.
     *
     * @param hotel tenant identifier
     * @return tenant-owned suppliers
     * @implSpec Overrides must not return suppliers from another tenant.
     */
    @Transactional(readOnly = true)
    public List<Supplier> suppliers(final UUID hotel) {
        return suppliers.findAllByHotelId(hotel);
    }

    /**
     * Lists purchase orders for a tenant in reverse creation order.
     *
     * @param hotel tenant identifier
     * @return tenant-owned purchase orders
     * @implSpec Overrides must not return purchase orders from another tenant.
     */
    @Transactional(readOnly = true)
    public List<PurchaseOrder> purchases(final UUID hotel) {
        return purchases.findAllByHotelIdOrderByCreatedAtDesc(hotel);
    }

    /**
     * Receives ordered stock exactly once and records weighted cost and receipt history.
     *
     * @param hotel tenant identifier
     * @param purchaseId tenant-owned purchase-order identifier
     * @param request idempotent receipt quantities
     * @return updated purchase order
     * @implSpec Overrides must preserve tenant isolation, row locking, idempotency, and stock
     *     accounting.
     */
    @Transactional
    public PurchaseOrder receive(
            final UUID hotel, final UUID purchaseId, final PurchaseReceiptRequest request) {
        final PurchaseOrder purchase =
                purchases
                        .findForUpdateByIdAndHotelId(purchaseId, hotel)
                        .orElseThrow(() -> new IllegalArgumentException(PURCHASE_NOT_FOUND));
        final String receiptDigest = request.requestId() == null ? null : receiptDigest(request);
        if (receiptDigest != null) {
            final String previousDigest =
                    purchase.getReceiptRequestDigests() == null
                            ? null
                            : purchase.getReceiptRequestDigests()
                                    .get(request.requestId().toString());
            if (previousDigest != null) {
                if (previousDigest.equals(receiptDigest)) {
                    return purchase;
                }
                throw new IllegalArgumentException("PURCHASE_RECEIPT_REQUEST_ID_REUSED");
            }
        }
        if (PURCHASE_CANCELLED.equals(purchase.getStatus())) {
            throw new IllegalStateException("PURCHASE_CANCELLED");
        }
        if (PURCHASE_RECEIVED.equals(purchase.getStatus())) {
            throw new IllegalStateException("PURCHASE_ALREADY_RECEIVED");
        }
        if (purchase.getLines() == null || purchase.getLines().isEmpty()) {
            throw new IllegalStateException("LEGACY_PURCHASE_HAS_NO_ORDER_LINES");
        }
        if (request.lines().isEmpty()) {
            throw new IllegalArgumentException("RECEIPT_LINES_REQUIRED");
        }
        if (request.lines().stream().noneMatch(line -> line.quantity() > 0)) {
            throw new IllegalArgumentException("RECEIPT_QUANTITY_REQUIRED");
        }
        final var receivedProducts = new HashSet<UUID>();
        final List<Map.Entry<PurchaseOrderLine, Long>> receiptLines = new ArrayList<>();
        for (final var received : request.lines()) {
            if (received.quantity() <= 0) {
                throw new IllegalArgumentException("RECEIPT_QUANTITY_MUST_BE_POSITIVE");
            }
            if (!receivedProducts.add(received.productId())) {
                throw new IllegalArgumentException("DUPLICATE_PRODUCT_ON_RECEIPT");
            }
            final PurchaseOrderLine line =
                    purchase.getLines().stream()
                            .filter(item -> item.getProductId().equals(received.productId()))
                            .findFirst()
                            .orElseThrow(
                                    () -> new IllegalArgumentException("PRODUCT_NOT_ON_PURCHASE"));
            if (received.quantity() > line.getQuantityOrdered() - line.getQuantityReceived()) {
                throw new IllegalArgumentException("RECEIPT_EXCEEDS_ORDERED_QUANTITY");
            }
            receiptLines.add(Map.entry(line, received.quantity()));
        }
        receiptLines.sort(Comparator.comparing(entry -> entry.getKey().getProductId()));
        final LocalDateTime receivedAt = LocalDateTime.now();
        for (final var receipt : receiptLines) {
            final PurchaseOrderLine line = receipt.getKey();
            final InventoryProduct product =
                    products.findForUpdateByIdAndHotelId(line.getProductId(), hotel)
                            .orElseThrow(() -> new IllegalArgumentException(PRODUCT_NOT_FOUND));
            final long availableQuantity =
                    balances.sumQuantityByHotelAndProduct(hotel, product.getId())
                            + reservations.sumReservedQuantityByHotelAndProduct(
                                    hotel, product.getId());
            inventory.move(
                    hotel,
                    new MovementRequest(
                            purchase.getBranchId(),
                            line.getProductId(),
                            receipt.getValue(),
                            "Purchase receipt " + purchase.getId()));
            final long newQuantity = availableQuantity + receipt.getValue();
            final BigDecimal weightedCost =
                    product.getCost()
                            .multiply(BigDecimal.valueOf(availableQuantity))
                            .add(
                                    line.getUnitCost()
                                            .multiply(BigDecimal.valueOf(receipt.getValue())))
                            .divide(BigDecimal.valueOf(newQuantity), 2, RoundingMode.HALF_UP);
            product.setCost(weightedCost);
            if (product.getSupplierId() == null) {
                product.setSupplierId(purchase.getSupplierId());
            }
            products.save(product);
            line.setQuantityReceived(line.getQuantityReceived() + receipt.getValue());
            appendReceiptEvent(purchase, line, receipt.getValue(), "RECEIPT", receivedAt);
        }
        final boolean complete =
                purchase.getLines().stream()
                        .allMatch(line -> line.getQuantityReceived() >= line.getQuantityOrdered());
        final boolean anyReceived =
                purchase.getLines().stream().anyMatch(line -> line.getQuantityReceived() > 0);
        purchase.setLastReceivedAt(receivedAt);
        purchase.setStatus(
                complete
                        ? PURCHASE_RECEIVED
                        : anyReceived ? PURCHASE_PARTIALLY_RECEIVED : PURCHASE_PENDING);
        if (receiptDigest != null) {
            final var requestDigests =
                    purchase.getReceiptRequestDigests() == null
                            ? new HashMap<String, String>()
                            : new HashMap<>(purchase.getReceiptRequestDigests());
            requestDigests.put(request.requestId().toString(), receiptDigest);
            purchase.setReceiptRequestDigests(requestDigests);
        }
        return purchases.save(purchase);
    }

    private String receiptDigest(final PurchaseReceiptRequest request) {
        final String canonical =
                request.lines().stream()
                        .sorted(Comparator.comparing(line -> line.productId().toString()))
                        .map(line -> line.productId() + ":" + line.quantity())
                        .collect(Collectors.joining("|"));
        try {
            final byte[] digest =
                    MessageDigest.getInstance("SHA-256")
                            .digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (final NoSuchAlgorithmException exception) {
            throw new IllegalStateException("RECEIPT_DIGEST_UNAVAILABLE", exception);
        }
    }

    /**
     * Corrects a prior receipt without consuming stock already reserved by service orders.
     *
     * @param hotel tenant identifier
     * @param purchaseId tenant-owned purchase-order identifier
     * @param request corrected cumulative quantities
     * @return updated purchase order
     * @implSpec Overrides must preserve tenant checks and prevent negative or reserved stock.
     */
    @Transactional
    public PurchaseOrder correctReceipt(
            final UUID hotel,
            final UUID purchaseId,
            final PurchaseReceiptCorrectionRequest request) {
        final var purchase =
                purchases
                        .findForUpdateByIdAndHotelId(purchaseId, hotel)
                        .orElseThrow(() -> new IllegalArgumentException(PURCHASE_NOT_FOUND));
        if (PURCHASE_CANCELLED.equals(purchase.getStatus())) {
            throw new IllegalStateException("PURCHASE_CANCELLED");
        }
        if (purchase.getLines() == null || purchase.getLines().isEmpty()) {
            throw new IllegalStateException("LEGACY_PURCHASE_HAS_NO_ORDER_LINES");
        }
        final var correctedProducts = new HashSet<UUID>();
        for (final var correction : request.lines()) {
            if (!correctedProducts.add(correction.productId())) {
                throw new IllegalArgumentException("DUPLICATE_PRODUCT_ON_RECEIPT_CORRECTION");
            }
            final var line =
                    purchase.getLines().stream()
                            .filter(item -> item.getProductId().equals(correction.productId()))
                            .findFirst()
                            .orElseThrow(
                                    () -> new IllegalArgumentException("PRODUCT_NOT_ON_PURCHASE"));
            if (correction.quantityReceived() > line.getQuantityOrdered()) {
                throw new IllegalArgumentException("CORRECTED_RECEIPT_EXCEEDS_ORDERED_QUANTITY");
            }
            final long delta = correction.quantityReceived() - line.getQuantityReceived();
            if (delta == 0) {
                continue;
            }
            final var product =
                    products.findForUpdateByIdAndHotelId(line.getProductId(), hotel)
                            .orElseThrow(() -> new IllegalArgumentException(PRODUCT_NOT_FOUND));
            if (delta < 0) {
                final long branchQuantity =
                        balances.findByHotelIdAndBranchIdAndProductId(
                                        hotel, purchase.getBranchId(), product.getId())
                                .map(InventoryBalance::getQuantity)
                                .orElse(0L);
                final long branchReserved =
                        reservations.sumReservedQuantityByHotelAndBranchAndProduct(
                                hotel, purchase.getBranchId(), product.getId());
                if (branchQuantity + delta < branchReserved) {
                    throw new IllegalStateException("RECEIVED_STOCK_RESERVED_FOR_SERVICE_ORDERS");
                }
            }
            final long currentQuantity =
                    balances.sumQuantityByHotelAndProduct(hotel, product.getId())
                            + reservations.sumReservedQuantityByHotelAndProduct(
                                    hotel, product.getId());
            final long nextQuantity = currentQuantity + delta;
            if (nextQuantity < 0) {
                throw new IllegalStateException("RECEIVED_STOCK_ALREADY_CONSUMED_OR_RESERVED");
            }
            inventory.move(
                    hotel,
                    new MovementRequest(
                            purchase.getBranchId(),
                            product.getId(),
                            delta,
                            "Purchase receipt correction " + purchase.getId()));
            appendReceiptEvent(purchase, line, delta, "CORRECTION", LocalDateTime.now());
            if (nextQuantity > 0) {
                final BigDecimal adjustedValue =
                        product.getCost()
                                .multiply(BigDecimal.valueOf(currentQuantity))
                                .add(line.getUnitCost().multiply(BigDecimal.valueOf(delta)));
                if (adjustedValue.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalStateException(
                            "RECEIPT_CORRECTION_WOULD_INVALIDATE_WEIGHTED_COST");
                }
                product.setCost(
                        adjustedValue.divide(
                                BigDecimal.valueOf(nextQuantity), 2, RoundingMode.HALF_UP));
                products.save(product);
            }
            line.setQuantityReceived(correction.quantityReceived());
        }
        final boolean complete =
                purchase.getLines().stream()
                        .allMatch(line -> line.getQuantityReceived() >= line.getQuantityOrdered());
        final boolean anyReceived =
                purchase.getLines().stream().anyMatch(line -> line.getQuantityReceived() > 0);
        purchase.setLastReceivedAt(anyReceived ? LocalDateTime.now() : null);
        purchase.setStatus(
                complete
                        ? PURCHASE_RECEIVED
                        : anyReceived ? PURCHASE_PARTIALLY_RECEIVED : PURCHASE_PENDING);
        return purchases.save(purchase);
    }

    private void appendReceiptEvent(
            final PurchaseOrder purchase,
            final PurchaseOrderLine line,
            final long quantityDelta,
            final String kind,
            final LocalDateTime occurredAt) {
        final var events =
                purchase.getReceiptEvents() == null
                        ? new ArrayList<Map<String, String>>()
                        : new ArrayList<>(purchase.getReceiptEvents());
        events.add(
                Map.of(
                        "kind",
                        kind,
                        "productId",
                        line.getProductId().toString(),
                        "quantity",
                        Long.toString(quantityDelta),
                        "unitCost",
                        line.getUnitCost().toPlainString(),
                        "receivedAt",
                        occurredAt.toString()));
        purchase.setReceiptEvents(events);
    }

    /**
     * Cancels an unreceived purchase order after tenant-scoped row locking.
     *
     * @param hotel tenant identifier
     * @param purchaseId tenant-owned purchase-order identifier
     * @return cancelled purchase order
     * @implSpec Overrides must prevent cancellation of received stock.
     */
    @Transactional
    public PurchaseOrder cancel(final UUID hotel, final UUID purchaseId) {
        final PurchaseOrder purchase =
                purchases
                        .findForUpdateByIdAndHotelId(purchaseId, hotel)
                        .orElseThrow(() -> new IllegalArgumentException(PURCHASE_NOT_FOUND));
        if (PURCHASE_CANCELLED.equals(purchase.getStatus())) {
            return purchase;
        }
        if (PURCHASE_RECEIVED.equals(purchase.getStatus())) {
            throw new IllegalStateException("PURCHASE_ALREADY_RECEIVED");
        }
        if (purchase.getLines() != null
                && purchase.getLines().stream().anyMatch(line -> line.getQuantityReceived() > 0)) {
            throw new IllegalStateException("PARTIALLY_RECEIVED_PURCHASE_CANNOT_BE_CANCELLED");
        }
        purchase.setStatus("CANCELLED");
        return purchases.save(purchase);
    }

    private static String normalizeTaxId(final String taxId) {
        final String normalized = normalize(taxId);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private static String normalize(final String value) {
        if (value == null) {
            return null;
        }
        final String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
