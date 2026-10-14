-- Include the expense-write capability in tenant scope for old and new tenants.
INSERT INTO tenant_capability (tenant_id, capability_key, enabled, updated_at)
SELECT tenant_id, 'FINANCE_WRITE', TRUE, CURRENT_TIMESTAMP
FROM (
    SELECT id AS tenant_id FROM hotel_registry
    UNION
    SELECT hotel_id AS tenant_id FROM user_account WHERE hotel_id IS NOT NULL
) tenants
ON CONFLICT (tenant_id, capability_key) DO NOTHING;
