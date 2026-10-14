ALTER TABLE inventory_suppliers
    ADD COLUMN contact_name VARCHAR(200),
    ADD COLUMN email VARCHAR(254),
    ADD COLUMN phone VARCHAR(40),
    ADD COLUMN website VARCHAR(500),
    ADD COLUMN description VARCHAR(1000);
