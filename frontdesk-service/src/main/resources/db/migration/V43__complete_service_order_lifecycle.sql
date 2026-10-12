ALTER TABLE service_orders
  ADD COLUMN IF NOT EXISTS proposed_solution VARCHAR(4000),
  ADD COLUMN IF NOT EXISTS estimated_minutes INTEGER,
  ADD COLUMN IF NOT EXISTS labor_amount NUMERIC(12,2),
  ADD COLUMN IF NOT EXISTS parts_amount NUMERIC(12,2),
  ADD COLUMN IF NOT EXISTS tax_amount NUMERIC(12,2),
  ADD COLUMN IF NOT EXISTS quote_total NUMERIC(12,2),
  ADD COLUMN IF NOT EXISTS delivered_at TIMESTAMP,
  ADD COLUMN IF NOT EXISTS delivered_by UUID;

CREATE TABLE service_order_tests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  hotel_id UUID NOT NULL,
  service_order_id UUID NOT NULL REFERENCES service_orders(id),
  test_name VARCHAR(160) NOT NULL,
  passed BOOLEAN NOT NULL,
  notes VARCHAR(2000),
  technician_id UUID,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_service_order_tests_tenant_order ON service_order_tests(hotel_id, service_order_id, created_at);

CREATE TABLE service_order_status_history (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  hotel_id UUID NOT NULL,
  service_order_id UUID NOT NULL REFERENCES service_orders(id),
  from_status VARCHAR(32),
  to_status VARCHAR(32) NOT NULL,
  actor VARCHAR(100) NOT NULL,
  notes VARCHAR(1000),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_service_order_history_tenant_order ON service_order_status_history(hotel_id, service_order_id, created_at);
