# A.SPEC AI-0057 — Verify backend build and test suite execution via root Gradle wrapper

ID: AI-0057
Mode: VERIFY
RISK: LOW

## WHY
Frontend npm commands were blocked by supervisor verification policy in AI-0056. Backend Gradle build wrapper is present at root and needs verification to ensure all Java modules compile and pass test suites.

## WHAT
Execute `./gradlew test` from repository root to verify all backend Gradle subprojects (guest-service, frontdesk-service, billing-service, auth-service, api-gateway, etc.).

## SCOPE
- Backend subprojects test execution
- Java test suite and build verification

## OUT OF SCOPE
- npm/frontend execution
- Database schema migrations
- Source code mutations

## CONTRACT
- Gradle test execution reports pass/fail across all submodules deterministically without modifying source files.

## INVARIANTS
- Worktree and source code remain completely untracked-mutation free.

## VERIFICATION
- ./gradlew test

## ROLLBACK
No rollback required for VERIFY mode.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew test
END_VERIFY_COMMANDS
