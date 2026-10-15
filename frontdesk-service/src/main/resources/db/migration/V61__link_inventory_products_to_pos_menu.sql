ALTER TABLE inventory_products ADD COLUMN IF NOT EXISTS pos_menu_item_id UUID;
CREATE INDEX IF NOT EXISTS idx_inventory_products_pos_menu_item
    ON inventory_products (hotel_id, pos_menu_item_id)
    WHERE pos_menu_item_id IS NOT NULL;
