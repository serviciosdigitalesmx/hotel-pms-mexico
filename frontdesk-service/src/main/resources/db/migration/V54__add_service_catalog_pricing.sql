ALTER TABLE service_catalog
    ADD COLUMN base_price NUMERIC(12, 2) NOT NULL DEFAULT 0,
    ADD COLUMN estimated_minutes INTEGER NOT NULL DEFAULT 60;

ALTER TABLE service_catalog
    ADD CONSTRAINT chk_service_catalog_estimated_minutes_positive
    CHECK (estimated_minutes > 0);
