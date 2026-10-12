CREATE TABLE service_order_time_entries (id UUID PRIMARY KEY DEFAULT gen_random_uuid(), hotel_id UUID NOT NULL, service_order_id UUID NOT NULL, user_id UUID NOT NULL, minutes INTEGER NOT NULL, note VARCHAR(1000), created_at TIMESTAMP NOT NULL);
CREATE INDEX idx_time_entries_hotel_order ON service_order_time_entries(hotel_id,service_order_id);
