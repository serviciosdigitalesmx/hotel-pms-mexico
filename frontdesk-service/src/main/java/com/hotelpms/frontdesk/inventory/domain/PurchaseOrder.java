package com.hotelpms.frontdesk.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Type domain type. */
@Entity
@Table(name = "inventory_purchases")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "hotel_id", nullable = false)
  private UUID hotelId;

  @Column(name = "branch_id", nullable = false)
  private UUID branchId;

  @Column(name = "supplier_id", nullable = false)
  private UUID supplierId;

  @Column(nullable = false, length = 32)
  @Builder.Default
  private String status = "PENDING";

  @Column(name = "service_order_id")
  private UUID serviceOrderId;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
