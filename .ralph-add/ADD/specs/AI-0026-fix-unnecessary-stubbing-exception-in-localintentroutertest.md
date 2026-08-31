# A.SPEC AI-0026 — Fix unnecessary stubbing exception in LocalIntentRouterTest

ID: AI-0026
Mode: WRITE
RISK: LOW

## WHY
AI-0025 verification failed due to Mockito strictness throwing UnnecessaryStubbingException in unknownMessageUsesGroqFallbackUnchanged().

## WHAT
Adjust Mockito stubbing in LocalIntentRouterTest to remove or mark lenient unused stubs during the fallback test case execution.

## SCOPE
- frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouterTest.java

## OUT OF SCOPE
- Production logic in frontdesk-service
- Other microservices or tests

## CONTRACT
- LocalIntentRouterTest executes cleanly without Mockito stubbing failures under strict extension settings.

## INVARIANTS
- Test coverage for intent routing logic and Groq fallback remains effective and valid.

## VERIFICATION
- ./gradlew :frontdesk-service:test --tests com.hotelpms.frontdesk.assistant.engine.LocalIntentRouterTest

## ROLLBACK
Git checkout or revert changes in LocalIntentRouterTest.java.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouterTest.java
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test --tests com.hotelpms.frontdesk.assistant.engine.LocalIntentRouterTest
END_VERIFY_COMMANDS
