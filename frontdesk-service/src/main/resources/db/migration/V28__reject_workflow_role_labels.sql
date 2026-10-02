ALTER TABLE workflow_transitions
    ADD CONSTRAINT chk_workflow_transition_no_role_labels
    CHECK (upper(trim(required_permission)) NOT IN ('ADMIN', 'OWNER'));
