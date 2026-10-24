-- Platform operators are global identities and must not be assigned to a customer tenant.
ALTER TABLE user_account ALTER COLUMN hotel_id DROP NOT NULL;
