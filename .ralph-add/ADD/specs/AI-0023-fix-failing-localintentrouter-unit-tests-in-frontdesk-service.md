# A.SPEC AI-0023 — Fix failing LocalIntentRouter unit tests in frontdesk-service

ID: AI-0023
Mode: WRITE
RISK: MEDIUM

## WHY
AI-0022 verification failed due to two failures in LocalIntentRouterTest: TooManyActualInvocations in repeatedConfirmationDoesNotDuplicateCheckIn and UnnecessaryStubbingException in unknownMessageUsesGroqFallbackUnchanged.

## WHAT
Adjust LocalIntentRouterTest stubbing (making fallbacks lenient or removing unused stubs) and fix mock invocation expectations for repeated check-in confirmations.

## SCOPE
- frontdesk-service LocalIntentRouterTest fixes for Mockito stubbing and verification checks
- Minor adjustments to LocalIntentRouter if needed for idempotent intent routing behavior

## OUT OF SCOPE
- Modifications outside frontdesk-service
- Database migration or schema modifications
- Remote AI service configuration changes

## CONTRACT
- LocalIntentRouterTest must run cleanly with zero Mockito stubbing or invocation errors

## INVARIANTS
- Existing test suite for frontdesk-service must pass
- No non-deterministic thread/time dependencies introduced in tests

## VERIFICATION
- ./gradlew :frontdesk-service:test

## ROLLBACK
Revert modified files under frontdesk-service/src/

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service/src/main/java/
- frontdesk-service/src/test/java/
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test
END_VERIFY_COMMANDS
