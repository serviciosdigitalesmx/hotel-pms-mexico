package com.hotelpms.frontdesk.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** Type domain type. */
@Entity
@Table(
        name = "inventory_balances",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uq_inventory_balance_branch_product",
                        columnNames = {"branch_id", "product_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryBalance {
    private static final long DEFAULT_QUANTITY = 0L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "hotel_id", nullable = false)
    private UUID hotelId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false)
    @Builder.Default
    private long quantity = DEFAULT_QUANTITY;

    @Version private long version;
}
