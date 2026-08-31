# A.SPEC AI-0052 — Verify build and test suite status across PMS modules

ID: AI-0052
Mode: VERIFY
RISK: LOW

## WHY
AI-0051 updated unit tests in guest-service. We must verify that all module tests and build constraints pass cleanly without regressions across the multi-project build.

## WHAT
Run full Gradle unit test suite to validate current stability and test coverage across guest-service and dependent modules.

## SCOPE
- Run ./gradlew test across all Gradle modules.

## OUT OF SCOPE
- Source code changes.
- Database schema modifications.
- Frontend build execution.

## CONTRACT
- All Java/Kotlin test tasks in root gradle workspace return exit code 0.

## INVARIANTS
- No source files modified during verification.
- No destructive database or repository operations.

## VERIFICATION
- ./gradlew test

## ROLLBACK
Not applicable for VERIFY step.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew test
END_VERIFY_COMMANDS
