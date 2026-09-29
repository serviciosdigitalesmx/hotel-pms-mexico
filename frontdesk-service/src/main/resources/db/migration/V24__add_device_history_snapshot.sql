ALTER TABLE stays ADD COLUMN IF NOT EXISTS device_id UUID;
ALTER TABLE stays ADD COLUMN IF NOT EXISTS device_category VARCHAR(80);
ALTER TABLE stays ADD COLUMN IF NOT EXISTS device_manufacturer VARCHAR(150);
ALTER TABLE stays ADD COLUMN IF NOT EXISTS device_model VARCHAR(150);
ALTER TABLE stays ADD COLUMN IF NOT EXISTS device_serial_number VARCHAR(150);
ALTER TABLE stays ADD COLUMN IF NOT EXISTS device_imei VARCHAR(150);
COMMENT ON COLUMN stays.device_id IS 'Logical device reference; snapshot columns preserve historical identity.';
