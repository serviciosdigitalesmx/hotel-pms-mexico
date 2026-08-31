# A.SPEC AI-0019 — Fix LocalIntentRouter unit test assertions and slot preservation behavior

ID: AI-0019
Mode: WRITE
RISK: MEDIUM

## WHY
AI-0018 failed verification because three tests in LocalIntentRouterTest failed (Mockito verification errors and IllegalStateException). Fixing these ensures deterministic intent routing and slot handling without regressions.

## WHAT
Fix the slot handling, fallback behavior, or mock expectations in LocalIntentRouter and LocalIntentRouterTest to ensure all unit tests pass cleanly.

## SCOPE
- frontdesk-service LocalIntentRouter implementation and tests

## OUT OF SCOPE
- Database schema changes
- Other services or external API contracts

## CONTRACT
- LocalIntentRouter correctly handles fallback and slot preservation without unexpected mock invocations or state errors.
- All tests in LocalIntentRouterTest pass cleanly.

## INVARIANTS
- No changes outside frontdesk-service assistant components.
- No real remote Groq/AI calls are made during unit tests.

## VERIFICATION
- ./gradlew :frontdesk-service:test --tests com.hotelpms.frontdesk.assistant.engine.LocalIntentRouterTest

## ROLLBACK
git checkout -- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant
- frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test --tests com.hotelpms.frontdesk.assistant.engine.LocalIntentRouterTest
END_VERIFY_COMMANDS
