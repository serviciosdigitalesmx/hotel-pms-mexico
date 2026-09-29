CREATE TABLE devices (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(), hotel_id UUID NOT NULL, guest_id UUID NOT NULL,
 category VARCHAR(80) NOT NULL, manufacturer VARCHAR(150), model VARCHAR(150), serial_number VARCHAR(150),
 imei VARCHAR(150), custom_fields VARCHAR(2000), active BOOLEAN NOT NULL DEFAULT TRUE,
 created_at TIMESTAMP NOT NULL, updated_at TIMESTAMP NOT NULL,
 CONSTRAINT fk_devices_customer FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE RESTRICT
);
CREATE INDEX idx_devices_hotel_customer ON devices(hotel_id, guest_id) WHERE active = TRUE;
CREATE UNIQUE INDEX uq_devices_customer_serial ON devices(hotel_id, guest_id, serial_number) WHERE active = TRUE AND serial_number IS NOT NULL;
