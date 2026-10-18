package com.hotelpms.frontdesk.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/** A tenant-owned product in the repair inventory catalog. */
@Entity
@Table(
        name = "inventory_products",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uq_inventory_product_hotel_sku",
                        columnNames = {"hotel_id", "sku"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryProduct {
    private static final int SKU_LENGTH = 80;
    private static final int NAME_LENGTH = 200;
    private static final int CATEGORY_LENGTH = 120;
    private static final int DESCRIPTION_LENGTH = 2000;
    private static final int MONEY_PRECISION = 12;
    private static final int MONEY_SCALE = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "hotel_id", nullable = false)
    private UUID hotelId;

    @Column(nullable = false, length = SKU_LENGTH)
    private String sku;

    @Column(nullable = false, length = NAME_LENGTH)
    private String name;

    @Column(length = DESCRIPTION_LENGTH)
    private String description;

    @Column(nullable = false, length = CATEGORY_LENGTH)
    @Builder.Default
    private String category = "General";

    @Column(nullable = false, precision = MONEY_PRECISION, scale = MONEY_SCALE)
    @Builder.Default
    private BigDecimal cost = BigDecimal.ZERO;

    @Column(nullable = false, precision = MONEY_PRECISION, scale = MONEY_SCALE)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "reorder_level", nullable = false)
    private long reorderLevel;

    @Column(name = "maximum_stock_level", nullable = false)
    private long maximumStockLevel;

    @Column(name = "supplier_id")
    private UUID supplierId;

    /** Linked food-and-beverage menu item whose sale consumes this stock product. */
    @Column(name = "pos_menu_item_id")
    private UUID posMenuItemId;

    @Column(nullable = false)
    @Builder.Default
    private boolean optional = true;
}
