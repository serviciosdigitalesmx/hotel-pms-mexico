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

/** InventoryProductRepository domain type. */
public interface InventoryProductRepository extends JpaRepository<InventoryProduct, UUID> {
    /**
     * Executes the operation.
     *
     * @param hotelId parameter.
     * @return operation result.
     */
    List<InventoryProduct> findAllByHotelId(UUID hotelId);

    /**
     * Executes the operation.
     *
     * @param hotelId parameter.
     * @param sku parameter.
     * @return operation result.
     */
    Optional<InventoryProduct> findByHotelIdAndSku(UUID hotelId, String sku);

    boolean existsByHotelIdAndPosMenuItemIdAndIdNot(UUID hotelId, UUID posMenuItemId, UUID id);

    boolean existsByHotelIdAndPosMenuItemId(UUID hotelId, UUID posMenuItemId);

    Optional<InventoryProduct> findByIdAndHotelId(UUID id, UUID hotelId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            "select product from InventoryProduct product where product.id = :id and"
                + " product.hotelId = :hotelId")
    Optional<InventoryProduct> findForUpdateByIdAndHotelId(
            @Param("id") UUID id, @Param("hotelId") UUID hotelId);
}
