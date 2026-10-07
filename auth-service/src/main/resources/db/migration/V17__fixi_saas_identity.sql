ALTER TABLE hotel_registry
    ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN IF NOT EXISTS onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS contact_phone VARCHAR(40),
    ADD COLUMN IF NOT EXISTS business_hours VARCHAR(500);

ALTER TABLE user_account
    ADD COLUMN IF NOT EXISTS full_name VARCHAR(160),
    ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT TRUE;

-- Existing tenants have already been provisioned through the legacy platform flow;
-- only newly self-registered Fixi tenants enter the explicit onboarding state.
UPDATE hotel_registry SET onboarding_completed = TRUE WHERE onboarding_completed = FALSE;

CREATE TABLE account_token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES user_account(id),
    purpose VARCHAR(32) NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    consumed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_account_token_user_purpose ON account_token(user_id, purpose, created_at DESC);

CREATE TABLE tenant_invitation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hotel_id UUID NOT NULL REFERENCES hotel_registry(id),
    email VARCHAR(100) NOT NULL,
    role VARCHAR(32) NOT NULL,
    branch_id UUID REFERENCES tenant_branch(id),
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    accepted_at TIMESTAMP,
    created_by UUID NOT NULL REFERENCES user_account(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_tenant_invitation_hotel_email ON tenant_invitation(hotel_id, email);
