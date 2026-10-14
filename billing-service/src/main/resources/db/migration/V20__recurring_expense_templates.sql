CREATE TABLE recurring_expense_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hotel_id UUID NOT NULL,
    category VARCHAR(120) NOT NULL,
    amount NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    description VARCHAR(255),
    reference VARCHAR(100),
    subcategory VARCHAR(120),
    frequency VARCHAR(16) NOT NULL CHECK (frequency IN ('WEEKLY', 'MONTHLY', 'YEARLY')),
    next_due_at DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_recurring_expense_templates_hotel_due
    ON recurring_expense_templates(hotel_id, active, next_due_at);
