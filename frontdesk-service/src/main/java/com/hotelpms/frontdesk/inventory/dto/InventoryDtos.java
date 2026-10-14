package com.hotelpms.frontdesk.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;
import java.math.BigDecimal;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

/** InventoryDtos domain type. */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class InventoryDtos {
  private static final int MIN_MOVEMENT_DELTA = -1_000_000;

  private InventoryDtos() { }

  /**
   * DTO record.
   *
   * @param sku value.
   *
   * @param name value.
   *
   * @param optional value.
   */
  public record ProductRequest(
      @NotBlank @Size(max = 80) String sku,
      @NotBlank @Size(max = 200) String name,
      @Size(max = 2000) String description,
      boolean optional,
      @NotBlank @Size(max = 120) String category,
      @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal cost,
      @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal price,
      @Min(0) long reorderLevel,
      UUID supplierId,
      @Min(0) long initialQuantity,
      UUID branchId) {
    public ProductRequest(final String sku, final String name, final boolean optional,
        final String category, final BigDecimal cost, final BigDecimal price,
        final long reorderLevel, final UUID supplierId, final long initialQuantity,
        final UUID branchId) {
      this(sku, name, null, optional, category, cost, price, reorderLevel, supplierId,
          initialQuantity, branchId);
    }
    public ProductRequest(final String sku, final String name, final boolean optional,
        final String category, final BigDecimal cost, final BigDecimal price,
        final long reorderLevel) {
      this(sku, name, null, optional, category, cost, price, reorderLevel, null, 0, null);
    }
    public ProductRequest(final String sku, final String name, final boolean optional) {
      this(sku, name, null, optional, "General", BigDecimal.ZERO, BigDecimal.ZERO, 0, null, 0, null);
    }
  }

  public record BulkPriceRequest(
      @NotNull @jakarta.validation.constraints.Size(min = 1, max = 500) java.util.List<@NotNull UUID> productIds,
      @NotNull @DecimalMin("0.00") @jakarta.validation.constraints.DecimalMax("1000.00")
          @Digits(integer = 4, fraction = 2) BigDecimal percentage) { }

  /**
   * DTO record.
   *
   * @param branchId branch identifier.
   * @param productId product identifier.
   * @param delta signed movement quantity.
   */
  public record MovementRequest(
      @NotNull UUID branchId, @NotNull UUID productId, @Min(MIN_MOVEMENT_DELTA) long delta,
      @jakarta.validation.constraints.Size(max = 500) String reason) {
    public MovementRequest(final UUID branchId, final UUID productId, final long delta) {
      this(branchId, productId, delta, null);
    }
  }

  /** Audited physical stock count that replaces the current branch quantity. */
  public record StockCountRequest(
      @NotNull UUID branchId, @NotNull UUID productId, @Min(0) long countedQuantity,
      @NotBlank @Size(max = 500) String reason) { }

  /**
   * DTO record.
   *
   * @param branchId value.
   *
   * @param productId value.
   *
   * @param serviceOrderId value.
   *
   * @param quantity value.
   */
  public record ReservationRequest(
      @NotNull UUID branchId,
      @NotNull UUID productId,
      @NotNull UUID serviceOrderId,
      @Positive long quantity) { }

  /**
   * DTO record.
   *
   * @param name value.
   *
   * @param taxId value.
   *
   * @param contact value.
   */
  public record SupplierRequest(
      @NotBlank @jakarta.validation.constraints.Size(max = 200) String name,
      @jakarta.validation.constraints.Size(max = 20) String taxId,
      @jakarta.validation.constraints.Size(max = 200) String contact,
      @jakarta.validation.constraints.Size(max = 200) String contactName,
      @jakarta.validation.constraints.Email @jakarta.validation.constraints.Size(max = 254) String email,
      @jakarta.validation.constraints.Size(max = 40) String phone,
      @jakarta.validation.constraints.Size(max = 500) String website,
      @jakarta.validation.constraints.Size(max = 1000) String description) {
    public SupplierRequest(final String name, final String taxId, final String contact) {
      this(name, taxId, contact, null, null, null, null, null);
    }
  }

  /**
   * DTO record.
   *
   * @param branchId branch identifier.
   * @param supplierId supplier identifier.
   * @param serviceOrderId optional service order identifier.
   */
  public record PurchaseRequest(
      @NotNull UUID branchId, @NotNull UUID supplierId, UUID serviceOrderId,
      @NotNull @jakarta.validation.constraints.Size(min = 1, max = 200)
          java.util.List<@NotNull PurchaseLineRequest> lines) {
    public PurchaseRequest(final UUID branchId, final UUID supplierId, final UUID serviceOrderId) {
      this(branchId, supplierId, serviceOrderId, java.util.List.of());
    }
  }

  public record PurchaseRequestWithLines(
      @NotNull UUID branchId, @NotNull UUID supplierId, UUID serviceOrderId,
      @NotNull @jakarta.validation.constraints.Size(min = 1, max = 200)
          java.util.List<@NotNull PurchaseLineRequest> lines) { }

  public record PurchaseLineRequest(@NotNull UUID productId, @Positive long quantity,
      @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal unitCost) { }

  public record ReceiptLine(@NotNull UUID productId, @Positive long quantity) { }

  public record PurchaseReceiptRequest(@NotNull java.util.List<@NotNull ReceiptLine> lines) { }

  /**
   * DTO record.
   *
   * @param imported value.
   *
   * @param rejected value.
   *
   * @param errors value.
   */
  public record ImportResult(int imported, int rejected, java.util.List<String> errors) { }
}
