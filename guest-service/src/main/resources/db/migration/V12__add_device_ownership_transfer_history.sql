CREATE TABLE device_ownership_transfers (
    id UUID PRIMARY KEY,
    hotel_id UUID NOT NULL,
    device_id UUID NOT NULL,
    from_guest_id UUID,
    to_guest_id UUID NOT NULL,
    actor_id UUID,
    evidence VARCHAR(2000),
    transferred_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_device_transfer_tenant_device
    ON device_ownership_transfers (hotel_id, device_id, transferred_at DESC);
