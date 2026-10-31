-- Keep the persisted role constraint aligned with the Role enum used by invitations.
-- KITCHEN and HOUSEKEEPER remain accepted for existing legacy accounts, but Fixi's
-- employee-role policy controls which roles can be assigned to new workshop users.
ALTER TABLE user_account
    DROP CONSTRAINT IF EXISTS chk_user_account_role;

ALTER TABLE user_account
    ADD CONSTRAINT chk_user_account_role
        CHECK (role IN (
            'ADMIN',
            'OWNER',
            'MANAGER',
            'TECHNICIAN',
            'RECEPTIONIST',
            'KITCHEN',
            'HOUSEKEEPER',
            'GUEST'
        ));
