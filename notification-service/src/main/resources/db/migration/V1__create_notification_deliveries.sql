CREATE TABLE notification_deliveries (
    id UUID PRIMARY KEY,
    hotel_id UUID NOT NULL,
    event_key VARCHAR(180) NOT NULL UNIQUE,
    event_type VARCHAR(120) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(24) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    last_error TEXT NULL,
    created_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP NULL
);
CREATE INDEX idx_notification_deliveries_hotel_created ON notification_deliveries(hotel_id, created_at DESC);
