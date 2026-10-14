package com.hotelpms.frontdesk.inventory.domain;

import jakarta.persistence.UniqueConstraint;
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
@Table(
    name = "inventory_reservations",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_inventory_reservation_order_product",
            columnNames = {"service_order_id", "product_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReservation {
  private static final int STATUS_LENGTH = 20;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "hotel_id", nullable = false)
  private UUID hotelId;

  @Column(name = "branch_id", nullable = false)
  private UUID branchId;

  @Column(name = "product_id", nullable = false)
  private UUID productId;

  @Column(name = "service_order_id", nullable = false)
  private UUID serviceOrderId;

  @Column(nullable = false)
  private long quantity;

  @Column(nullable = false, length = STATUS_LENGTH)
  @Builder.Default
  private String status = "RESERVED";

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "consumed_at")
  private LocalDateTime consumedAt;
}
