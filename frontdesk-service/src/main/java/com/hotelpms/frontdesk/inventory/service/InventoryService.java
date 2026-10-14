package com.hotelpms.frontdesk.inventory.service;

import com.hotelpms.frontdesk.inventory.domain.InventoryBalance;
import com.hotelpms.frontdesk.audit.domain.AuditEvent;
import com.hotelpms.frontdesk.audit.service.AuditEventService;
import com.hotelpms.frontdesk.inventory.domain.InventoryProduct;
import com.hotelpms.frontdesk.inventory.repository.SupplierRepository;
import com.hotelpms.frontdesk.inventory.domain.StockReservation;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.ImportResult;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.ProductRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.MovementRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.StockCountRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.ReservationRequest;
import com.hotelpms.frontdesk.inventory.repository.InventoryBalanceRepository;
import com.hotelpms.frontdesk.inventory.repository.InventoryProductRepository;
import com.hotelpms.frontdesk.inventory.repository.StockReservationRepository;
import com.hotelpms.frontdesk.client.BranchValidationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.io.IOException;
import java.io.InputStream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Type domain type. */
@Service
@RequiredArgsConstructor
public class InventoryService {
  private static final String INSUFFICIENT_STOCK = "INSUFFICIENT_STOCK";
  private final InventoryProductRepository products;
  private final InventoryBalanceRepository balances;
  private final AuditEventService auditEvents;
  private final StockReservationRepository reservations;
  private final SupplierRepository suppliers;
  private final BranchValidationService branchValidation;

  /**
   * Creates an inventory product.
   *
   * @param hotel tenant identifier
   * @param r product request
   * @return persisted product
   */
  @Transactional
  public InventoryProduct product(final UUID hotel, final ProductRequest r) {
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
    if (r.supplierId() != null) {
      suppliers.findByIdAndHotelId(r.supplierId(), hotel)
          .orElseThrow(() -> new IllegalArgumentException("SUPPLIER_NOT_FOUND"));
    }
    p.setSupplierId(r.supplierId());
    final InventoryProduct saved = products.save(p);
    if (r.initialQuantity() > 0) {
      if (r.branchId() == null) {
        throw new IllegalArgumentException("INITIAL_STOCK_BRANCH_REQUIRED");
      }
      move(hotel, new MovementRequest(r.branchId(), saved.getId(), r.initialQuantity(), "Initial product stock"));
    }
    return saved;
  }

  /** Updates commercial catalog fields without changing branch stock. */
  @Transactional
  public InventoryProduct updateProduct(final UUID hotel, final UUID productId, final ProductRequest r) {
    final InventoryProduct product = products.findByIdAndHotelId(productId, hotel)
        .orElseThrow(() -> new IllegalArgumentException("PRODUCT_NOT_FOUND"));
    if (r.supplierId() != null) {
      suppliers.findByIdAndHotelId(r.supplierId(), hotel)
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
    product.setSupplierId(r.supplierId());
    return products.save(product);
  }

  /** Applies a validated percentage increase to the selected tenant-owned sale prices. */
  @Transactional
  public List<InventoryProduct> increasePrices(
      final UUID hotel, final List<UUID> productIds, final java.math.BigDecimal percentage) {
    final List<InventoryProduct> selected = products.findAllById(productIds).stream()
        .filter(product -> hotel.equals(product.getHotelId())).toList();
    if (selected.size() != productIds.stream().distinct().count()) {
      throw new IllegalArgumentException("PRODUCT_NOT_FOUND");
    }
    final java.math.BigDecimal multiplier = java.math.BigDecimal.ONE.add(
        percentage.divide(java.math.BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
    selected.forEach(product -> product.setPrice(
        product.getPrice().multiply(multiplier).setScale(2, RoundingMode.HALF_UP)));
    return products.saveAll(selected);
  }

  /**
   * Applies an inventory movement.
   *
   * @param hotel tenant identifier
   * @param r movement request
   * @return updated balance
   */
  @Transactional
  public InventoryBalance move(final UUID hotel, final MovementRequest r) {
    branchValidation.requireActiveBranch(hotel, r.branchId());
    final InventoryProduct p =
        products
            .findForUpdateByIdAndHotelId(r.productId(), hotel)
            .orElseThrow(() -> new IllegalArgumentException("PRODUCT_NOT_FOUND"));
    final InventoryBalance b =
        balances
            .findByHotelIdAndBranchIdAndProductId(hotel, r.branchId(), p.getId())
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
    final String reason = r.reason() == null || r.reason().isBlank()
        ? "Manual stock adjustment" : r.reason().trim();
    final String escapedReason = escapeJson(reason);
    auditEvents.record(hotel, null, AuditEvent.Category.INVENTORY, "INVENTORY_MOVEMENT_RECORDED",
        saved.getId(), null, "{\"branchId\":\"" + r.branchId() + "\",\"productId\":\""
            + r.productId() + "\",\"delta\":" + r.delta() + ",\"previous\":"
            + (next - r.delta()) + ",\"quantity\":" + next + ",\"reason\":\""
            + escapedReason + "\"}");
    return saved;
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
          if (character < 0x20) escaped.append(String.format("\\u%04x", (int) character));
          else escaped.append(character);
        }
      }
    }
    return escaped.toString();
  }

  /** Sets a physically counted stock quantity and retains the adjustment reason in audit. */
  @Transactional
  public InventoryBalance count(final UUID hotel, final StockCountRequest request) {
    branchValidation.requireActiveBranch(hotel, request.branchId());
    final InventoryProduct product = products.findForUpdateByIdAndHotelId(request.productId(), hotel)
        .orElseThrow(() -> new IllegalArgumentException("PRODUCT_NOT_FOUND"));
    final InventoryBalance balance = balances.findByHotelIdAndBranchIdAndProductId(
        hotel, request.branchId(), product.getId()).orElseGet(() -> InventoryBalance.builder()
            .hotelId(hotel).branchId(request.branchId()).productId(product.getId()).build());
    final long previous = balance.getQuantity();
    balance.setQuantity(request.countedQuantity());
    final InventoryBalance saved = balances.save(balance);
    auditEvents.record(hotel, null, AuditEvent.Category.INVENTORY, "INVENTORY_STOCK_COUNTED",
        saved.getId(), null, "{\"branchId\":\"" + request.branchId()
            + "\",\"productId\":\"" + request.productId() + "\",\"previous\":"
            + previous + ",\"counted\":" + request.countedQuantity()
            + ",\"reason\":\"" + request.reason().replace("\\", "\\\\")
                .replace("\"", "\\\"") + "\"}");
    return saved;
  }

  /**
   * Reserves stock for a service order.
   *
   * @param hotel tenant identifier
   * @param r reservation request
   * @return reservation
   */
  @Transactional
  public StockReservation reserve(final UUID hotel, final ReservationRequest r) {
    branchValidation.requireActiveBranch(hotel, r.branchId());
    final Optional<StockReservation> existing =
        reservations.findByHotelIdAndServiceOrderIdAndProductId(
            hotel, r.serviceOrderId(), r.productId());
    if (existing.isPresent()) {
      return existing.get();
    }
    final InventoryBalance b =
        balances
            .findByHotelIdAndBranchIdAndProductId(hotel, r.branchId(), r.productId())
            .orElseThrow(() -> new IllegalStateException(INSUFFICIENT_STOCK));
    if (b.getQuantity() < r.quantity()) {
      throw new IllegalStateException(INSUFFICIENT_STOCK);
    }
    b.setQuantity(b.getQuantity() - r.quantity());
    balances.save(b);
    return reservations.save(
        StockReservation.builder()
            .hotelId(hotel)
            .branchId(r.branchId())
            .productId(r.productId())
            .serviceOrderId(r.serviceOrderId())
            .quantity(r.quantity())
            .createdAt(java.time.LocalDateTime.now())
            .build());
  }

  /**
   * Releases a stock reservation.
   *
   * @param hotel tenant identifier
   * @param order service order identifier
   * @param product product identifier
   * @return released reservation
   */
  @Transactional
  public StockReservation release(final UUID hotel, final UUID order, final UUID product) {
    final StockReservation r =
        reservations
            .findByHotelIdAndServiceOrderIdAndProductId(hotel, order, product)
            .orElseThrow(() -> new IllegalArgumentException("RESERVATION_NOT_FOUND"));
    if ("RELEASED".equals(r.getStatus())) {
      return r;
    }
    final InventoryBalance b =
        balances
            .findByHotelIdAndBranchIdAndProductId(hotel, r.getBranchId(), r.getProductId())
            .orElseThrow();
    b.setQuantity(b.getQuantity() + r.getQuantity());
    balances.save(b);
    r.setStatus("RELEASED");
    return reservations.save(r);
  }

  /**
   * Lists products for a tenant.
   *
   * @param hotel tenant identifier
   * @return products
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
   */
  @Transactional
  public ImportResult importCsv(final UUID hotel, final String csv) {
    return importCsv(hotel, csv, null);
  }

  @Transactional
  public ImportResult importCsv(final UUID hotel, final String csv, final UUID defaultBranchId) {
    int imported = 0;
    int rejected = 0;
    final List<String> errors = new ArrayList<>();
    final java.util.Set<String> seenSkus = new java.util.HashSet<>();
    final List<List<String>> rows = parseCsv(csv == null ? "" : csv.replaceFirst("^\\uFEFF", ""));
    List<String> headers = List.of("sku", "name", "optional");
    int firstDataRow = 0;
    if (!rows.isEmpty() && rows.get(0).stream().map(value -> value.trim().toLowerCase(Locale.ROOT)
        .replaceAll("[\\s_-]+", "")).anyMatch(value -> List.of(
            "sku", "name", "product", "productname", "category", "description", "quantity", "stock")
            .contains(value))) {
      headers = rows.get(0).stream().map(value -> value.trim().toLowerCase(Locale.ROOT)
          .replaceAll("[\\s_-]+", "")).toList();
      firstDataRow = 1;
    }
    for (int i = firstDataRow; i < rows.size(); i++) {
      final List<String> cells = rows.get(i);
      if (cells.size() > headers.size()) {
        rejected++;
        errors.add("row " + (i + 1) + ": more columns than the CSV header");
        continue;
      }
      final java.util.Map<String, String> values = new java.util.HashMap<>();
      for (int column = 0; column < headers.size() && column < cells.size(); column++) {
        values.put(headers.get(column), cells.get(column).trim());
      }
      final String suppliedSku = values.getOrDefault("sku", "");
      final String name = values.getOrDefault("name", values.getOrDefault("product", values.getOrDefault("productname", "")));
      if (name.isBlank()) {
        rejected++;
        errors.add("row " + (i + 1) + ": product name is required");
        continue;
      }
      final String category = values.getOrDefault("category", "General").isBlank()
          ? "General" : values.get("category");
      final String description = values.getOrDefault("description", "");
      final String rawOptional = values.getOrDefault("optional", "false");
      if (name.length() > 200 || description.length() > 2000 || category.length() > 120) {
        rejected++;
        errors.add("row " + (i + 1) + ": name, description, or category exceeds its maximum length");
        continue;
      }
      if (!"true".equalsIgnoreCase(rawOptional) && !"false".equalsIgnoreCase(rawOptional)) {
        rejected++;
        errors.add("row " + (i + 1) + ": optional must be true or false");
        continue;
      }
      final ProductRequest productRequest;
      try {
        final String sku = suppliedSku.isBlank()
            ? "IMP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase(Locale.ROOT)
            : suppliedSku;
        if (sku.length() > 80 || products.findByHotelIdAndSku(hotel, sku).isPresent()
            || seenSkus.contains(sku)) {
          rejected++;
          errors.add("row " + (i + 1) + ": SKU is too long or already exists");
          continue;
        }
        final UUID supplierId = values.containsKey("supplierid") && !values.get("supplierid").isBlank()
            ? UUID.fromString(values.get("supplierid")) : null;
        final UUID branchId = values.containsKey("branchid") && !values.get("branchid").isBlank()
            ? UUID.fromString(values.get("branchid")) : defaultBranchId;
        final long quantity = longValue(values.getOrDefault("quantity", values.get("stock")), 0);
        final long reorderLevel = longValue(values.get("reorderlevel"), 0);
        final BigDecimal cost = decimalValue(values.get("cost"));
        final BigDecimal price = decimalValue(values.get("price"));
        if (quantity < 0 || reorderLevel < 0 || !fitsProductAmount(cost) || !fitsProductAmount(price)) {
          rejected++;
          errors.add("row " + (i + 1) + ": quantity and minimum stock must be nonnegative; cost and price must fit 10 digits and 2 decimals");
          continue;
        }
        if (supplierId != null && suppliers.findByIdAndHotelId(supplierId, hotel).isEmpty()) {
          rejected++;
          errors.add("row " + (i + 1) + ": supplier does not belong to this business");
          continue;
        }
        if (quantity > 0) {
          try {
            branchValidation.requireActiveBranch(hotel, branchId);
          } catch (IllegalArgumentException invalidBranch) {
            rejected++;
            errors.add("row " + (i + 1) + ": choose an active branch for the initial stock");
            continue;
          }
        }
        productRequest = new ProductRequest(sku, name, description,
            Boolean.parseBoolean(rawOptional), category, cost, price, reorderLevel,
            supplierId, quantity, branchId);
        seenSkus.add(sku);
      } catch (final IllegalArgumentException ex) {
        rejected++;
        errors.add("row " + (i + 1) + ": invalid UUID or numeric value");
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

  private boolean fitsProductAmount(final BigDecimal value) {
    final BigDecimal normalized = value.stripTrailingZeros();
    return normalized.signum() >= 0 && normalized.scale() <= 2
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
        cell.append('"'); i++;
      } else if (current == '"') {
        quoted = !quoted;
      } else if (current == ',' && !quoted) {
        row.add(cell.toString()); cell.setLength(0);
      } else if ((current == '\n' || current == '\r') && !quoted) {
        if (current == '\r' && i + 1 < csv.length() && csv.charAt(i + 1) == '\n') i++;
        row.add(cell.toString()); cell.setLength(0);
        if (row.stream().anyMatch(value -> !value.isBlank())) rows.add(row);
        row = new ArrayList<>();
      } else {
        cell.append(current);
      }
    }
    row.add(cell.toString());
    if (row.stream().anyMatch(value -> !value.isBlank())) rows.add(row);
    if (quoted) throw new IllegalArgumentException("CSV_UNCLOSED_QUOTE");
    return rows;
  }

  /** Imports the first two columns of an XLSX catalog as SKU and product name. */
  @Transactional
  public ImportResult importXlsx(final UUID hotel, final MultipartFile file) {
    int imported = 0;
    int rejected = 0;
    final List<String> errors = new ArrayList<>();
    try (InputStream input = file.getInputStream(); var workbook = WorkbookFactory.create(input)) {
      final var sheet = workbook.getSheetAt(0);
      for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
        final var row = sheet.getRow(rowIndex);
        if (row == null) {
          continue;
        }
        final String sku = row.getCell(0) == null ? "" : row.getCell(0).toString().trim();
        final String name = row.getCell(1) == null ? "" : row.getCell(1).toString().trim();
        if (rowIndex == 0 && "sku".equalsIgnoreCase(sku)) {
          continue;
        }
        if (sku.isBlank() || name.isBlank()) {
          rejected++;
          errors.add("row " + (rowIndex + 1) + ": sku and name are required");
          continue;
        }
        product(hotel, new ProductRequest(sku, name, false));
        imported++;
      }
    } catch (final IOException | RuntimeException ex) {
      throw new IllegalArgumentException("XLSX_IMPORT_FAILED", ex);
    }
    return new ImportResult(imported, rejected, errors);
  }

  /** Extracts supplier text from a PDF and imports simple SKU/name lines. */
  @Transactional
  public ImportResult importPdf(final UUID hotel, final MultipartFile file) {
    try {
      final byte[] bytes = file.getBytes();
      try (var document = Loader.loadPDF(bytes)) {
        final String text = new PDFTextStripper().getText(document);
        return importCsv(hotel, text.replace('|', ',').replace(';', ','));
      }
    } catch (final IOException | RuntimeException ex) {
      throw new IllegalArgumentException("PDF_IMPORT_FAILED", ex);
    }
  }
}
