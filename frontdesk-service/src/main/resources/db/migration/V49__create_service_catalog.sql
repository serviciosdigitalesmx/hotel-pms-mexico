CREATE TABLE service_catalog (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hotel_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_service_catalog_tenant_name UNIQUE (hotel_id, name)
);

CREATE INDEX idx_service_catalog_tenant_active_name
    ON service_catalog (hotel_id, active, name);
