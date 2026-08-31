# A.SPEC AI-0015 — Inspect LocalIntentRouter and LocalIntentRouterTest to diagnose test failures

ID: AI-0015
Mode: READ_ONLY
RISK: LOW

## WHY
AI-0014 failed with 3 test failures in LocalIntentRouterTest (NeverWantedButInvoked mock verification failures and IllegalStateException). Reading the test and implementation sources is required to formulate the precise write fix.

## WHAT
Examine LocalIntentRouter.java, LocalIntentRouterTest.java, and supporting engine classes to locate the source of mock expectation mismatches and exception handling.

## SCOPE
- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouter.java
- frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouterTest.java

## OUT OF SCOPE
- Modifying code or tests during READ_ONLY step
- Modules other than frontdesk-service

## CONTRACT
- Gather exact failure details line-by-line from LocalIntentRouterTest.java and LocalIntentRouter.java to design targeted fixes.

## INVARIANTS
- No workspace or source file mutations in READ_ONLY step.

## VERIFICATION
- Identify the specific lines and mocking assertions causing the 3 test failures.

## ROLLBACK
Not applicable for READ_ONLY step.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/
- frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test --tests com.hotelpms.frontdesk.assistant.engine.LocalIntentRouterTest
END_VERIFY_COMMANDS
