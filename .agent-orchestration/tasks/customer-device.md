Implement this assigned task from the operational DAG. The master contract closes product decisions. Choose technical details consistent with existing Camra code. Respect ownership and forbidden paths. Return the structured report; do not commit or merge.\n{
  "id": "customer-device",
  "title": "Customer and persistent device model",
  "objective": "Create Customer to many Devices and preserve order snapshots/history.",
  "dependencies": [
    "foundation-contracts"
  ],
  "affected_services": [
    "guest-service",
    "frontdesk-service"
  ],
  "owned_paths": [
    "guest-service/**",
    "frontdesk-service/**"
  ],
  "forbidden_paths": [
    "billing-service/**",
    "frontend/**"
  ],
  "database_changes": "additive customer/device migrations",
  "API changes": "customer/device APIs",
  "frontend changes": "none",
  "permissions": "tenant scoped",
  "events": "customer/device audit events",
  "tests_required": [
    "identity matching",
    "tenant isolation",
    "snapshot immutability"
  ],
  "validation_commands": [
    "./gradlew :guest-service:test :frontdesk-service:test"
  ],
  "acceptance_criteria": [
    "one customer has many devices; orders retain historical snapshot"
  ],
  "estimated_parallelism_group": "G1",
  "status": "BLOCKED",
  "integration_risk": "HIGH"
}
