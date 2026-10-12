CREATE TABLE service_orders (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(), hotel_id UUID NOT NULL, branch_id UUID, customer_id UUID NOT NULL,
 device_id UUID, status VARCHAR(32) NOT NULL, summary VARCHAR(500) NOT NULL, diagnosis VARCHAR(4000), work_log VARCHAR(8000),
 assigned_user_id UUID, created_at TIMESTAMP NOT NULL, updated_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_service_orders_hotel_customer ON service_orders(hotel_id, customer_id);
