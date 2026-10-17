ALTER TABLE inventory_purchases
  ADD COLUMN receipt_request_digests JSONB NOT NULL DEFAULT '{}'::jsonb;
