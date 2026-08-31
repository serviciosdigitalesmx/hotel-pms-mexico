# A.SPEC AI-0071 — Verify backend multi-module build and frontend test suite

ID: AI-0071
Mode: VERIFY
RISK: LOW

## WHY
Validate full system build and test suite integrity across microservices and frontend after AI-0070 execution.

## WHAT
Execute Gradle test suite across all microservices and run Vitest test suite for the frontend application.

## SCOPE
- Backend Gradle microservices test verification
- Frontend test verification

## OUT OF SCOPE
- Source code modifications
- Database schema migrations
- Deployment or environment modifications

## CONTRACT
- All backend unit/integration tests compile and pass
- Frontend component and logic unit tests pass without errors

## INVARIANTS
- No source code or tracked files are modified
- Local environment setup remains untouched

## VERIFICATION
- ./gradlew test
- npm --prefix frontend test

## ROLLBACK
Not applicable for VERIFY mode.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew test
- npm --prefix frontend test
END_VERIFY_COMMANDS
