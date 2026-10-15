ALTER TABLE ledger_entries
    DROP CONSTRAINT IF EXISTS ledger_entries_reference_key_key;

ALTER TABLE deposits
    DROP CONSTRAINT IF EXISTS deposits_reference_key_key;

CREATE UNIQUE INDEX IF NOT EXISTS uq_ledger_entries_hotel_reference
    ON ledger_entries (hotel_id, reference_key);

CREATE UNIQUE INDEX IF NOT EXISTS uq_deposits_hotel_reference
    ON deposits (hotel_id, reference_key);
