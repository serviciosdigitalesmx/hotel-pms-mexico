# A.SPEC AI-0020 — repair LocalIntentRouter implementation and test assertions in frontdesk-service

ID: AI-0020
Mode: WRITE
RISK: MEDIUM

## WHY
AI-0019 failed verification after modifying LocalIntentRouterTest.java. The local intent router or test assertions in frontdesk-service require repair to achieve a passing deterministic build.

## WHAT
Fix the LocalIntentRouter logic and LocalIntentRouterTest assertions in frontdesk-service to ensure robust, deterministic fallback handling, intent classification, and entity extraction.

## SCOPE
- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouter.java
- frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouterTest.java

## OUT OF SCOPE
- Changes to other microservices or core database schemas
- Changes to external AI provider API credentials or gateway routes

## CONTRACT
- LocalIntentRouter correctly matches known local intents and gracefully defaults to fallback on low confidence or unknown prompts
- LocalIntentRouterTest executes and passes completely without test failures or compile errors

## INVARIANTS
- No breaking changes to public frontdesk assistant APIs
- Determinism in local intent resolution test cases

## VERIFICATION
- ./gradlew :frontdesk-service:test --tests "com.hotelpms.frontdesk.assistant.engine.LocalIntentRouterTest"
- ./gradlew :frontdesk-service:test

## ROLLBACK
git checkout HEAD -- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/ frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/
- frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test --tests "com.hotelpms.frontdesk.assistant.engine.LocalIntentRouterTest"
- ./gradlew :frontdesk-service:test
END_VERIFY_COMMANDS
