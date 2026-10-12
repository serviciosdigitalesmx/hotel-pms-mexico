CREATE TABLE portal_otp_challenges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token_id UUID NOT NULL,
    code_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    consumed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_portal_otp_token_created ON portal_otp_challenges(token_id, consumed, created_at DESC);
