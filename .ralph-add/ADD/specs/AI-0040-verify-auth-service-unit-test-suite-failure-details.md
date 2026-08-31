# A.SPEC AI-0040 — Verify auth-service unit test suite failure details

ID: AI-0040
Mode: VERIFY
RISK: LOW

## WHY
AI-0039 failed verification when modifying UserManagementServiceImplTest. Running the auth-service test suite captures the exact compiler or test failure for diagnostic purposes.

## WHAT
Execute auth-service unit tests via Gradle to obtain test diagnostic evidence.

## SCOPE
- Execute Gradle test task for auth-service module.

## OUT OF SCOPE
- Modifying source or test files
- Running non-auth service tests

## CONTRACT
- Run Gradle test runner on auth-service to expose underlying test or build failure.

## INVARIANTS
- Repository source files remain untouched.
- Build caches and test reports written only to standard build outputs.

## VERIFICATION
- ./gradlew :auth-service:test

## ROLLBACK
No code changes made in VERIFY mode.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :auth-service:test
END_VERIFY_COMMANDS
