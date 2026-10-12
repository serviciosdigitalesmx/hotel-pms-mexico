ALTER TABLE menu_items ADD COLUMN inventory_product_id UUID NULL;
CREATE INDEX idx_menu_items_inventory_product ON menu_items(hotel_id, inventory_product_id);
