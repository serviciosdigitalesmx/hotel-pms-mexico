# A.SPEC AI-0037 — Verify full backend test suite across all services

ID: AI-0037
Mode: VERIFY
RISK: LOW

## WHY
Validate that all backend microservices compile and pass unit/integration tests to guarantee overall system stability after AI resilience enhancements.

## WHAT
Execute the full Gradle test suite across all modules.

## SCOPE
- Backend services test suite execution.

## OUT OF SCOPE
- Modifying source files or test files
- Frontend build or test tasks

## CONTRACT
- Run `./gradlew test` without altering repository source code or configurations.

## INVARIANTS
- Zero workspace mutations detected.
- All backend tests pass cleanly.

## VERIFICATION
- Output indicates BUILD SUCCESSFUL for `./gradlew test`.

## ROLLBACK
No rollback needed for VERIFY execution mode.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew test
END_VERIFY_COMMANDS
