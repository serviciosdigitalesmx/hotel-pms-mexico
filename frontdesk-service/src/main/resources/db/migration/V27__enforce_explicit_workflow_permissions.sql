ALTER TABLE workflow_transitions
    ADD CONSTRAINT chk_workflow_transition_explicit_permission
    CHECK (length(trim(required_permission)) > 0 AND required_permission NOT LIKE 'ROLE_%');
