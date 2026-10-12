CREATE TABLE deposits (
    id UUID PRIMARY KEY,
    hotel_id UUID NOT NULL,
    invoice_id UUID NOT NULL,
    amount NUMERIC(19,4) NOT NULL,
    status VARCHAR(24) NOT NULL,
    reference_key VARCHAR(180) NOT NULL UNIQUE,
    received_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_deposits_hotel_invoice ON deposits(hotel_id, invoice_id, received_at);
