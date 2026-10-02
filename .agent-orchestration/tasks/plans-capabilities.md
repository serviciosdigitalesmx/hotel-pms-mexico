Implement this assigned task from the operational DAG. The master contract closes product decisions. Choose technical details consistent with existing Camra code. Respect ownership and forbidden paths. Return the structured report; do not commit or merge.\n{
  "id": "plans-capabilities",
  "title": "Plans and capabilities engine",
  "objective": "Gate modules/capabilities without fixing commercial prices.",
  "dependencies": [
    "foundation-contracts"
  ],
  "affected_services": [
    "auth-service",
    "config-service"
  ],
  "owned_paths": [
    "auth-service/**",
    "config-service/**"
  ],
  "forbidden_paths": [
    "frontend/**"
  ],
  "database_changes": "plan/capability migrations",
  "API changes": "capability evaluation API",
  "frontend changes": "none",
  "permissions": "capability plus permission checks",
  "events": "plan audit events",
  "tests_required": [
    "tenant isolation",
    "capability enforcement"
  ],
  "validation_commands": [
    "./gradlew :auth-service:test :config-service:test"
  ],
  "acceptance_criteria": [
    "prices are not hardcoded as product authority"
  ],
  "estimated_parallelism_group": "G1",
  "status": "BLOCKED",
  "integration_risk": "MEDIUM"
}
