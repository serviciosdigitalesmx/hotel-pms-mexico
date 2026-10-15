CREATE TABLE pos_inventory_consumptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hotel_id UUID NOT NULL,
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    quantity BIGINT NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_pos_inventory_consumption UNIQUE (hotel_id, order_id, product_id)
);

CREATE INDEX idx_pos_inventory_consumptions_branch
    ON pos_inventory_consumptions (hotel_id, branch_id, created_at DESC);
