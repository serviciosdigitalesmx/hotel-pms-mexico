package com.hotelpms.fb.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for an order item response.
 *
 * @param id        the ID of the order item
 * @param menuItemId menu item identifier, if the order line came from the menu
 * @param inventoryProductId linked inventory product identifier, if any
 * @param itemName  the name of the item
 * @param quantity  the quantity of the item
 * @param unitPrice the unit price of the item
 */
@Builder
public record OrderItemResponse(
        UUID id,
        UUID menuItemId,
        UUID inventoryProductId,
        String itemName,
        Integer quantity,
        BigDecimal unitPrice) {
    /**
     * Backward-compatible constructor retained for existing response consumers.
     *
     * @param id order item identifier
     * @param itemName item name
     * @param quantity ordered quantity
     * @param unitPrice unit price
     */
    public OrderItemResponse(final UUID id, final String itemName, final Integer quantity,
            final BigDecimal unitPrice) {
        this(id, null, null, itemName, quantity, unitPrice);
    }

    /**
     * Constructs a response for consumers that know the menu item but not its inventory link.
     *
     * @param id order item identifier
     * @param menuItemId menu item identifier
     * @param itemName item name
     * @param quantity ordered quantity
     * @param unitPrice unit price
     */
    public OrderItemResponse(final UUID id, final UUID menuItemId, final String itemName,
            final Integer quantity, final BigDecimal unitPrice) {
        this(id, menuItemId, null, itemName, quantity, unitPrice);
    }
}
