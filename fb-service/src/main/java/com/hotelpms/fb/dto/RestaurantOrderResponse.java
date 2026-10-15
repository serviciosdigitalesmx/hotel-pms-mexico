package com.hotelpms.fb.dto;

import com.hotelpms.fb.domain.OrderStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for a restaurant order response.
 *
 * @param id                the ID of the order
 * @param stayId            the ID of the stay this order is associated with
 * @param branchId           optional inventory branch for the order
 * @param roomNumber        the room the order is charged to, denormalized at creation time
 * @param guestDisplayName  the primary guest's "Cognome Nome", denormalized at creation time
 * @param orderDate         the date and time the order was placed
 * @param totalAmount       the total amount of the order
 * @param paymentId         the payment ID
 * @param status            the status of the order
 * @param items             the items included in the order
 * @param createdAt         the creation timestamp
 * @param updatedAt         the last update timestamp
 */
@Builder
public record RestaurantOrderResponse(
                UUID id,
                UUID stayId,
                UUID branchId,
                String roomNumber,
                String guestDisplayName,
                LocalDateTime orderDate,
                BigDecimal totalAmount,
                UUID paymentId,
                OrderStatus status,
                List<OrderItemResponse> items,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {

        /** Backward-compatible constructor retained for existing response consumers. */
        public RestaurantOrderResponse(final UUID id, final UUID stayId, final String roomNumber,
                final String guestDisplayName, final LocalDateTime orderDate, final BigDecimal totalAmount,
                final UUID paymentId, final OrderStatus status, final List<OrderItemResponse> items,
                final LocalDateTime createdAt, final LocalDateTime updatedAt) {
                this(id, stayId, null, roomNumber, guestDisplayName, orderDate, totalAmount,
                        paymentId, status, items, createdAt, updatedAt);
        }

        /**
         * Compact constructor to ensure defensive copying of the items list.
         *
         * @param id                the order ID
         * @param stayId            the stay ID
         * @param roomNumber        the room number
         * @param guestDisplayName  the guest display name
         * @param orderDate         the order date
         * @param totalAmount       the total amount
         * @param status            the status
         * @param items             the items
         * @param createdAt         the creation time
         * @param updatedAt         the update time
         */
        public RestaurantOrderResponse {
                items = items == null ? null : List.copyOf(items);
        }

        /**
         * Returns an unmodifiable list of items.
         *
         * @return unmodifiable items list
         */
        @Override
        public List<OrderItemResponse> items() {
                return items == null ? null : List.copyOf(items);
        }
}
