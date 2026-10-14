ALTER TABLE expenses
    ADD COLUMN payment_method VARCHAR(20),
    ADD COLUMN paid_at TIMESTAMP;

UPDATE expenses
SET payment_method = CASE WHEN cash_session_id IS NOT NULL THEN 'CASH' ELSE 'OTHER' END,
    paid_at = created_at
WHERE paid = TRUE;
