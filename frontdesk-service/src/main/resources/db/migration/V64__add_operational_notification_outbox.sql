CREATE TABLE operational_notification_outbox (
  id UUID PRIMARY KEY,
  hotel_id UUID NOT NULL,
  event_key VARCHAR(180) NOT NULL,
  source_key VARCHAR(180) NOT NULL,
  event_type VARCHAR(120) NOT NULL,
  payload TEXT NOT NULL,
  status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
  attempts INTEGER NOT NULL DEFAULT 0,
  next_attempt_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  last_error TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  sent_at TIMESTAMP,
  CONSTRAINT uq_operational_notification_hotel_event UNIQUE (hotel_id, event_key)
);

CREATE INDEX idx_operational_notification_due
  ON operational_notification_outbox (next_attempt_at, created_at)
  WHERE status IN ('PENDING', 'FAILED');

CREATE INDEX idx_operational_notification_source
  ON operational_notification_outbox (hotel_id, event_type, source_key);

CREATE INDEX idx_service_orders_promised_at
  ON service_orders (promised_at, hotel_id)
  WHERE promised_at IS NOT NULL AND status NOT IN ('COMPLETED', 'CANCELLED');
