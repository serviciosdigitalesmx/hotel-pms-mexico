package com.hotelpms.frontdesk.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

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
  public record ProductRequest(@NotBlank String sku, @NotBlank String name, boolean optional) { }

  /**
   * DTO record.
   *
   * @param branchId branch identifier.
   * @param productId product identifier.
   * @param delta signed movement quantity.
   */
  public record MovementRequest(
      @NotNull UUID branchId, @NotNull UUID productId, @Min(MIN_MOVEMENT_DELTA) long delta) { }

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
  public record SupplierRequest(@NotBlank String name, String taxId, String contact) { }

  /**
   * DTO record.
   *
   * @param branchId branch identifier.
   * @param supplierId supplier identifier.
   * @param serviceOrderId optional service order identifier.
   */
  public record PurchaseRequest(
      @NotNull UUID branchId, @NotNull UUID supplierId, UUID serviceOrderId) { }

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
