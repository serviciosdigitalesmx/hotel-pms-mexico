package com.hotelpms.frontdesk.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

  @Column(name = "last_received_at")
  private LocalDateTime lastReceivedAt;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "receipt_request_digests", nullable = false, columnDefinition = "jsonb")
  @Builder.Default
  private Map<String, String> receiptRequestDigests = new HashMap<>();

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "receipt_events", nullable = false, columnDefinition = "jsonb")
  @Builder.Default
  private List<Map<String, String>> receiptEvents = new ArrayList<>();

  @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private List<PurchaseOrderLine> lines = new ArrayList<>();

  /** Keeps old purchase rows and pre-migration API consumers readable. */
  public List<PurchaseOrderLine> getLines() { return lines; }
}
