package com.hotelpms.fb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request payload for creating or updating a menu item.
 *
 * @param name        display name (required)
 * @param price       unit price in EUR (required, must be positive)
 * @param category    display category (required, e.g. "Bar", "Cucina")
 * @param description optional free-text description
 * @param available   whether the item is visible in the order form
 * @param inventoryProductId optional inventory product linked to this menu item
 */
public record MenuItemRequest(
        @NotBlank String name,
        @NotNull @Positive BigDecimal price,
        @NotBlank String category,
        String description,
        boolean available,
        UUID inventoryProductId) {
    /** Preserves the request contract for menu items without an inventory link.
     *
     * @param name display name
     * @param price unit price
     * @param category display category
     * @param description optional description
     * @param available whether the item can be ordered
     */
    public MenuItemRequest(final String name, final BigDecimal price, final String category,
            final String description, final boolean available) {
        this(name, price, category, description, available, null);
    }
}
