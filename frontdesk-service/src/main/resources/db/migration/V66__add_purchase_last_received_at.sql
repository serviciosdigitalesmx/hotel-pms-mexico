ALTER TABLE inventory_purchases
  ADD COLUMN last_received_at TIMESTAMP;

UPDATE inventory_purchases
SET last_received_at = created_at
WHERE status IN ('RECEIVED', 'PARTIALLY_RECEIVED');
