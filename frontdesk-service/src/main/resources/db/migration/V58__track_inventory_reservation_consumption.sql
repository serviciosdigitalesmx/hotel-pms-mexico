ALTER TABLE inventory_reservations
    ADD COLUMN consumed_at TIMESTAMP;

UPDATE inventory_reservations reservation
SET status = 'CONSUMED', consumed_at = service_order.delivered_at
FROM service_orders service_order
WHERE reservation.service_order_id = service_order.id
  AND reservation.hotel_id = service_order.hotel_id
  AND reservation.status = 'RESERVED'
  AND service_order.status = 'COMPLETED'
  AND service_order.delivered_at IS NOT NULL;
