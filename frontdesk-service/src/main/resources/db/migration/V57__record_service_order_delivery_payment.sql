ALTER TABLE service_orders
    ADD COLUMN payment_method VARCHAR(24),
    ADD COLUMN payment_status VARCHAR(16);
