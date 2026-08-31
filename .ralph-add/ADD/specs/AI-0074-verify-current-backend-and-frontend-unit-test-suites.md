# A.SPEC AI-0074 — Verify current backend and frontend unit test suites

ID: AI-0074
Mode: VERIFY
RISK: LOW

## WHY
The previous WRITE step AI-0073 encountered an external provider exhaustion error without mutating the workspace. A deterministic VERIFY step is required to re-baseline unit test execution and verify workspace integrity across backend and frontend services.

## WHAT
Run root Gradle test execution for all backend modules and run Vitest suite for the frontend application.

## SCOPE
- Execute ./gradlew test for backend microservices
- Execute npm --prefix frontend test for frontend application

## OUT OF SCOPE
- Source code or asset modifications
- Database migrations or schema changes
- Deployment or external infrastructure interactions
- Modifying secrets, configuration, or RBAC definitions

## CONTRACT
- Run backend and frontend tests without modifying source files
- Provide deterministic report on passing/failing test suites to guide the next atomic WRITE step

## INVARIANTS
- No source code or configuration files shall be modified
- No destructive Git or volume operations shall be issued

## VERIFICATION
- ./gradlew test
- npm --prefix frontend test

## ROLLBACK
No rollback required as VERIFY performs no source mutations.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew test
- npm --prefix frontend test
END_VERIFY_COMMANDS
