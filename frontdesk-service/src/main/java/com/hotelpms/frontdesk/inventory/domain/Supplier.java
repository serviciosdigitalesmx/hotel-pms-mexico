package com.hotelpms.frontdesk.inventory.domain;

import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Type domain type. */
@Entity
@Table(
    name = "inventory_suppliers",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_supplier_hotel_tax",
            columnNames = {"hotel_id", "tax_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {
  private static final int NAME_LENGTH = 200;
  private static final int TAX_ID_LENGTH = 20;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "hotel_id", nullable = false)
  private UUID hotelId;

  @Column(nullable = false, length = NAME_LENGTH)
  private String name;

  @Column(name = "tax_id", length = TAX_ID_LENGTH)
  private String taxId;

  @Column(length = NAME_LENGTH)
  private String contact;
}
