ALTER TABLE user_account
    DROP CONSTRAINT IF EXISTS chk_user_account_role;

ALTER TABLE user_account
    ADD CONSTRAINT chk_user_account_role
    CHECK (role IN (
        'ADMIN',
        'OWNER',
        'RECEPTIONIST',
        'HOUSEKEEPER',
        'KITCHEN',
        'GUEST'
    ));

COMMENT ON COLUMN user_account.role
IS 'Authorization level: ADMIN | OWNER | RECEPTIONIST | HOUSEKEEPER | KITCHEN | GUEST.';
