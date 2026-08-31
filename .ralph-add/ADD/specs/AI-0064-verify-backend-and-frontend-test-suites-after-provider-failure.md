# A.SPEC AI-0064 — Verify backend and frontend test suites after provider failure

ID: AI-0064
Mode: VERIFY
RISK: LOW

## WHY
AI-0063 failed with BLOCKED_PROVIDER without workspace changes. Running verification establishes a baseline check on system health across backend and frontend services.

## WHAT
Run root Gradle tests and frontend unit tests to confirm overall repository compilation and test suite health.

## SCOPE
- Backend Gradle unit and integration test execution across submodules
- Frontend Vitest suite execution

## OUT OF SCOPE
- Source code or configuration modifications
- Database migrations or deployment steps

## CONTRACT
- Gradle test task runs to completion for all submodules
- Frontend test script runs to completion

## INVARIANTS
- No source files altered during verification
- No financial, schema, or secret modifications executed

## VERIFICATION
- ./gradlew test
- npm --prefix frontend test

## ROLLBACK
No rollback required for read-only verification execution.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew test
- npm --prefix frontend test
END_VERIFY_COMMANDS
