CREATE TABLE inventory_purchase_lines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    purchase_id UUID NOT NULL REFERENCES inventory_purchases(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    quantity_ordered BIGINT NOT NULL CHECK (quantity_ordered > 0),
    quantity_received BIGINT NOT NULL DEFAULT 0 CHECK (quantity_received >= 0),
    unit_cost NUMERIC(12, 2) NOT NULL CHECK (unit_cost >= 0)
);

CREATE INDEX idx_inventory_purchase_lines_purchase ON inventory_purchase_lines(purchase_id);
