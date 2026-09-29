CREATE TABLE tenant_plan (
    tenant_id UUID PRIMARY KEY REFERENCES hotel_registry(id),
    plan_key VARCHAR(80) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tenant_capability (
    tenant_id UUID NOT NULL REFERENCES hotel_registry(id),
    capability_key VARCHAR(120) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    limit_value BIGINT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, capability_key)
);

CREATE TABLE capability_audit_event (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES hotel_registry(id),
    actor_username VARCHAR(50) NOT NULL,
    capability_key VARCHAR(120) NOT NULL,
    enabled BOOLEAN NOT NULL,
    event_type VARCHAR(40) NOT NULL,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_capability_audit_tenant_time ON capability_audit_event (tenant_id, occurred_at);
