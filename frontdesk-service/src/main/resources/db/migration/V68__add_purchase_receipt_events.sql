ALTER TABLE inventory_purchases
  ADD COLUMN receipt_events JSONB NOT NULL DEFAULT '[]'::jsonb;

UPDATE inventory_purchases purchase
SET receipt_events = COALESCE((
  SELECT jsonb_agg(jsonb_build_object(
    'kind', 'LEGACY_SNAPSHOT',
    'productId', line.product_id::text,
    'quantity', line.quantity_received::text,
    'unitCost', line.unit_cost::text,
    'receivedAt', COALESCE(purchase.last_received_at, purchase.created_at)::text
  ))
  FROM inventory_purchase_lines line
  WHERE line.purchase_id = purchase.id AND line.quantity_received > 0
), '[]'::jsonb);
