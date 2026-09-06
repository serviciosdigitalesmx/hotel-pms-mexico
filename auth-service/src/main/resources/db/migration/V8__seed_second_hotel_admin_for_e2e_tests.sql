-- ============================================================
-- Flyway migration: V8__seed_second_hotel_admin_for_e2e_tests.sql
-- Service       : auth-service
-- Purpose       : Second-tenant ADMIN fixture for the live E2E suite.
--
-- This was originally named V7, but V7 is already used by the housekeeper
-- role migration. Keeping unique Flyway versions is required for a clean
-- database to start; no seed values or runtime contracts are changed.
-- ============================================================

INSERT INTO user_account (
    id,
    username,
    password_hash,
    email,
    role,
    hotel_id,
    active,
    must_change_password,
    created_at,
    updated_at
)
VALUES (
    'ffffffff-ffff-ffff-ffff-ffffffffffff',
    'e2e-live-other-hotel-admin',
    '{bcrypt}$2a$10$8aXe/PIDoC/tOecWVAMxsu57InT1n4F4Uq2ObRGB4W8DhGowDrbMi',
    'e2e-live-other-hotel-admin@hotel-pms.local',
    'ADMIN',
    '99999999-9999-9999-9999-999999999999',
    TRUE,
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO UPDATE SET password_hash = EXCLUDED.password_hash;
