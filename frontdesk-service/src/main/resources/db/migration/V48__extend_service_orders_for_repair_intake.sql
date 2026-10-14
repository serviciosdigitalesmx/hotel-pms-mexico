ALTER TABLE service_orders
    ADD COLUMN priority VARCHAR(16) NOT NULL DEFAULT 'NORMAL',
    ADD COLUMN service_name VARCHAR(200),
    ADD COLUMN deposit_amount NUMERIC(12, 2) NOT NULL DEFAULT 0;

ALTER TABLE inventory_products
    ADD COLUMN supplier_id UUID;
