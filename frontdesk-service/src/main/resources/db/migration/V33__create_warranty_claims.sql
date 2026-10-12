CREATE TABLE warranty_claims (id UUID PRIMARY KEY DEFAULT gen_random_uuid(), hotel_id UUID NOT NULL, service_order_id UUID NOT NULL, customer_id UUID NOT NULL, status VARCHAR(32) NOT NULL, expires_at DATE NOT NULL, description VARCHAR(2000) NOT NULL, created_at TIMESTAMP NOT NULL);
CREATE INDEX idx_warranty_claims_hotel_order ON warranty_claims(hotel_id,service_order_id);
