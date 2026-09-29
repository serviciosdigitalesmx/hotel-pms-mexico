CREATE TABLE workflow_definitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hotel_id UUID NOT NULL,
    vertical_key VARCHAR(80) NOT NULL,
    name VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_workflow_definition_tenant_key UNIQUE (hotel_id, vertical_key)
);
CREATE INDEX idx_workflow_definitions_hotel ON workflow_definitions (hotel_id, active);
ALTER TABLE workflow_definitions ADD CONSTRAINT uq_workflow_tenant_id UNIQUE (hotel_id, id);

CREATE TABLE workflow_states (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workflow_id UUID NOT NULL REFERENCES workflow_definitions(id) ON DELETE CASCADE,
    state_key VARCHAR(80) NOT NULL,
    label VARCHAR(120) NOT NULL,
    semantic_phase VARCHAR(40) NOT NULL,
    initial_state BOOLEAN NOT NULL DEFAULT FALSE,
    terminal_state BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_workflow_state_key UNIQUE (workflow_id, state_key),
    CONSTRAINT chk_workflow_semantic_phase CHECK (semantic_phase IN ('INTAKE','DIAGNOSIS','QUOTE','AUTHORIZATION','WORK','READY','DELIVERED','WARRANTY','CLOSED','CANCELLED'))
);

CREATE TABLE workflow_transitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workflow_id UUID NOT NULL REFERENCES workflow_definitions(id) ON DELETE CASCADE,
    from_state_id UUID NOT NULL REFERENCES workflow_states(id) ON DELETE CASCADE,
    to_state_id UUID NOT NULL REFERENCES workflow_states(id) ON DELETE CASCADE,
    transition_key VARCHAR(80) NOT NULL,
    required_permission VARCHAR(120) NOT NULL,
    CONSTRAINT uq_workflow_transition UNIQUE (workflow_id, from_state_id, transition_key)
);

CREATE TABLE workflow_transition_audit (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hotel_id UUID NOT NULL,
    workflow_id UUID NOT NULL REFERENCES workflow_definitions(id),
    aggregate_id UUID NOT NULL,
    from_state_key VARCHAR(80) NOT NULL,
    to_state_key VARCHAR(80) NOT NULL,
    semantic_phase VARCHAR(40) NOT NULL,
    actor_id VARCHAR(160) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_workflow_audit_tenant ON workflow_transition_audit (hotel_id, created_at);

CREATE UNIQUE INDEX uq_workflow_initial_state ON workflow_states (workflow_id) WHERE initial_state;
CREATE TABLE workflow_instances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hotel_id UUID NOT NULL,
    workflow_id UUID NOT NULL,
    aggregate_id UUID NOT NULL,
    state_key VARCHAR(80) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_workflow_instance UNIQUE (hotel_id, workflow_id, aggregate_id),
    CONSTRAINT fk_workflow_instance_tenant FOREIGN KEY (hotel_id, workflow_id)
        REFERENCES workflow_definitions(hotel_id, id),
    CONSTRAINT fk_workflow_instance_state FOREIGN KEY (workflow_id, state_key)
        REFERENCES workflow_states(workflow_id, state_key)
);
