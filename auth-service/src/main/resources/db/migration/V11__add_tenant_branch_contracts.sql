CREATE TABLE tenant_branch (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hotel_id UUID NOT NULL REFERENCES hotel_registry(id),
    name VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_tenant_branch_hotel_name UNIQUE (hotel_id, name)
);

CREATE INDEX idx_tenant_branch_hotel_active
    ON tenant_branch (hotel_id) WHERE active = TRUE;

CREATE TABLE user_branch_membership (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES user_account(id),
    branch_id UUID NOT NULL REFERENCES tenant_branch(id),
    hotel_id UUID NOT NULL REFERENCES hotel_registry(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_branch_membership UNIQUE (user_id, branch_id)
);

CREATE INDEX idx_user_branch_membership_user_hotel
    ON user_branch_membership (user_id, hotel_id);
