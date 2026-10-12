CREATE TABLE public_portal_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hotel_id UUID NOT NULL,
    resource_type VARCHAR(40) NOT NULL,
    resource_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_public_portal_tokens_resource ON public_portal_tokens(hotel_id, resource_type, resource_id);
CREATE INDEX idx_public_portal_tokens_expiry ON public_portal_tokens(expires_at, revoked);
