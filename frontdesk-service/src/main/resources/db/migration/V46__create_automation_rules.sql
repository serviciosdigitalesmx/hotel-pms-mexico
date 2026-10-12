CREATE TABLE automation_rules (
    id UUID PRIMARY KEY,
    hotel_id UUID NOT NULL,
    domain VARCHAR(24) NOT NULL,
    event_type VARCHAR(120) NOT NULL,
    action_type VARCHAR(120) NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_automation_rules_hotel_enabled ON automation_rules(hotel_id, enabled);
