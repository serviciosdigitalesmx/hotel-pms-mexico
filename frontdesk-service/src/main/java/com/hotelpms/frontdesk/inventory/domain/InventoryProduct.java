package com.hotelpms.frontdesk.inventory.domain;

import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Type domain type. */
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

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "hotel_id", nullable = false)
  private UUID hotelId;

  @Column(nullable = false, length = SKU_LENGTH)
  private String sku;

  @Column(nullable = false, length = NAME_LENGTH)
  private String name;

  @Column(length = 2000)
  private String description;

  @Column(nullable = false, length = CATEGORY_LENGTH)
  @Builder.Default
  private String category = "General";

  @Column(nullable = false, precision = 12, scale = 2)
  @Builder.Default
  private BigDecimal cost = BigDecimal.ZERO;

  @Column(nullable = false, precision = 12, scale = 2)
  @Builder.Default
  private BigDecimal price = BigDecimal.ZERO;

  @Column(name = "reorder_level", nullable = false)
  @Builder.Default
  private long reorderLevel = 0;

  @Column(name = "supplier_id")
  private UUID supplierId;

  @Column(nullable = false)
  @Builder.Default
  private boolean optional = true;
}
