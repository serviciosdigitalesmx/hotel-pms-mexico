CREATE TABLE IF NOT EXISTS cash_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hotel_id UUID NOT NULL,
    opened_by UUID NOT NULL,
    opened_at TIMESTAMP NOT NULL,
    closed_at TIMESTAMP,
    opening_balance NUMERIC(12,2) NOT NULL CHECK (opening_balance >= 0),
    closing_balance NUMERIC(12,2) CHECK (closing_balance >= 0),
    status VARCHAR(16) NOT NULL CHECK (status IN ('OPEN','CLOSED'))
);
CREATE TABLE IF NOT EXISTS cash_movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES cash_sessions(id),
    type VARCHAR(16) NOT NULL CHECK (type IN ('CASH_IN','CASH_OUT')),
    amount NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
    reference VARCHAR(100),
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_cash_open_session ON cash_sessions(hotel_id) WHERE status = 'OPEN';
