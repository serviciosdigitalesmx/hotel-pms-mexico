# A.SPEC AI-0029 — Fix unnecessary stubbings in LocalIntentRouterTest

ID: AI-0029
Mode: WRITE
RISK: LOW

## WHY
UnnecessaryStubbingException during Mockito afterEach in LocalIntentRouterTest prevents unknownMessageUsesGroqFallbackUnchanged() from succeeding.

## WHAT
Configure sessionStore stubbings in LocalIntentRouterTest setup using Mockito.lenient() or move stubbings to individual test cases where invoked, ensuring setup stubbings do not trigger unnecessary stubbing exceptions on fallback paths.

## SCOPE
- frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouterTest.java

## OUT OF SCOPE
- Production source files in frontdesk-service
- Any other service or test file

## CONTRACT
- LocalIntentRouterTest passes cleanly including unknownMessageUsesGroqFallbackUnchanged() without Mockito UnnecessaryStubbingException.

## INVARIANTS
- No changes to actual assistant intent routing code or production behaviors.
- All existing test assertions remain present and functional.

## VERIFICATION
- ./gradlew :frontdesk-service:test --tests "com.hotelpms.frontdesk.assistant.engine.LocalIntentRouterTest"

## ROLLBACK
git checkout frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouterTest.java

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouterTest.java
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test --tests "com.hotelpms.frontdesk.assistant.engine.LocalIntentRouterTest"
END_VERIFY_COMMANDS
