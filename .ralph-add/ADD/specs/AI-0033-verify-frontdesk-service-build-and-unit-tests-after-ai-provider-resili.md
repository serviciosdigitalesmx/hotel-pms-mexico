# A.SPEC AI-0033 — Verify frontdesk-service build and unit tests after AI provider resilience changes

ID: AI-0033
Mode: VERIFY
RISK: LOW

## WHY
AI-0032 updated AssistantService exception handling and resiliency configuration. Verification ensures all tests pass deterministically.

## WHAT
Execute frontdesk-service unit tests using the Gradle wrapper.

## SCOPE
- frontdesk-service test suite

## OUT OF SCOPE
- Source code or configuration edits
- Other microservices build unless implicitly required

## CONTRACT
- ./gradlew :frontdesk-service:test exits with code 0 and all tests pass.

## INVARIANTS
- No source files modified during verification.

## VERIFICATION
- ./gradlew :frontdesk-service:test

## ROLLBACK
No rollback needed for verification mode.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test
END_VERIFY_COMMANDS
