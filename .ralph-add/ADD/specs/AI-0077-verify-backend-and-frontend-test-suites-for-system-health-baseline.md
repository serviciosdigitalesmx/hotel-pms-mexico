# A.SPEC AI-0077 — Verify backend and frontend test suites for system health baseline

ID: AI-0077
Mode: VERIFY
RISK: LOW

## WHY
AI-0076 was already satisfied; a full verification run across backend Gradle services and React frontend is required to confirm codebase health baseline and identify any failing tests or compilation gaps.

## WHAT
Execute full unit and integration test suites for all backend modules and frontend component test runner.

## SCOPE
- Backend microservices test suite execution via `./gradlew test`
- Frontend component and utility test suite execution via `npm --prefix frontend test`

## OUT OF SCOPE
- Source code edits
- Database schema or Flyway migrations
- Production deployment or environment changes

## CONTRACT
- Run tests deterministically using repository Gradle wrapper and frontend npm scripts
- Report test execution pass/fail statuses across all modules

## INVARIANTS
- Source code and configuration files remain untouched

## VERIFICATION
- ./gradlew test
- npm --prefix frontend test

## ROLLBACK
No rollback necessary for non-mutating VERIFY step.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew test
- npm --prefix frontend test
END_VERIFY_COMMANDS
