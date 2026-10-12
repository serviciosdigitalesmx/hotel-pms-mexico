CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    hotel_id UUID NOT NULL,
    actor_id UUID NULL,
    category VARCHAR(32) NOT NULL,
    event_type VARCHAR(120) NOT NULL,
    aggregate_id UUID NULL,
    correlation_id VARCHAR(120) NULL,
    details TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_audit_events_hotel_created ON audit_events(hotel_id, created_at DESC);
