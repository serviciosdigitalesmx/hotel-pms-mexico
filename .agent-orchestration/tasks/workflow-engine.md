Validation recovery: An independent Gradle run completed after 5m17s and revealed a missing jakarta.persistence.LockModeType import. The orchestrator corrected that import. Allow the initial Kotlin/Gradle configuration to finish; silence alone does not prove a hang. Run all required tests and resolve actual compiler errors. Main now contains customer-device migration V24__add_device_history_snapshot.sql: ensure the unintegrated workflow migration uses a distinct version before integration. Implement explicit permissions; ADMIN/OWNER role labels alone must not grant permission. Verify tenant and branch isolation and transition concurrency with tests.

Implement this assigned task from the operational DAG. The master contract closes product decisions. Choose technical details consistent with existing Camra code. Respect ownership and forbidden paths. Return the structured report; do not commit or merge.
{
  "id": "workflow-engine",
  "title": "Configurable workflow semantic engine",
  "objective": "Add configurable tenant/vertical workflow with shared semantic phases.",
  "dependencies": [
    "foundation-contracts"
  ],
  "affected_services": [
    "frontdesk-service"
  ],
  "owned_paths": [
    "frontdesk-service/**"
  ],
  "forbidden_paths": [
    "auth-service/**",
    "billing-service/**",
    "frontend/**"
  ],
  "database_changes": "workflow/config migrations",
  "API changes": "workflow and transition APIs",
  "frontend changes": "none",
  "permissions": "workflow permissions",
  "events": "status transition audit events",
  "tests_required": [
    "transition concurrency",
    "tenant workflow"
  ],
  "validation_commands": [
    "./gradlew :frontdesk-service:test"
  ],
  "acceptance_criteria": [
    "tenant-specific transitions map to common semantics"
  ],
  "estimated_parallelism_group": "G1",
  "status": "BLOCKED",
  "integration_risk": "HIGH"
}
