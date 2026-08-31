# A.SPEC AI-0024 — Repair local intent router unit tests in frontdesk-service

ID: AI-0024
Mode: WRITE
RISK: MEDIUM

## WHY
AI-0023 failed verification when updating local intent router test coverage. Repairing the router and test suite ensures deterministic local AI intent classification in frontdesk-service.

## WHAT
Fix assertion errors and implementation mismatches in LocalIntentRouter and LocalIntentRouterTest so that intent matching and fallback mechanisms execute cleanly.

## SCOPE
- Fix LocalIntentRouterTest and LocalIntentRouter under frontdesk-service assistant engine package.
- Ensure all test cases pass using standard Gradle test execution.

## OUT OF SCOPE
- Changes to other microservices or gateway routes.
- Database schema or Flyway migration edits.
- Remote AI provider integrations or API token modifications.

## CONTRACT
- LocalIntentRouter accurately maps intent prompts to handling strategies or fallback actions.
- LocalIntentRouterTest passes all test cases cleanly without build failure.

## INVARIANTS
- Frontdesk-service compilation remains unbroken.
- No security or internal auth permissions broken.

## VERIFICATION
- ./gradlew :frontdesk-service:test --tests "com.hotelpms.frontdesk.assistant.engine.LocalIntentRouterTest"

## ROLLBACK
Git checkout frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/ and frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/
- frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test --tests "com.hotelpms.frontdesk.assistant.engine.LocalIntentRouterTest"
END_VERIFY_COMMANDS
