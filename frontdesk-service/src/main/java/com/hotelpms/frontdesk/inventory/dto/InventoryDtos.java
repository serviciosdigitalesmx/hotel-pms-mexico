package com.hotelpms.frontdesk.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Validated request and response records for Fixi inventory and procurement APIs.
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class InventoryDtos {
    private static final int MIN_MOVEMENT_DELTA = -1_000_000;
    private static final int MAX_PRODUCT_SKU_LENGTH = 80;
    private static final int MAX_PRODUCT_NAME_LENGTH = 200;
    private static final int MAX_PRODUCT_DESCRIPTION_LENGTH = 2000;
    private static final int MAX_CATEGORY_LENGTH = 120;
    private static final int MAX_BULK_PRODUCTS = 500;
    private static final int MAX_REASON_LENGTH = 500;
    private static final int MAX_TAX_ID_LENGTH = 20;
    private static final int MAX_EMAIL_LENGTH = 254;
    private static final int MAX_PHONE_LENGTH = 40;
    private static final int MAX_WEBSITE_LENGTH = 500;
    private static final int MAX_SUPPLIER_DESCRIPTION_LENGTH = 1000;
    private static final int MAX_PURCHASE_LINES = 200;

    private InventoryDtos() { }

    /**
     * Inventory product fields including commercial price and optional initial stock.
     *
     * @param sku tenant-unique product code
     * @param name product display name
     * @param description optional product description
     * @param optional whether the product is optional on service orders
     * @param category product category
     * @param cost purchase cost
     * @param price sale price
     * @param reorderLevel low-stock threshold
     * @param maximumStockLevel optional maximum stock threshold
     * @param supplierId optional tenant-owned supplier
     * @param initialQuantity initial units to receive
     * @param branchId branch receiving initial units
     * @param posMenuItemId optional linked POS menu item
     */
    public record ProductRequest(
            @NotBlank @Size(max = MAX_PRODUCT_SKU_LENGTH) String sku,
            @NotBlank @Size(max = MAX_PRODUCT_NAME_LENGTH) String name,
            @Size(max = MAX_PRODUCT_DESCRIPTION_LENGTH) String description,
            boolean optional,
            @NotBlank @Size(max = MAX_CATEGORY_LENGTH) String category,
            @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal cost,
            @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal price,
            @Min(0) long reorderLevel,
            @Min(0) long maximumStockLevel,
            UUID supplierId,
            @Min(0) long initialQuantity,
            UUID branchId,
            UUID posMenuItemId) {
        /**
         * Validates that a configured maximum is not below the reorder threshold.
         */
        @AssertTrue(message = "MAXIMUM_STOCK_LEVEL_BELOW_REORDER_LEVEL")
        public boolean isStockLimitsRangeValid() {
            return maximumStockLevel == 0 || maximumStockLevel >= reorderLevel;
        }

        /**
         * Creates a product request without a maximum stock limit.
         *
         * @param sku product code
         * @param name display name
         * @param description optional description
         * @param optional whether it is optional on an order
         * @param category product category
         * @param cost purchase cost
         * @param price sale price
         * @param reorderLevel low-stock threshold
         * @param supplierId tenant-owned supplier
         * @param initialQuantity initial units
         * @param branchId branch receiving initial units
         * @param posMenuItemId linked POS menu item
         */
        public ProductRequest(
                final String sku,
                final String name,
                final String description,
                final boolean optional,
                final String category,
                final BigDecimal cost,
                final BigDecimal price,
                final long reorderLevel,
                final UUID supplierId,
                final long initialQuantity,
                final UUID branchId,
                final UUID posMenuItemId) {
            this(
                    sku,
                    name,
                    description,
                    optional,
                    category,
                    cost,
                    price,
                    reorderLevel,
                    0,
                    supplierId,
                    initialQuantity,
                    branchId,
                    posMenuItemId);
        }

        /**
         * Creates a product request without a maximum threshold or POS menu link.
         *
         * @param sku product code
         * @param name display name
         * @param description optional description
         * @param optional whether it is optional on an order
         * @param category product category
         * @param cost purchase cost
         * @param price sale price
         * @param reorderLevel low-stock threshold
         * @param supplierId tenant-owned supplier
         * @param initialQuantity initial units
         * @param branchId branch receiving initial units
         */
        public ProductRequest(
                final String sku,
                final String name,
                final String description,
                final boolean optional,
                final String category,
                final BigDecimal cost,
                final BigDecimal price,
                final long reorderLevel,
                final UUID supplierId,
                final long initialQuantity,
                final UUID branchId) {
            this(
                    sku,
                    name,
                    description,
                    optional,
                    category,
                    cost,
                    price,
                    reorderLevel,
                    0,
                    supplierId,
                    initialQuantity,
                    branchId,
                    null);
        }

        /**
         * Creates a product request without description, maximum threshold, or POS link.
         *
         * @param sku product code
         * @param name display name
         * @param optional whether it is optional on an order
         * @param category product category
         * @param cost purchase cost
         * @param price sale price
         * @param reorderLevel low-stock threshold
         * @param supplierId tenant-owned supplier
         * @param initialQuantity initial units
         * @param branchId branch receiving initial units
         */
        public ProductRequest(
                final String sku,
                final String name,
                final boolean optional,
                final String category,
                final BigDecimal cost,
                final BigDecimal price,
                final long reorderLevel,
                final UUID supplierId,
                final long initialQuantity,
                final UUID branchId) {
            this(
                    sku,
                    name,
                    null,
                    optional,
                    category,
                    cost,
                    price,
                    reorderLevel,
                    0,
                    supplierId,
                    initialQuantity,
                    branchId,
                    null);
        }

        /**
         * Creates a product request without description, supplier, initial stock, or branch.
         *
         * @param sku product code
         * @param name display name
         * @param optional whether it is optional on an order
         * @param category product category
         * @param cost purchase cost
         * @param price sale price
         * @param reorderLevel low-stock threshold
         */
        public ProductRequest(
                final String sku,
                final String name,
                final boolean optional,
                final String category,
                final BigDecimal cost,
                final BigDecimal price,
                final long reorderLevel) {
            this(
                    sku,
                    name,
                    null,
                    optional,
                    category,
                    cost,
                    price,
                    reorderLevel,
                    0,
                    null,
                    0,
                    null,
                    null);
        }

        /**
         * Creates a minimal product request with general category and zero prices.
         *
         * @param sku product code
         * @param name display name
         * @param optional whether it is optional on an order
         */
        public ProductRequest(final String sku, final String name, final boolean optional) {
            this(
                    sku,
                    name,
                    null,
                    optional,
                    "General",
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0,
                    0,
                    null,
                    0,
                    null,
                    null);
        }
    }

    /**
     * Bounded bulk percentage adjustment for selected product sale prices.
     *
     * @param productIds tenant-owned products to update
     * @param percentage percentage increase from zero through the supported maximum
     */
    public record BulkPriceRequest(
            @NotNull @Size(min = 1, max = MAX_BULK_PRODUCTS) List<@NotNull UUID> productIds,
            @NotNull
                    @DecimalMin("0.00")
                    @DecimalMax("1000.00")
                    @Digits(integer = 4, fraction = 2)
                    BigDecimal percentage) { }

    /**
     * Audited inventory adjustment for a product at one branch.
     *
     * @param branchId branch identifier
     * @param productId product identifier
     * @param delta signed movement quantity
     * @param reason adjustment reason retained in the audit trail
     */
    public record MovementRequest(
            @NotNull UUID branchId,
            @NotNull UUID productId,
            @Min(MIN_MOVEMENT_DELTA) long delta,
            @NotBlank @Size(max = MAX_REASON_LENGTH) String reason) {
        /**
         * Creates a movement request without a reason for compatibility with legacy callers.
         *
         * @param branchId stock branch identifier
         * @param productId tenant-owned product identifier
         * @param delta signed quantity adjustment
         */
        public MovementRequest(final UUID branchId, final UUID productId, final long delta) {
            this(branchId, productId, delta, null);
        }
    }

    /**
     * Audited physical stock count that replaces the current branch quantity.
     *
     * @param branchId branch where stock was counted
     * @param productId tenant-owned product identifier
     * @param countedQuantity observed on-hand quantity
     * @param reason reason for the adjustment
     * @param expectedCurrentQuantity optional stale-write guard
     */
    public record StockCountRequest(
            @NotNull UUID branchId,
            @NotNull UUID productId,
            @Min(0) long countedQuantity,
            @NotBlank @Size(max = MAX_REASON_LENGTH) String reason,
            @PositiveOrZero Long expectedCurrentQuantity) {
        /**
         * Creates an unconditional stock count without an expected-current guard.
         *
         * @param branchId stock branch identifier
         * @param productId tenant-owned product identifier
         * @param countedQuantity physically counted quantity
         * @param reason adjustment reason
         */
        public StockCountRequest(
                final UUID branchId,
                final UUID productId,
                final long countedQuantity,
                final String reason) {
            this(branchId, productId, countedQuantity, reason, null);
        }
    }

    /**
     * Reserves a quantity of a product for a service order at a specific branch.
     *
     * @param branchId stock branch identifier
     * @param productId tenant-owned product identifier
     * @param serviceOrderId tenant-owned service-order identifier
     * @param quantity positive quantity to reserve
     */
    public record ReservationRequest(
            @NotNull UUID branchId,
            @NotNull UUID productId,
            @NotNull UUID serviceOrderId,
            @Positive long quantity) { }

    /**
     * Supplier contact and catalog metadata scoped to the current tenant.
     *
     * @param name supplier display name
     * @param taxId tax identifier
     * @param contact general contact details
     * @param contactName named contact person
     * @param email supplier email address
     * @param phone supplier phone number
     * @param website HTTP or HTTPS supplier website
     * @param description optional supplier notes
     */
    public record SupplierRequest(
            @NotBlank @Size(max = MAX_PRODUCT_NAME_LENGTH) String name,
            @Size(max = MAX_TAX_ID_LENGTH) String taxId,
            @Size(max = MAX_PRODUCT_NAME_LENGTH) String contact,
            @Size(max = MAX_PRODUCT_NAME_LENGTH) String contactName,
            @Email @Size(max = MAX_EMAIL_LENGTH) String email,
            @Size(max = MAX_PHONE_LENGTH) String phone,
            @Pattern(
                            regexp = "|https?://.+",
                            message = "WEBSITE_MUST_USE_HTTP_OR_HTTPS")
                    @Size(max = MAX_WEBSITE_LENGTH) String website,
            @Size(max = MAX_SUPPLIER_DESCRIPTION_LENGTH) String description) {
        /**
         * Creates a supplier request with the commonly required contact fields.
         *
         * @param name supplier name
         * @param taxId tax identifier
         * @param contact general contact details
         */
        public SupplierRequest(final String name, final String taxId, final String contact) {
            this(name, taxId, contact, null, null, null, null, null);
        }
    }

    /**
     * Purchase order header and lines for a supplier delivery at one branch.
     *
     * @param branchId receiving branch identifier
     * @param supplierId tenant-owned supplier identifier
     * @param serviceOrderId optional associated service order
     * @param lines requested product quantities and unit costs
     */
    public record PurchaseRequest(
            @NotNull UUID branchId,
            @NotNull UUID supplierId,
            UUID serviceOrderId,
            @NotNull @Size(min = 1, max = MAX_PURCHASE_LINES) List<@NotNull PurchaseLineRequest> lines) {
        /**
         * Creates a purchase request with no lines for legacy service callers.
         *
         * @param branchId receiving branch
         * @param supplierId tenant-owned supplier
         * @param serviceOrderId optional associated service order
         */
        public PurchaseRequest(
                final UUID branchId, final UUID supplierId, final UUID serviceOrderId) {
            this(branchId, supplierId, serviceOrderId, List.of());
        }
    }

    /**
     * Purchase order header with a non-empty set of requested lines.
     *
     * @param branchId receiving branch identifier
     * @param supplierId tenant-owned supplier identifier
     * @param serviceOrderId optional associated service order
     * @param lines requested product quantities and unit costs
     */
    public record PurchaseRequestWithLines(
            @NotNull UUID branchId,
            @NotNull UUID supplierId,
            UUID serviceOrderId,
            @NotNull @Size(min = 1, max = MAX_PURCHASE_LINES) List<@NotNull PurchaseLineRequest> lines) { }

    /**
     * One product and quantity requested from a supplier.
     *
     * @param productId tenant-owned inventory product identifier
     * @param quantity number of units ordered
     * @param unitCost agreed cost per unit
     */
    public record PurchaseLineRequest(
            @NotNull UUID productId,
            @Positive long quantity,
            @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal unitCost) { }

    /**
     * Quantity received for one purchase-order product.
     *
     * @param productId purchase-order product identifier
     * @param quantity positive quantity received
     */
    public record ReceiptLine(@NotNull UUID productId, @Positive long quantity) { }

    /**
     * Idempotent request to record a supplier receipt.
     *
     * @param requestId optional idempotency key
     * @param lines quantities received per product
     */
    public record PurchaseReceiptRequest(
            UUID requestId, @NotNull @Size(min = 1) List<@NotNull @Valid ReceiptLine> lines) {
        /**
         * Creates a receipt request without an explicit idempotency identifier.
         *
         * @param lines quantities received per product
         */
        public PurchaseReceiptRequest(final List<ReceiptLine> lines) {
            this(null, lines);
        }
    }

    /**
     * Corrected received quantity for one product after a prior receipt.
     *
     * @param productId purchase-order product identifier
     * @param quantityReceived corrected cumulative quantity received
     */
    public record ReceiptCorrectionLine(
            @NotNull UUID productId,
            @PositiveOrZero long quantityReceived) { }

    /**
     * Idempotent correction payload for quantities previously received.
     *
     * @param lines corrected cumulative quantities per product
     */
    public record PurchaseReceiptCorrectionRequest(
            @NotNull @Size(min = 1) List<@NotNull @Valid ReceiptCorrectionLine> lines) { }

    /**
     * Product summary ranked by quantity consumed over a reporting period.
     *
     * @param productId tenant-owned product identifier
     * @param sku product code
     * @param name product display name
     * @param quantity total quantity consumed
     */
    public record ProductConsumption(UUID productId, String sku, String name, long quantity) { }

    /**
     * Summary of accepted and rejected catalog import rows.
     *
     * @param imported number of imported rows
     * @param rejected number of rejected rows
     * @param errors row-level validation messages
     */
    public record ImportResult(int imported, int rejected, List<String> errors) { }
}
