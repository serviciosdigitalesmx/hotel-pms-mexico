CREATE TABLE user_capability_grant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES hotel_registry(id),
    user_id UUID NOT NULL REFERENCES user_account(id),
    branch_id UUID REFERENCES tenant_branch(id),
    capability_key VARCHAR(120) NOT NULL,
    enabled BOOLEAN NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_capability_grant_scope
        UNIQUE (tenant_id, user_id, branch_id, capability_key)
);

CREATE INDEX idx_user_capability_grant_lookup
    ON user_capability_grant (tenant_id, user_id, capability_key);
