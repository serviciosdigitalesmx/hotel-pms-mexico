ALTER TABLE order_items
    ADD COLUMN IF NOT EXISTS menu_item_id UUID;

ALTER TABLE order_items
    ADD COLUMN IF NOT EXISTS inventory_product_id UUID;

ALTER TABLE restaurant_orders
    ADD COLUMN IF NOT EXISTS branch_id UUID;

CREATE INDEX IF NOT EXISTS idx_order_items_menu_item_id
    ON order_items (menu_item_id)
    WHERE menu_item_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_order_items_inventory_product_id
    ON order_items (inventory_product_id)
    WHERE inventory_product_id IS NOT NULL;
