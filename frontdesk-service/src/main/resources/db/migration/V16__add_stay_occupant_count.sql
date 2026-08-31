ALTER TABLE stays
    ADD COLUMN IF NOT EXISTS occupant_count INTEGER NOT NULL DEFAULT 1;

ALTER TABLE stays
    DROP CONSTRAINT IF EXISTS chk_stays_occupant_count;

ALTER TABLE stays
    ADD CONSTRAINT chk_stays_occupant_count CHECK (occupant_count >= 1);

COMMENT ON COLUMN stays.occupant_count IS
    'Total room occupants; detailed personal data remains optional for companions.';
