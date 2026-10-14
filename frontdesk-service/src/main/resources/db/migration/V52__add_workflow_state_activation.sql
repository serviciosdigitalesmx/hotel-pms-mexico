ALTER TABLE workflow_states
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX idx_workflow_states_workflow_active
    ON workflow_states (workflow_id, active);
