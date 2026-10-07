-- The runtime password encoder uses Argon2 for seeded accounts.
-- Keep the legacy platform-admin fixture aligned with the E2E password contract.
UPDATE user_account
SET password_hash = '{argon2}$argon2id$v=19$m=19456,t=2,p=1$J/OSdC/4uBf6XPvfaznuWA$tODScmXWkkAnf+k3VangES9gHfC+g08fswgR5gZ0KPU'
WHERE username = 'admin';
