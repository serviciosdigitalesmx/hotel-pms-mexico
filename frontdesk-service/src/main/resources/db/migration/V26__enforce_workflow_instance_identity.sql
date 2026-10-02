ALTER TABLE workflow_instances
    ADD CONSTRAINT uq_workflow_instance_identity UNIQUE (hotel_id, workflow_id, aggregate_id);
