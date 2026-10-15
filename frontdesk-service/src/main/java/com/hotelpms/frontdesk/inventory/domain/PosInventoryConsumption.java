package com.hotelpms.frontdesk.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Idempotency record for inventory consumed by a confirmed POS order. */
@Entity
@Table(name = "pos_inventory_consumptions")
@Getter
@Setter
@NoArgsConstructor
public class PosInventoryConsumption {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "hotel_id", nullable = false)
  private UUID hotelId;

  @Column(name = "order_id", nullable = false)
  private UUID orderId;

  @Column(name = "product_id", nullable = false)
  private UUID productId;

  @Column(name = "branch_id", nullable = false)
  private UUID branchId;

  @Column(nullable = false)
  private long quantity;
}
