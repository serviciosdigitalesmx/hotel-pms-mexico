ALTER TABLE service_orders
  ADD COLUMN payment_events JSONB NOT NULL DEFAULT '[]'::jsonb;
