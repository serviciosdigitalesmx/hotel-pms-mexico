CREATE TABLE service_requests (id UUID PRIMARY KEY DEFAULT gen_random_uuid(), hotel_id UUID NOT NULL, requester_name VARCHAR(150) NOT NULL, requester_contact VARCHAR(200) NOT NULL, description VARCHAR(1000) NOT NULL, status VARCHAR(20) NOT NULL, created_at TIMESTAMP NOT NULL);
CREATE INDEX idx_service_requests_hotel_status ON service_requests(hotel_id,status);
