package com.hotelpms.frontdesk.inventory.repository;

import com.hotelpms.frontdesk.inventory.domain.InventoryProduct;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Tenant-scoped product catalog access, including row-locked updates. */
public interface InventoryProductRepository extends JpaRepository<InventoryProduct, UUID> {
    /**
     * Lists products owned by a tenant.
     *
     * @param hotelId tenant identifier
     * @return tenant-owned products
     */
    List<InventoryProduct> findAllByHotelId(UUID hotelId);

    /**
     * Finds a product by its tenant-unique SKU.
     *
     * @param hotelId tenant identifier
     * @param sku product SKU
     * @return matching product, if present
     */
    Optional<InventoryProduct> findByHotelIdAndSku(UUID hotelId, String sku);

    /**
     * Checks whether another product links the specified POS menu item.
     *
     * @param hotelId tenant identifier
     * @param posMenuItemId menu item identifier
     * @param id product excluded from the check
     * @return whether another product has the menu link
     */
    boolean existsByHotelIdAndPosMenuItemIdAndIdNot(UUID hotelId, UUID posMenuItemId, UUID id);

    /**
     * Checks whether a tenant product already links the specified POS menu item.
     *
     * @param hotelId tenant identifier
     * @param posMenuItemId menu item identifier
     * @return whether a product has the menu link
     */
    boolean existsByHotelIdAndPosMenuItemId(UUID hotelId, UUID posMenuItemId);

    /**
     * Finds a product by identifier and tenant.
     *
     * @param id product identifier
     * @param hotelId tenant identifier
     * @return matching tenant-owned product, if present
     */
    Optional<InventoryProduct> findByIdAndHotelId(UUID id, UUID hotelId);

    /**
     * Locks a product row after verifying tenant ownership.
     *
     * @param id product identifier
     * @param hotelId tenant identifier
     * @return locked tenant-owned product, if present
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            "select product from InventoryProduct product where product.id = :id and"
                + " product.hotelId = :hotelId")
    Optional<InventoryProduct> findForUpdateByIdAndHotelId(
            @Param("id") UUID id, @Param("hotelId") UUID hotelId);
}
