package com.hotelpms.frontdesk.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** A supplier and contact record owned by a single Fixi tenant. */
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
    private static final int EMAIL_LENGTH = 254;
    private static final int PHONE_LENGTH = 40;
    private static final int WEBSITE_LENGTH = 500;

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

    @Column(name = "contact_name", length = NAME_LENGTH)
    private String contactName;

    @Column(length = EMAIL_LENGTH)
    private String email;

    @Column(length = PHONE_LENGTH)
    private String phone;

    @Column(name = "website", length = WEBSITE_LENGTH)
    private String website;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}
