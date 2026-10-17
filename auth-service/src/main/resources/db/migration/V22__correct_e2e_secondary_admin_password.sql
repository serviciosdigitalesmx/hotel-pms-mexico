-- Keep the secondary-tenant fixture aligned with the documented E2E password.
-- The original V8 seed hash predates the corrected bcrypt fixture used for `admin`.
UPDATE user_account
SET password_hash = '{bcrypt}$2a$10$8aXe/PIDoC/tOecWVAMxsu57InT1n4F4U2ObRGB4W8DhGowDrbMi'
WHERE username = 'e2e-live-other-hotel-admin';
