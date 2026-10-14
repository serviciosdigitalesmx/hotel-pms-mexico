ALTER TABLE expenses
    ADD COLUMN recurring_template_id UUID,
    ADD COLUMN recurring_period_due_at DATE;

CREATE UNIQUE INDEX uq_expenses_recurring_period
    ON expenses(hotel_id, recurring_template_id, recurring_period_due_at)
    WHERE recurring_template_id IS NOT NULL;
