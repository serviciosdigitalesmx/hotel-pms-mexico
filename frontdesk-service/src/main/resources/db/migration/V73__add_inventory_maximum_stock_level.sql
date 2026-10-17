ALTER TABLE inventory_products
    ADD COLUMN maximum_stock_level BIGINT NOT NULL DEFAULT 0;

ALTER TABLE inventory_products
    ADD CONSTRAINT chk_inventory_products_maximum_stock_level
        CHECK (maximum_stock_level >= 0 AND (maximum_stock_level = 0 OR maximum_stock_level >= reorder_level));
