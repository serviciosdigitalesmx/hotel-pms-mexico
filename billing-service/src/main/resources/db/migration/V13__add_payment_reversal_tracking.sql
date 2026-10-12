ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS reversed_amount NUMERIC(12, 2) NOT NULL DEFAULT 0;

ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS reversal_reference VARCHAR(100);

ALTER TABLE payments
    ADD CONSTRAINT chk_payments_reversed_amount
    CHECK (reversed_amount >= 0 AND reversed_amount <= amount);
