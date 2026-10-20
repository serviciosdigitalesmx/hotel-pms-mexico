package com.hotelpms.frontdesk.inventory.service;

import com.hotelpms.frontdesk.audit.domain.AuditEvent;
import com.hotelpms.frontdesk.audit.service.AuditEventService;
import com.hotelpms.frontdesk.client.BranchValidationService;
import com.hotelpms.frontdesk.exception.ConflictException;
import com.hotelpms.frontdesk.inventory.domain.InventoryBalance;
import com.hotelpms.frontdesk.inventory.domain.InventoryProduct;
import com.hotelpms.frontdesk.inventory.domain.StockReservation;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.ImportResult;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.MovementRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.ProductConsumption;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.ProductRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.ReservationRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.StockCountRequest;
import com.hotelpms.frontdesk.inventory.repository.InventoryBalanceRepository;
import com.hotelpms.frontdesk.inventory.repository.InventoryProductRepository;
import com.hotelpms.frontdesk.inventory.repository.PosInventoryConsumptionRepository;
import com.hotelpms.frontdesk.inventory.repository.StockReservationRepository;
import com.hotelpms.frontdesk.inventory.repository.SupplierRepository;
import com.hotelpms.frontdesk.notifications.OperationalNotificationOutboxService;
import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrderStatus;
import com.hotelpms.frontdesk.serviceorder.repository.ServiceOrderRepository;

import lombok.RequiredArgsConstructor;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Enforces tenant- and branch-scoped stock operations, reservations, and catalog imports for Fixi.
 */
@Service
@RequiredArgsConstructor
public class InventoryService {
    private static final String INSUFFICIENT_STOCK = "INSUFFICIENT_STOCK";
    private static final String PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND";
    private static final String SERVICE_ORDER_NOT_FOUND = "SERVICE_ORDER_NOT_FOUND";
    private static final String RESERVATION_RESERVED = "RESERVED";
    private static final String RESERVATION_CONSUMED = "CONSUMED";
    private static final String SKU_HEADER = "sku";
    private static final String NAME_HEADER = "name";
    private static final String SUPPLIER_ID_HEADER = "supplierid";
    private static final String BRANCH_ID_HEADER = "branchid";
    private static final String PRODUCT_ID_JSON = "\",\"productId\":\"";
    private static final String SERVICE_ORDER_JSON = "{\"serviceOrderId\":\"";
    private static final String QUANTITY_JSON = "\",\"quantity\":";
    private static final String CSV_ROW_PREFIX = "row ";
    private static final String PRODUCT_CATEGORY_HEADER = "category";
    private static final int MAX_BULK_PRICE_PRODUCTS = 500;
    private static final int MAX_REASON_LENGTH = 500;
    private static final int PRICE_PERCENTAGE_SCALE = 6;
    private static final int PERCENTAGE_BASE = 100;
    private static final int MONEY_SCALE = 2;
    private static final int MAX_CONSUMED_PRODUCTS = 5;
    private static final int MAX_CSV_BYTES = 5 * 1024 * 1024;
    private static final int MAX_CSV_ROWS = 5000;
    private static final int PRODUCT_NAME_MAX_LENGTH = 200;
    private static final int PRODUCT_DESCRIPTION_MAX_LENGTH = 2000;
    private static final int PRODUCT_CATEGORY_MAX_LENGTH = 120;
    private static final int PRODUCT_SKU_MAX_LENGTH = 80;
    private final InventoryProductRepository products;
    private final InventoryBalanceRepository balances;
    private final AuditEventService auditEvents;
    private final StockReservationRepository reservations;
    private final SupplierRepository suppliers;
    private final BranchValidationService branchValidation;
    private final ServiceOrderRepository serviceOrders;
    private final PosInventoryConsumptionRepository posConsumptions;
    private final OperationalNotificationOutboxService notificationOutbox;

    /**
     * Creates an inventory product.
     *
     * @param hotel tenant identifier
     * @param r product request
     * @return persisted product
     * @implSpec Overrides must preserve tenant ownership and initial stock accounting.
     */
    @Transactional
    public InventoryProduct product(final UUID hotel, final ProductRequest r) {
        validatePosMenuItemLink(hotel, r.posMenuItemId(), null);
        final InventoryProduct p = new InventoryProduct();
        p.setHotelId(hotel);
        p.setSku(r.sku().trim());
        p.setName(r.name().trim());
        p.setDescription(r.description() == null ? null : r.description().trim());
        p.setOptional(r.optional());
        p.setCategory(r.category().trim());
        p.setCost(r.cost());
        p.setPrice(r.price());
        p.setReorderLevel(r.reorderLevel());
        p.setMaximumStockLevel(r.maximumStockLevel());
        if (r.supplierId() != null) {
            suppliers
                    .findByIdAndHotelId(r.supplierId(), hotel)
                    .orElseThrow(() -> new IllegalArgumentException("SUPPLIER_NOT_FOUND"));
        }
        p.setSupplierId(r.supplierId());
        p.setPosMenuItemId(r.posMenuItemId());
        final InventoryProduct saved = products.save(p);
        if (r.initialQuantity() > 0) {
            if (r.branchId() == null) {
                throw new IllegalArgumentException("INITIAL_STOCK_BRANCH_REQUIRED");
            }
            move(
                    hotel,
                    new MovementRequest(
                            r.branchId(),
                            saved.getId(),
                            r.initialQuantity(),
                            "Initial product stock"));
        }
        return saved;
    }

    /**
     * Updates commercial catalog fields without changing branch stock.
     *
     * @param hotel tenant identifier
     * @param productId product identifier
     * @param r updated product fields
     * @return updated product
     * @implSpec Overrides must preserve tenant ownership and branch-stock invariants.
     */
    @Transactional
    public InventoryProduct updateProduct(
            final UUID hotel, final UUID productId, final ProductRequest r) {
        validatePosMenuItemLink(hotel, r.posMenuItemId(), productId);
        final InventoryProduct product =
                products.findByIdAndHotelId(productId, hotel)
                        .orElseThrow(() -> new IllegalArgumentException(PRODUCT_NOT_FOUND));
        if (r.supplierId() != null) {
            suppliers
                    .findByIdAndHotelId(r.supplierId(), hotel)
                    .orElseThrow(() -> new IllegalArgumentException("SUPPLIER_NOT_FOUND"));
        }
        product.setSku(r.sku().trim());
        product.setName(r.name().trim());
        product.setDescription(r.description() == null ? null : r.description().trim());
        product.setOptional(r.optional());
        product.setCategory(r.category().trim());
        product.setCost(r.cost());
        product.setPrice(r.price());
        product.setReorderLevel(r.reorderLevel());
        product.setMaximumStockLevel(r.maximumStockLevel());
        product.setSupplierId(r.supplierId());
        product.setPosMenuItemId(r.posMenuItemId());
        return products.save(product);
    }

    private void validatePosMenuItemLink(
            final UUID hotel, final UUID menuItemId, final UUID productId) {
        if (menuItemId == null) {
            return;
        }
        final boolean linkedElsewhere =
                productId == null
                        ? products.existsByHotelIdAndPosMenuItemId(hotel, menuItemId)
                        : products.existsByHotelIdAndPosMenuItemIdAndIdNot(
                                hotel, menuItemId, productId);
        if (linkedElsewhere) {
            throw new IllegalArgumentException("POS_MENU_ITEM_ALREADY_LINKED");
        }
    }

    /**
     * Applies a validated percentage increase to selected tenant-owned sale prices.
     *
     * @param hotel tenant identifier
     * @param productIds selected product identifiers
     * @param percentage percentage increase in the range supported by the API
     * @return updated products
     * @implSpec Overrides must preserve tenant ownership and the bounded batch size.
     */
    @Transactional
    public List<InventoryProduct> increasePrices(
            final UUID hotel, final List<UUID> productIds, final BigDecimal percentage) {
        if (productIds == null
                || productIds.isEmpty()
                || productIds.size() > MAX_BULK_PRICE_PRODUCTS
                || percentage == null
                || percentage.signum() < 0
                || percentage.compareTo(new BigDecimal("1000.00")) > 0) {
            throw new IllegalArgumentException("INVALID_BULK_PRICE_REQUEST");
        }
        final List<InventoryProduct> selected =
                products.findAllById(productIds).stream()
                        .filter(product -> hotel.equals(product.getHotelId()))
                        .toList();
        if (selected.size() != productIds.stream().distinct().count()) {
            throw new IllegalArgumentException(PRODUCT_NOT_FOUND);
        }
        final BigDecimal multiplier =
                BigDecimal.ONE.add(
                        percentage.divide(
                                BigDecimal.valueOf(PERCENTAGE_BASE),
                                PRICE_PERCENTAGE_SCALE,
                                RoundingMode.HALF_UP));
        selected.forEach(
                product ->
                        product.setPrice(
                                product.getPrice()
                                        .multiply(multiplier)
                                        .setScale(MONEY_SCALE, RoundingMode.HALF_UP)));
        return products.saveAll(selected);
    }

    /**
     * Applies an inventory movement.
     *
     * @param hotel tenant identifier
     * @param r movement request
     * @return updated balance
     * @implSpec Overrides must preserve tenant/branch isolation and audit recording.
     */
    @Transactional
    public InventoryBalance move(final UUID hotel, final MovementRequest r) {
        if (r.reason() == null
                || r.reason().isBlank()
                || r.reason().trim().length() > MAX_REASON_LENGTH) {
            throw new IllegalArgumentException("INVENTORY_MOVEMENT_REASON_REQUIRED");
        }
        branchValidation.requireActiveBranch(hotel, r.branchId());
        final InventoryProduct p =
                products.findForUpdateByIdAndHotelId(r.productId(), hotel)
                        .orElseThrow(() -> new IllegalArgumentException(PRODUCT_NOT_FOUND));
        final InventoryBalance b =
                balances.findByHotelIdAndBranchIdAndProductId(hotel, r.branchId(), p.getId())
                        .orElseGet(
                                () ->
                                        InventoryBalance.builder()
                                                .hotelId(hotel)
                                                .branchId(r.branchId())
                                                .productId(p.getId())
                                                .build());
        final long next = b.getQuantity() + r.delta();
        if (next < 0) {
            throw new IllegalStateException(INSUFFICIENT_STOCK);
        }
        b.setQuantity(next);
        final InventoryBalance saved = balances.save(b);
        publishLowStockIfCrossed(hotel, p, next - r.delta(), next, r.branchId());
        final String reason = r.reason().trim();
        final String escapedReason = escapeJson(reason);
        auditEvents.record(
                hotel,
                null,
                AuditEvent.Category.INVENTORY,
                "INVENTORY_MOVEMENT_RECORDED",
                saved.getId(),
                null,
                "{\"branchId\":\""
                        + r.branchId()
                        + PRODUCT_ID_JSON
                        + r.productId()
                        + "\",\"delta\":"
                        + r.delta()
                        + ",\"previous\":"
                        + (next - r.delta())
                        + ",\"quantity\":"
                        + next
                        + ",\"reason\":\""
                        + escapedReason
                        + "\"}");
        return saved;
    }

    /**
     * Consumes linked POS products once per order, even when confirmation is retried.
     *
     * @param hotel tenant identifier
     * @param orderId POS order identifier
     * @param branchId stock branch identifier
     * @param lines requested product quantities
     * @implSpec Overrides must retain tenant/branch checks and replay idempotency.
     */
    @Transactional
    public void consumePosOrder(
            final UUID hotel,
            final UUID orderId,
            final UUID branchId,
            final List<PosConsumptionLine> lines) {
        if (orderId == null || branchId == null || lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("POS_ORDER_BRANCH_AND_LINES_REQUIRED");
        }
        branchValidation.requireActiveBranch(hotel, branchId);
        final var consolidated = new TreeMap<UUID, Long>();
        for (final PosConsumptionLine line : lines) {
            if (line.quantity() < 1) {
                throw new IllegalArgumentException("INVALID_POS_QUANTITY");
            }
            consolidated.merge(line.productId(), line.quantity(), Math::addExact);
        }
        for (final var entry : consolidated.entrySet()) {
            final PosConsumptionLine line =
                    new PosConsumptionLine(entry.getKey(), entry.getValue());
            products.findByIdAndHotelId(line.productId(), hotel)
                    .orElseThrow(() -> new IllegalArgumentException(PRODUCT_NOT_FOUND));
            final var existing =
                    posConsumptions
                            .findByHotelIdAndOrderIdAndProductId(hotel, orderId, line.productId())
                            .orElse(null);
            if (existing != null) {
                if (!existing.getBranchId().equals(branchId)
                        || existing.getQuantity() != line.quantity()) {
                    throw new IllegalStateException("POS_CONSUMPTION_REPLAY_CONFLICT");
                }
                continue;
            }
            final var record =
                    new com.hotelpms.frontdesk.inventory.domain.PosInventoryConsumption();
            record.setHotelId(hotel);
            record.setOrderId(orderId);
            record.setProductId(line.productId());
            record.setBranchId(branchId);
            record.setQuantity(line.quantity());
            posConsumptions.saveAndFlush(record);
            move(
                    hotel,
                    new MovementRequest(
                            branchId, line.productId(), -line.quantity(), "POS order " + orderId));
        }
    }

    private static String escapeJson(final String value) {
        final StringBuilder escaped = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.toString();
    }

    private void publishLowStockIfCrossed(
            final UUID hotel,
            final InventoryProduct product,
            final long previous,
            final long current,
            final UUID branchId) {
        final long threshold = product.getReorderLevel();
        if (threshold <= 0 || previous <= threshold || current > threshold) {
            return;
        }
        notificationOutbox.enqueue(
                hotel,
                "STOCK_LOW",
                "Existencia baja: " + product.getName(),
                "El producto "
                        + product.getName()
                        + " ("
                        + product.getSku()
                        + ") quedó en "
                        + current
                        + " unidades, por debajo del mínimo de "
                        + threshold
                        + ". Sucursal: "
                        + branchId
                        + ".",
                "STOCK_LOW:" + branchId + ":" + product.getId() + ":" + UUID.randomUUID());
    }

    /**
     * Sets a physically counted stock quantity and retains the adjustment reason in audit.
     *
     * @param hotel tenant identifier
     * @param request branch, product, and counted quantity
     * @return persisted branch balance
     * @implSpec Overrides must preserve tenant and branch isolation and audit recording.
     */
    @Transactional
    public InventoryBalance count(final UUID hotel, final StockCountRequest request) {
        branchValidation.requireActiveBranch(hotel, request.branchId());
        final InventoryProduct product =
                products.findForUpdateByIdAndHotelId(request.productId(), hotel)
                        .orElseThrow(() -> new IllegalArgumentException(PRODUCT_NOT_FOUND));
        final InventoryBalance balance =
                balances.findByHotelIdAndBranchIdAndProductId(
                                hotel, request.branchId(), product.getId())
                        .orElseGet(
                                () ->
                                        InventoryBalance.builder()
                                                .hotelId(hotel)
                                                .branchId(request.branchId())
                                                .productId(product.getId())
                                                .build());
        final long previous = balance.getQuantity();
        if (request.expectedCurrentQuantity() != null
                && request.expectedCurrentQuantity() != previous) {
            throw new ConflictException(
                    "STOCK_COUNT_STALE: current stock changed from "
                            + request.expectedCurrentQuantity()
                            + " to "
                            + previous);
        }
        balance.setQuantity(request.countedQuantity());
        final InventoryBalance saved = balances.save(balance);
        publishLowStockIfCrossed(
                hotel, product, previous, request.countedQuantity(), request.branchId());
        auditEvents.record(
                hotel,
                null,
                AuditEvent.Category.INVENTORY,
                "INVENTORY_STOCK_COUNTED",
                saved.getId(),
                null,
                "{\"branchId\":\""
                        + request.branchId()
                        + PRODUCT_ID_JSON
                        + request.productId()
                        + "\",\"previous\":"
                        + previous
                        + ",\"counted\":"
                        + request.countedQuantity()
                        + ",\"reason\":\""
                        + request.reason().replace("\\", "\\\\").replace("\"", "\\\"")
                        + "\"}");
        return saved;
    }

    /**
     * Reserves stock for a service order.
     *
     * @param hotel tenant identifier
     * @param r reservation request
     * @return reservation
     * @implSpec Overrides must preserve tenant/branch ownership and available-stock accounting.
     */
    @Transactional
    public StockReservation reserve(final UUID hotel, final ReservationRequest r) {
        if (r.quantity() <= 0) {
            throw new IllegalArgumentException("RESERVATION_QUANTITY_MUST_BE_POSITIVE");
        }
        branchValidation.requireActiveBranch(hotel, r.branchId());
        final var order =
                serviceOrders
                        .findForUpdateByIdAndHotelId(r.serviceOrderId(), hotel)
                        .orElseThrow(() -> new IllegalArgumentException(SERVICE_ORDER_NOT_FOUND));
        if (order.getStatus() == ServiceOrderStatus.COMPLETED
                || order.getStatus() == ServiceOrderStatus.CANCELLED) {
            throw new IllegalStateException("SERVICE_ORDER_NOT_OPEN_FOR_STOCK_RESERVATION");
        }
        if (order.getBranchId() == null || !order.getBranchId().equals(r.branchId())) {
            throw new IllegalArgumentException("RESERVATION_BRANCH_MISMATCH");
        }
        final InventoryProduct product =
                products.findForUpdateByIdAndHotelId(r.productId(), hotel)
                        .orElseThrow(() -> new IllegalArgumentException(PRODUCT_NOT_FOUND));
        final Optional<StockReservation> existing =
                reservations.findForUpdateByHotelIdAndServiceOrderIdAndProductId(
                        hotel, r.serviceOrderId(), product.getId());
        if (existing.isPresent() && RESERVATION_CONSUMED.equals(existing.get().getStatus())) {
            throw new IllegalStateException("RESERVATION_ALREADY_CONSUMED");
        }
        if (existing.isPresent()
                && RESERVATION_RESERVED.equals(existing.get().getStatus())
                && existing.get().getQuantity() == r.quantity()
                && existing.get().getBranchId().equals(r.branchId())) {
            return existing.get();
        }
        final InventoryBalance b =
                balances.findByHotelIdAndBranchIdAndProductId(hotel, r.branchId(), product.getId())
                        .orElseGet(
                                () ->
                                        InventoryBalance.builder()
                                                .hotelId(hotel)
                                                .branchId(r.branchId())
                                                .productId(product.getId())
                                                .build());
        final long previousReservation =
                existing.filter(item -> RESERVATION_RESERVED.equals(item.getStatus()))
                        .map(StockReservation::getQuantity)
                        .orElse(0L);
        final long difference = r.quantity() - previousReservation;
        if (difference > b.getQuantity()) {
            throw new IllegalStateException(INSUFFICIENT_STOCK);
        }
        b.setQuantity(b.getQuantity() - difference);
        balances.save(b);
        publishLowStockIfCrossed(
                hotel, product, b.getQuantity() + difference, b.getQuantity(), r.branchId());
        final StockReservation reservation =
                existing.map(
                                item -> {
                                    item.setBranchId(r.branchId());
                                    item.setQuantity(r.quantity());
                                    item.setStatus(RESERVATION_RESERVED);
                                    return item;
                                })
                        .orElseGet(
                                () ->
                                        StockReservation.builder()
                                                .hotelId(hotel)
                                                .branchId(r.branchId())
                                                .productId(product.getId())
                                                .serviceOrderId(r.serviceOrderId())
                                                .quantity(r.quantity())
                                                .createdAt(LocalDateTime.now())
                                                .build());
        final StockReservation saved = reservations.save(reservation);
        auditEvents.record(
                hotel,
                null,
                AuditEvent.Category.INVENTORY,
                "SERVICE_ORDER_STOCK_RESERVED",
                saved.getId(),
                null,
                SERVICE_ORDER_JSON
                        + r.serviceOrderId()
                        + "\",\"branchId\":\""
                        + r.branchId()
                        + PRODUCT_ID_JSON
                        + product.getId()
                        + QUANTITY_JSON
                        + r.quantity()
                        + "}");
        return saved;
    }

    /**
     * Releases a stock reservation.
     *
     * @param hotel tenant identifier
     * @param order service order identifier
     * @param product product identifier
     * @return released reservation
     * @implSpec Overrides must preserve tenant ownership and reservation state transitions.
     */
    @Transactional
    public StockReservation release(final UUID hotel, final UUID order, final UUID product) {
        final var serviceOrder =
                serviceOrders
                        .findForUpdateByIdAndHotelId(order, hotel)
                        .orElseThrow(() -> new IllegalArgumentException(SERVICE_ORDER_NOT_FOUND));
        if (serviceOrder.getStatus() == ServiceOrderStatus.COMPLETED) {
            throw new IllegalStateException("DELIVERED_SERVICE_ORDER_STOCK_CANNOT_BE_RELEASED");
        }
        final StockReservation r =
                reservations
                        .findForUpdateByHotelIdAndServiceOrderIdAndProductId(hotel, order, product)
                        .orElseThrow(() -> new IllegalArgumentException("RESERVATION_NOT_FOUND"));
        if ("RELEASED".equals(r.getStatus())) {
            return r;
        }
        if (RESERVATION_CONSUMED.equals(r.getStatus())) {
            throw new IllegalStateException("RESERVATION_ALREADY_CONSUMED");
        }
        products.findForUpdateByIdAndHotelId(r.getProductId(), hotel)
                .orElseThrow(() -> new IllegalArgumentException(PRODUCT_NOT_FOUND));
        final InventoryBalance b =
                balances.findByHotelIdAndBranchIdAndProductId(
                                hotel, r.getBranchId(), r.getProductId())
                        .orElseThrow();
        b.setQuantity(b.getQuantity() + r.getQuantity());
        balances.save(b);
        r.setStatus("RELEASED");
        final StockReservation released = reservations.save(r);
        auditEvents.record(
                hotel,
                null,
                AuditEvent.Category.INVENTORY,
                "SERVICE_ORDER_STOCK_RELEASED",
                released.getId(),
                null,
                SERVICE_ORDER_JSON
                        + order
                        + PRODUCT_ID_JSON
                        + product
                        + QUANTITY_JSON
                        + released.getQuantity()
                        + "}");
        return released;
    }

    /**
     * Releases every still-reserved product when a service order is cancelled.
     *
     * @param hotel tenant identifier
     * @param orderId service-order identifier
     * @return current reservation rows for the order
     * @implSpec Overrides must preserve tenant scoping and reservation state transitions.
     */
    @Transactional
    public List<StockReservation> releaseReservationsForOrder(
            final UUID hotel, final UUID orderId) {
        final var serviceOrder =
                serviceOrders
                        .findForUpdateByIdAndHotelId(orderId, hotel)
                        .orElseThrow(() -> new IllegalArgumentException(SERVICE_ORDER_NOT_FOUND));
        if (serviceOrder.getStatus() == ServiceOrderStatus.COMPLETED) {
            throw new IllegalStateException("DELIVERED_SERVICE_ORDER_STOCK_CANNOT_BE_RELEASED");
        }
        final List<StockReservation> orderReservations =
                reservations.findAllForUpdateByHotelIdAndServiceOrderIdOrderByCreatedAtAsc(
                        hotel, orderId);
        for (final StockReservation reservation : orderReservations) {
            if (RESERVATION_RESERVED.equals(reservation.getStatus())) {
                release(hotel, orderId, reservation.getProductId());
            }
        }
        return reservations.findAllForUpdateByHotelIdAndServiceOrderIdOrderByCreatedAtAsc(
                hotel, orderId);
    }

    /**
     * Marks reserved stock as consumed after a service order is delivered.
     *
     * @param hotel tenant identifier
     * @param orderId service-order identifier
     * @return reservation rows after consumption
     * @implSpec Overrides must preserve tenant scoping and inventory audit events.
     */
    @Transactional
    public List<StockReservation> consumeReservations(final UUID hotel, final UUID orderId) {
        final List<StockReservation> orderReservations =
                reservations.findAllForUpdateByHotelIdAndServiceOrderIdOrderByCreatedAtAsc(
                        hotel, orderId);
        for (final StockReservation reservation : orderReservations) {
            if (!RESERVATION_RESERVED.equals(reservation.getStatus())) {
                continue;
            }
            reservation.setStatus(RESERVATION_CONSUMED);
            reservation.setConsumedAt(LocalDateTime.now());
            final StockReservation consumed = reservations.save(reservation);
            auditEvents.record(
                    hotel,
                    null,
                    AuditEvent.Category.INVENTORY,
                    "SERVICE_ORDER_STOCK_CONSUMED",
                    consumed.getId(),
                    null,
                    SERVICE_ORDER_JSON
                            + orderId
                            + PRODUCT_ID_JSON
                            + consumed.getProductId()
                            + QUANTITY_JSON
                            + consumed.getQuantity()
                            + "}");
        }
        return orderReservations;
    }

    /**
     * Lists reservations after confirming the service order belongs to the tenant.
     *
     * @param hotel tenant identifier
     * @param orderId service-order identifier
     * @return order reservations in creation order
     * @implSpec Overrides must preserve tenant ownership checks.
     */
    @Transactional(readOnly = true)
    public List<StockReservation> reservationsForOrder(final UUID hotel, final UUID orderId) {
        serviceOrders
                .findByIdAndHotelId(orderId, hotel)
                .orElseThrow(() -> new IllegalArgumentException(SERVICE_ORDER_NOT_FOUND));
        return reservations.findAllByHotelIdAndServiceOrderIdOrderByCreatedAtAsc(hotel, orderId);
    }

    /**
     * Returns the most-consumed products for a tenant and optional branch/date range.
     *
     * @param hotel tenant identifier
     * @param branchId optional branch filter
     * @param from inclusive start date
     * @param to inclusive end date
     * @return up to five products ordered by consumed quantity
     * @implSpec Overrides must preserve tenant and branch scoping.
     */
    @Transactional(readOnly = true)
    public List<ProductConsumption> topConsumedProducts(
            final UUID hotel, final UUID branchId, final LocalDate from, final LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("INVALID_REPORT_DATE_RANGE");
        }
        if (branchId != null) {
            branchValidation.requireActiveBranch(hotel, branchId);
        }
        return reservations
                .findTopConsumedProducts(
                        hotel, branchId, from.atStartOfDay(), to.plusDays(1).atStartOfDay())
                .stream()
                .limit(MAX_CONSUMED_PRODUCTS)
                .map(
                        row -> {
                            final UUID productId = (UUID) row[0];
                            final long quantity = ((Number) row[1]).longValue();
                            final var product =
                                    products.findByIdAndHotelId(productId, hotel).orElse(null);
                            return new ProductConsumption(
                                    productId,
                                    product == null ? null : product.getSku(),
                                    product == null ? "Producto eliminado" : product.getName(),
                                    quantity);
                        })
                .toList();
    }

    /**
     * Lists products for a tenant.
     *
     * @param hotel tenant identifier
     * @return products
     * @implSpec Overrides must preserve tenant scoping.
     */
    @Transactional(readOnly = true)
    public List<InventoryProduct> products(final UUID hotel) {
        return products.findAllByHotelId(hotel);
    }

    /**
     * Lists branch stock balances.
     *
     * @param hotel tenant identifier
     * @param branch branch identifier
     * @return balances
     * @implSpec Overrides must preserve tenant and branch scoping.
     */
    @Transactional(readOnly = true)
    public List<InventoryBalance> stock(final UUID hotel, final UUID branch) {
        branchValidation.requireActiveBranch(hotel, branch);
        return balances.findAllByHotelIdAndBranchId(hotel, branch);
    }

    /**
     * Imports inventory products from CSV.
     *
     * @param hotel tenant identifier
     * @param csv CSV content
     * @return import summary
     * @implSpec Overrides must preserve tenant scope and import validation limits.
     */
    @Transactional
    public ImportResult importCsv(final UUID hotel, final String csv) {
        return importCsv(hotel, csv, null);
    }

    /**
     * Imports CSV catalog rows and optionally records initial stock at a default branch.
     *
     * @param hotel tenant identifier
     * @param csv CSV content
     * @param defaultBranchId branch for imported initial quantities when a row omits it
     * @return import summary, including row-level rejections
     * @implSpec Overrides must preserve tenant validation and row-level error reporting.
     */
    @Transactional
    public ImportResult importCsv(final UUID hotel, final String csv, final UUID defaultBranchId) {
        if (csv != null
                && csv.getBytes(StandardCharsets.UTF_8).length > MAX_CSV_BYTES) {
            throw new IllegalArgumentException("CSV_FILE_EXCEEDS_5_MB");
        }
        final List<String> errors = new ArrayList<>();
        final Set<String> seenSkus = new HashSet<>();
        final List<List<String>> rows =
                parseCsv(csv == null ? "" : csv.replaceFirst("^\\uFEFF", ""));
        List<String> headers = List.of(SKU_HEADER, NAME_HEADER, "optional");
        int firstDataRow = 0;
        if (!rows.isEmpty()
                && rows.get(0).stream()
                        .map(
                                value ->
                                        value.trim()
                                                .toLowerCase(Locale.ROOT)
                                                .replaceAll("[\\s_-]+", ""))
                        .anyMatch(
                                value ->
                                        List.of(
                                                        SKU_HEADER,
                                                        NAME_HEADER,
                                                        "product",
                                                        "productname",
                                                        PRODUCT_CATEGORY_HEADER,
                                                        "description",
                                                        "quantity",
                                                        "stock")
                                                .contains(value))) {
            headers =
                    rows.get(0).stream()
                            .map(
                                    value ->
                                            value.trim()
                                                    .toLowerCase(Locale.ROOT)
                                                    .replaceAll("[\\s_-]+", ""))
                            .toList();
            firstDataRow = 1;
        }
        if (rows.size() - firstDataRow > MAX_CSV_ROWS) {
            throw new IllegalArgumentException("CSV_EXCEEDS_5000_ROWS");
        }
        int imported = 0;
        int rejected = 0;
        for (int i = firstDataRow; i < rows.size(); i++) {
            final List<String> cells = rows.get(i);
            if (cells.size() > headers.size()) {
                rejected++;
                errors.add(CSV_ROW_PREFIX + (i + 1) + ": more columns than the CSV header");
                continue;
            }
            final Map<String, String> values = new HashMap<>();
            for (int column = 0; column < headers.size() && column < cells.size(); column++) {
                values.put(headers.get(column), cells.get(column).trim());
            }
            final String suppliedSku = values.getOrDefault(SKU_HEADER, "");
            final String name =
                    values.getOrDefault(
                            NAME_HEADER,
                            values.getOrDefault("product", values.getOrDefault("productname", "")));
            if (name.isBlank()) {
                rejected++;
                errors.add(CSV_ROW_PREFIX + (i + 1) + ": product name is required");
                continue;
            }
            final String category =
                    values.getOrDefault(PRODUCT_CATEGORY_HEADER, "General").isBlank()
                            ? "General"
                            : values.get(PRODUCT_CATEGORY_HEADER);
            final String description = values.getOrDefault("description", "");
            final String rawOptional = values.getOrDefault("optional", "false");
            if (name.length() > PRODUCT_NAME_MAX_LENGTH
                    || description.length() > PRODUCT_DESCRIPTION_MAX_LENGTH
                    || category.length() > PRODUCT_CATEGORY_MAX_LENGTH) {
                rejected++;
                errors.add(
                        CSV_ROW_PREFIX
                                + (i + 1)
                                + ": name, description, or category exceeds its maximum length");
                continue;
            }
            if (!"true".equalsIgnoreCase(rawOptional) && !"false".equalsIgnoreCase(rawOptional)) {
                rejected++;
                errors.add(CSV_ROW_PREFIX + (i + 1) + ": optional must be true or false");
                continue;
            }
            final ProductRequest productRequest;
            try {
                final String sku = suppliedSku.isBlank() ? generatedImportSku(values) : suppliedSku;
                if (sku.length() > PRODUCT_SKU_MAX_LENGTH
                        || products.findByHotelIdAndSku(hotel, sku).isPresent()
                        || seenSkus.contains(sku)) {
                    rejected++;
                    errors.add(CSV_ROW_PREFIX + (i + 1) + ": SKU is too long or already exists");
                    continue;
                }
                final UUID supplierId =
                        values.containsKey(SUPPLIER_ID_HEADER)
                                        && !values.get(SUPPLIER_ID_HEADER).isBlank()
                                ? UUID.fromString(values.get(SUPPLIER_ID_HEADER))
                                : null;
                final UUID branchId =
                        values.containsKey(BRANCH_ID_HEADER)
                                        && !values.get(BRANCH_ID_HEADER).isBlank()
                                ? UUID.fromString(values.get(BRANCH_ID_HEADER))
                                : defaultBranchId;
                final long quantity =
                        longValue(values.getOrDefault("quantity", values.get("stock")), 0);
                final long reorderLevel = longValue(values.get("reorderlevel"), 0);
                final long maximumStockLevel =
                        longValue(
                                values.getOrDefault(
                                        "maximumstocklevel", values.get("maxstocklevel")),
                                0);
                final BigDecimal cost = decimalValue(values.get("cost"));
                final BigDecimal price = decimalValue(values.get("price"));
                if (quantity < 0
                        || reorderLevel < 0
                        || maximumStockLevel < 0
                        || maximumStockLevel > 0 && maximumStockLevel < reorderLevel
                        || !fitsProductAmount(cost)
                        || !fitsProductAmount(price)) {
                    rejected++;
                    errors.add(
                            CSV_ROW_PREFIX
                                    + (i + 1)
                                    + ": quantity and stock limits must be nonnegative and maximum"
                                    + " must not be below minimum; cost and price must fit 10"
                                    + " digits and 2 decimals");
                    continue;
                }
                if (supplierId != null
                        && suppliers.findByIdAndHotelId(supplierId, hotel).isEmpty()) {
                    rejected++;
                    errors.add(
                            CSV_ROW_PREFIX
                                    + (i + 1)
                                    + ": supplier does not belong to this business");
                    continue;
                }
                if (quantity > 0) {
                    try {
                        branchValidation.requireActiveBranch(hotel, branchId);
                    } catch (final IllegalArgumentException invalidBranch) {
                        rejected++;
                        errors.add(
                                CSV_ROW_PREFIX
                                        + (i + 1)
                                        + ": choose an active branch for the initial stock");
                        continue;
                    }
                }
                productRequest =
                        new ProductRequest(
                                sku,
                                name,
                                description,
                                Boolean.parseBoolean(rawOptional),
                                category,
                                cost,
                                price,
                                reorderLevel,
                                maximumStockLevel,
                                supplierId,
                                quantity,
                                branchId,
                                null);
                seenSkus.add(sku);
            } catch (final IllegalArgumentException ex) {
                rejected++;
                errors.add(CSV_ROW_PREFIX + (i + 1) + ": invalid UUID or numeric value");
                continue;
            }
            product(hotel, productRequest);
            imported++;
        }
        return new ImportResult(imported, rejected, errors);
    }

    private BigDecimal decimalValue(final String value) {
        return value == null || value.isBlank() ? BigDecimal.ZERO : new BigDecimal(value.trim());
    }

    private String generatedImportSku(final Map<String, String> values) {
        final String canonical =
                values.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(entry -> entry.getKey() + "=" + entry.getValue())
                        .collect(Collectors.joining("|"));
        try {
            final byte[] digest =
                    MessageDigest.getInstance("SHA-256")
                            .digest(canonical.getBytes(StandardCharsets.UTF_8));
            return "IMP-"
                    + HexFormat.of().formatHex(digest, 0, 10).toUpperCase(Locale.ROOT);
        } catch (final NoSuchAlgorithmException exception) {
            throw new IllegalStateException("IMPORT_SKU_GENERATION_UNAVAILABLE", exception);
        }
    }

    private boolean fitsProductAmount(final BigDecimal value) {
        final BigDecimal normalized = value.stripTrailingZeros();
        return normalized.signum() >= 0
                && normalized.scale() <= 2
                && normalized.precision() - normalized.scale() <= 10;
    }

    private long longValue(final String value, final long fallback) {
        return value == null || value.isBlank() ? fallback : Long.parseLong(value.trim());
    }

    private List<List<String>> parseCsv(final String csv) {
        final List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        final StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < csv.length(); i++) {
            final char current = csv.charAt(i);
            if (current == '"' && quoted && i + 1 < csv.length() && csv.charAt(i + 1) == '"') {
                cell.append('"');
                i++;
            } else if (current == '"') {
                quoted = !quoted;
            } else if (current == ',' && !quoted) {
                row.add(cell.toString());
                cell.setLength(0);
            } else if ((current == '\n' || current == '\r') && !quoted) {
                if (current == '\r' && i + 1 < csv.length() && csv.charAt(i + 1) == '\n') {
                    i++;
                }
                row.add(cell.toString());
                cell.setLength(0);
                if (row.stream().anyMatch(value -> !value.isBlank())) {
                    rows.add(row);
                }
                row = new ArrayList<>();
            } else {
                cell.append(current);
            }
        }
        row.add(cell.toString());
        if (row.stream().anyMatch(value -> !value.isBlank())) {
            rows.add(row);
        }
        if (quoted) {
            throw new IllegalArgumentException("CSV_UNCLOSED_QUOTE");
        }
        return rows;
    }

    /**
     * Imports the first two columns of an XLSX catalog as SKU and product name.
     *
     * @param hotel tenant identifier
     * @param file uploaded workbook
     * @return import summary, including rejected rows
     * @implSpec Overrides must preserve tenant scope and workbook validation.
     */
    @Transactional
    public ImportResult importXlsx(final UUID hotel, final MultipartFile file) {
        int imported = 0;
        int rejected = 0;
        final List<String> errors = new ArrayList<>();
        try (InputStream input = file.getInputStream();
                var workbook = WorkbookFactory.create(input)) {
            final var sheet = workbook.getSheetAt(0);
            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                final var row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                final String sku = row.getCell(0) == null ? "" : row.getCell(0).toString().trim();
                final String name = row.getCell(1) == null ? "" : row.getCell(1).toString().trim();
                if (rowIndex == 0 && SKU_HEADER.equalsIgnoreCase(sku)) {
                    continue;
                }
                if (sku.isBlank() || name.isBlank()) {
                    rejected++;
                    errors.add(
                            CSV_ROW_PREFIX
                                    + (rowIndex + 1)
                                    + ": "
                                    + SKU_HEADER
                                    + " and "
                                    + NAME_HEADER
                                    + " are required");
                    continue;
                }
                product(hotel, new ProductRequest(sku, name, false));
                imported++;
            }
        } catch (
                final IOException
                        | IllegalArgumentException
                        | IllegalStateException ex) {
            throw new IllegalArgumentException("XLSX_IMPORT_FAILED", ex);
        }
        return new ImportResult(imported, rejected, errors);
    }

    /**
     * Extracts supplier text from a PDF and imports simple SKU/name lines.
     *
     * @param hotel tenant identifier
     * @param file uploaded PDF
     * @return import summary
     * @implSpec Overrides must preserve tenant scope and PDF parsing behavior.
     */
    @Transactional
    public ImportResult importPdf(final UUID hotel, final MultipartFile file) {
        try {
            final byte[] bytes = file.getBytes();
            try (var document = Loader.loadPDF(bytes)) {
                final String text = new PDFTextStripper().getText(document);
                return importCsv(hotel, text.replace('|', ',').replace(';', ','));
            }
        } catch (final IOException | IllegalArgumentException ex) {
            throw new IllegalArgumentException("PDF_IMPORT_FAILED", ex);
        }
    }

    /**
     * Product and quantity to deduct for a confirmed POS order.
     *
     * @param productId tenant-owned inventory product identifier
     * @param quantity positive quantity to consume
     */
    public record PosConsumptionLine(UUID productId, long quantity) { }
}
