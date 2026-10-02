Implement this assigned task from the operational DAG. The master contract closes product decisions. Choose technical details consistent with existing Camra code. Respect ownership and forbidden paths. Return the structured report; do not commit or merge.\n{
  "id": "platform-gates",
  "title": "Cross-service migration, security and observability gates",
  "objective": "Continuously validate Flyway, Docker, contracts, isolation and regressions.",
  "dependencies": [
    "foundation-contracts"
  ],
  "affected_services": [
    "all"
  ],
  "owned_paths": [
    "scripts/**",
    ".github/**",
    "docker/**"
  ],
  "forbidden_paths": [
    "**/src/main/**"
  ],
  "database_changes": "validation only",
  "API changes": "contract checks",
  "frontend changes": "build validation",
  "permissions": "security gate",
  "events": "agent audit logs",
  "tests_required": [
    "all configured gates"
  ],
  "validation_commands": [
    "scripts/integration-validate.ps1"
  ],
  "acceptance_criteria": [
    "failed validation returns task to worker"
  ],
  "estimated_parallelism_group": "G0",
  "status": "READY",
  "integration_risk": "HIGH"
}
