# A.SPEC AI-0017 — locate AI intent router components and existing test suite

ID: AI-0017
Mode: READ_ONLY
RISK: LOW

## WHY
AI-0016 failed because LocalIntentRouterTest was not found in the test execution path. We need to locate where AI intent routing components and tests reside in the multi-project build.

## WHAT
Inspect repository files across modules to identify existing AI intent router code, interfaces, and test classes.

## SCOPE
- api-gateway
- common-web-lib
- config-service
- settings.gradle.kts

## OUT OF SCOPE
- Modifying source code or build configuration files

## CONTRACT
- Identify directory layout and existing AI-related implementation and test files without making mutations.

## INVARIANTS
- No source files, build scripts, or repository state modified.

## VERIFICATION
- ./gradlew projects

## ROLLBACK
No rollback needed for read-only inspection.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- .
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew projects
END_VERIFY_COMMANDS
