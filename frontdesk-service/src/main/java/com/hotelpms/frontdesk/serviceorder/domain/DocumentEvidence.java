package com.hotelpms.frontdesk.serviceorder.domain;

import jakarta.persistence.Index;
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
    name = "document_evidence",
    indexes =
        @Index(
            name = "idx_document_evidence_hotel_order",
            columnList = "hotel_id,service_order_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEvidence {
  private static final int FILE_NAME_LENGTH = 200;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "hotel_id", nullable = false)
  private UUID hotelId;

  @Column(name = "service_order_id")
  private UUID serviceOrderId;

  @Column(name = "customer_id")
  private UUID customerId;

  @Column(nullable = false, length = FILE_NAME_LENGTH)
  private String fileName;

  @Column(nullable = false, length = 100)
  private String contentType;

  @Column(name = "storage_key", nullable = false, columnDefinition = "TEXT")
  private String storageKey;

  @Column(nullable = false, length = 32)
  @Builder.Default
  private String accessLevel = "ORDER";

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
