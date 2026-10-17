-- The historical BCrypt fixture is malformed and cannot authenticate.
-- Reuse the known-valid password fixture from V20 for the isolated E2E admin.
UPDATE user_account
SET password_hash = '{argon2}$argon2id$v=19$m=19456,t=2,p=1$J/OSdC/4uBf6XPvfaznuWA$tODScmXWkkAnf+k3VangES9gHfC+g08fswgR5gZ0KPU'
WHERE username = 'e2e-live-other-hotel-admin';
