package com.hotelpms.frontdesk.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A planned product and quantity on a supplier purchase order. */
@Entity
@Table(name = "inventory_purchase_lines")
@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrderLine {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "purchase_id", nullable = false)
  private PurchaseOrder purchaseOrder;

  @Column(name = "product_id", nullable = false)
  private UUID productId;

  @Column(name = "quantity_ordered", nullable = false)
  private long quantityOrdered;

  @Column(name = "quantity_received", nullable = false)
  private long quantityReceived;

  @Column(name = "unit_cost", nullable = false, precision = 12, scale = 2)
  private BigDecimal unitCost;
}
