# A.SPEC AI-0022 — Verify frontdesk-service test failures following AI-0021 VERIFY_FAIL

ID: AI-0022
Mode: VERIFY
RISK: LOW

## WHY
AI-0021 failed verification. Executing the frontdesk-service test suite directly captures the exact compiler or assertion errors required to target the repair step.

## WHAT
Execute frontdesk-service tests using the root Gradle wrapper to collect deterministic failure logs.

## SCOPE
- frontdesk-service unit and integration test execution

## OUT OF SCOPE
- Modifying application code, test classes, or build scripts

## CONTRACT
- Run ./gradlew :frontdesk-service:test safely to produce failure details without modifying source code.

## INVARIANTS
- Source files and configuration remain untouched during execution.

## VERIFICATION
- ./gradlew :frontdesk-service:test

## ROLLBACK
No rollback required as VERIFY mode makes no source modifications.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test
END_VERIFY_COMMANDS
