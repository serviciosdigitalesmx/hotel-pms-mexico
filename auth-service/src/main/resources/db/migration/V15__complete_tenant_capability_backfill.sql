-- Legacy fixture users may reference tenants created before hotel_registry.
-- Seed the same default capability catalog for every known tenant identity.
INSERT INTO hotel_registry (id, name, slug)
SELECT DISTINCT ua.hotel_id,
       'Legacy tenant ' || ua.hotel_id,
       'legacy-' || replace(ua.hotel_id::text, '-', '')
FROM user_account ua
WHERE ua.hotel_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM hotel_registry h WHERE h.id = ua.hotel_id)
ON CONFLICT (id) DO NOTHING;

INSERT INTO tenant_capability (tenant_id, capability_key, enabled, updated_at)
SELECT tenant_id, capability.capability_key, TRUE, CURRENT_TIMESTAMP
FROM (
    SELECT id AS tenant_id FROM hotel_registry
    UNION
    SELECT hotel_id AS tenant_id FROM user_account WHERE hotel_id IS NOT NULL
) tenants
CROSS JOIN (
    VALUES
      ('BRANCHES_READ'), ('BRANCHES_MANAGE'), ('CAPABILITIES_READ'), ('CAPABILITIES_MANAGE'),
      ('CUSTOMERS_READ'), ('CUSTOMERS_WRITE'), ('ORDERS_READ'), ('ORDERS_CREATE'),
      ('ORDERS_UPDATE'), ('ORDERS_CANCEL'), ('ORDERS_STATUS_CHANGE'), ('QUOTES_CREATE'),
      ('QUOTES_MODIFY'), ('QUOTES_AUTHORIZE_RECORD'), ('INVENTORY_READ'), ('INVENTORY_ADJUST'),
      ('INVENTORY_PURCHASE'), ('CASH_OPEN'), ('CASH_CLOSE'), ('CASH_MOVE'), ('PAYMENTS_RECEIVE'),
      ('PAYMENTS_REFUND'), ('FINANCE_READ'), ('PERMISSIONS_MANAGE'), ('WORKFLOWS_MANAGE'),
      ('SETTINGS_MANAGE'), ('SETTINGS_READ'), ('RESERVATIONS_READ'), ('RESERVATIONS_WRITE'),
      ('ROOMS_READ'), ('ROOMS_WRITE'), ('ROOM_TYPES_WRITE'), ('RATE_CALENDAR_WRITE'),
      ('GUESTS_READ'), ('GUESTS_WRITE'), ('STAYS_READ'), ('STAYS_WRITE'), ('BILLING_READ'),
      ('BILLING_WRITE'), ('REPORTS_READ'), ('USERS_MANAGE'), ('HOTEL_SETTINGS_READ'),
      ('HOTEL_SETTINGS_WRITE'), ('FB_ORDERS_READ'), ('FB_ORDERS_WRITE'), ('HOUSEKEEPING_READ'),
      ('HOUSEKEEPING_WRITE'), ('PLATFORM_HOTELS_MANAGE')
) AS capability(capability_key)
ON CONFLICT (tenant_id, capability_key) DO NOTHING;

-- Keep the documented E2E/default admin password usable with DelegatingPasswordEncoder.
UPDATE user_account
SET password_hash = '{bcrypt}$2a$10$8aXe/PIDoC/tOecWVAMxsu57InT1n4F4U2ObRGB4W8DhGowDrBmi'
WHERE username = 'admin';
