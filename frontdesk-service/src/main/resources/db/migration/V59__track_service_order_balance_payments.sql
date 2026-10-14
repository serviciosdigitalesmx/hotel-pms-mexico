ALTER TABLE service_orders
  ADD COLUMN IF NOT EXISTS paid_amount NUMERIC(12,2);

UPDATE service_orders
SET paid_amount = CASE
  WHEN payment_status = 'PAID' THEN GREATEST(COALESCE(quote_total, 0), COALESCE(deposit_amount, 0))
  ELSE COALESCE(deposit_amount, 0)
END
WHERE paid_amount IS NULL;

ALTER TABLE service_orders
  ALTER COLUMN paid_amount SET DEFAULT 0,
  ALTER COLUMN paid_amount SET NOT NULL;
