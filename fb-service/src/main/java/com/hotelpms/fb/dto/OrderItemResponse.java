package com.hotelpms.fb.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for an order item response.
 *
 * @param id        the ID of the order item
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
    /** Backward-compatible constructor retained for existing response consumers. */
    public OrderItemResponse(final UUID id, final String itemName, final Integer quantity,
            final BigDecimal unitPrice) {
        this(id, null, null, itemName, quantity, unitPrice);
    }

    public OrderItemResponse(final UUID id, final UUID menuItemId, final String itemName,
            final Integer quantity, final BigDecimal unitPrice) {
        this(id, menuItemId, null, itemName, quantity, unitPrice);
    }
}
