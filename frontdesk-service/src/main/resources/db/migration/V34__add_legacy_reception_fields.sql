ALTER TABLE service_orders ADD COLUMN IF NOT EXISTS reported_failure VARCHAR(500);
ALTER TABLE service_orders ADD COLUMN IF NOT EXISTS intake_checklist VARCHAR(4000);
ALTER TABLE service_orders ADD COLUMN IF NOT EXISTS promised_at TIMESTAMP;
ALTER TABLE service_orders ADD COLUMN IF NOT EXISTS intake_evidence VARCHAR(4000);

COMMENT ON COLUMN service_orders.reported_failure IS 'Failure reported by customer at intake';
COMMENT ON COLUMN service_orders.intake_checklist IS 'Reception checklist captured before diagnosis';
COMMENT ON COLUMN service_orders.promised_at IS 'Promised delivery date/time';
COMMENT ON COLUMN service_orders.intake_evidence IS 'Initial photos or evidence references';
