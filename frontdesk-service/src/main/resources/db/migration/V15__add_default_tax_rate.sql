-- Optional tenant-level tax percentage for service quotations. NULL preserves existing manual tax behavior.
ALTER TABLE hotel_settings
    ADD COLUMN IF NOT EXISTS default_tax_rate NUMERIC(5, 2);
