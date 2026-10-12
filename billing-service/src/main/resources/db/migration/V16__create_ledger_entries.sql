CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY,
    hotel_id UUID NOT NULL,
    aggregate_id UUID NULL,
    entry_type VARCHAR(32) NOT NULL,
    amount NUMERIC(19,4) NOT NULL,
    reference_key VARCHAR(180) NOT NULL UNIQUE,
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_ledger_entries_hotel_created ON ledger_entries(hotel_id, created_at);
