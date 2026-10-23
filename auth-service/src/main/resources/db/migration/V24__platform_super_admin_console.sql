CREATE TABLE platform_admin_grant (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES user_account(id),
    platform_role VARCHAR(32) NOT NULL CHECK (platform_role = 'SUPER_ADMIN'),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    granted_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    granted_by VARCHAR(80) NOT NULL
);

CREATE TABLE platform_audit_event (
    id UUID PRIMARY KEY,
    actor_username VARCHAR(50) NOT NULL,
    action VARCHAR(80) NOT NULL,
    target_tenant_id UUID REFERENCES hotel_registry(id),
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metadata_json TEXT NOT NULL DEFAULT '{}'
);
CREATE INDEX idx_platform_audit_time ON platform_audit_event (occurred_at DESC);
CREATE INDEX idx_platform_audit_tenant_time ON platform_audit_event (target_tenant_id, occurred_at DESC);

CREATE TABLE platform_tenant_subscription (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES hotel_registry(id),
    plan_key VARCHAR(80) NOT NULL,
    status VARCHAR(24) NOT NULL CHECK (status IN ('ACTIVE', 'ENDED')),
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ,
    created_by VARCHAR(50) NOT NULL,
    CHECK ((status = 'ACTIVE' AND ends_at IS NULL) OR status = 'ENDED')
);
CREATE INDEX idx_platform_subscription_tenant ON platform_tenant_subscription (tenant_id, starts_at DESC);
CREATE UNIQUE INDEX uq_platform_subscription_active_tenant
    ON platform_tenant_subscription (tenant_id) WHERE status = 'ACTIVE';

INSERT INTO platform_tenant_subscription (
    id, tenant_id, plan_key, status, starts_at, ends_at, created_by
)
SELECT gen_random_uuid(), tenant_id, plan_key,
       CASE WHEN active THEN 'ACTIVE' ELSE 'ENDED' END,
       created_at AT TIME ZONE 'UTC',
       CASE WHEN active THEN NULL ELSE updated_at AT TIME ZONE 'UTC' END,
       'SYSTEM_MIGRATION'
FROM tenant_plan;
