ALTER TABLE notification_deliveries
    ADD COLUMN next_attempt_at TIMESTAMP NULL;

CREATE INDEX idx_notification_deliveries_retry
    ON notification_deliveries(status, next_attempt_at, created_at)
    WHERE status IN ('PENDING', 'FAILED');
