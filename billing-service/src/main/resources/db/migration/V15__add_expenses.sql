CREATE TABLE IF NOT EXISTS expenses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hotel_id UUID NOT NULL,
    category VARCHAR(120) NOT NULL,
    amount NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    description VARCHAR(255),
    reference VARCHAR(100),
    cash_session_id UUID REFERENCES cash_sessions(id),
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_expenses_hotel_created ON expenses(hotel_id, created_at DESC);
