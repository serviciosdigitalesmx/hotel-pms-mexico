# A.SPEC AI-0030 — Verify frontdesk-service test suite following AI-0029 failure

ID: AI-0030
Mode: VERIFY
RISK: LOW

## WHY
AI-0029 resulted in VERIFY_FAIL after changes to LocalIntentRouterTest.java. Running the frontdesk-service test task will isolate the failing assertions or build errors.

## WHAT
Execute Gradle test suite on frontdesk-service to capture detailed failure output for LocalIntentRouterTest.

## SCOPE
- frontdesk-service module tests

## OUT OF SCOPE
- Source code edits
- Database migrations
- Other microservices build tasks

## CONTRACT
- Run ./gradlew :frontdesk-service:test to evaluate test suite status.

## INVARIANTS
- No source files or tests modified during VERIFY.

## VERIFICATION
- ./gradlew :frontdesk-service:test

## ROLLBACK
No rollback needed for read-only test verification.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test
END_VERIFY_COMMANDS
