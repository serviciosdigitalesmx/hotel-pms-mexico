ALTER TABLE service_orders ADD COLUMN authorized_by UUID;
ALTER TABLE service_orders ADD COLUMN authorization_evidence VARCHAR(2000);
ALTER TABLE service_orders ADD COLUMN authorized_at TIMESTAMP;
