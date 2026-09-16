CREATE TABLE hotel_registry (
    id UUID PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    slug VARCHAR(80) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE hotel_registry IS 'Platform-managed tenants; a hotel owner account is created in the same auth transaction.';
