# A.SPEC AI-0021 — Implement resilient fallback handler for frontdesk AI intent router

ID: AI-0021
Mode: WRITE
RISK: MEDIUM

## WHY
AI-0020 failed due to git patch apply failures on LocalIntentRouter.java. Extracting resilient fallback routing into a dedicated component modularizes provider error and timeout handling while preventing line-drift patch failures.

## WHAT
Create ResilientIntentFallbackHandler to manage fallback intent resolution when primary AI models fail, and integrate it modularly into LocalIntentRouter with unit test coverage.

## SCOPE
- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/ResilientIntentFallbackHandler.java
- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouter.java
- frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/ResilientIntentFallbackHandlerTest.java

## OUT OF SCOPE
- Database schema changes or Flyway scripts
- External API credentials or remote secrets modification
- Changes outside frontdesk-service module

## CONTRACT
- ResilientIntentFallbackHandler safely intercepts intent routing failures and returns deterministic fallback intent classifications.
- LocalIntentRouter integrates fallback delegation without breaking existing intent routing logic.

## INVARIANTS
- Existing intent routing behavior for valid requests must remain unchanged.
- No secrets or hardcoded remote URLs in code.
- All unit tests in frontdesk-service must pass.

## VERIFICATION
- Run ./gradlew :frontdesk-service:test and verify clean compilation and passing test suite.

## ROLLBACK
Git checkout LocalIntentRouter.java and remove added files ResilientIntentFallbackHandler.java and ResilientIntentFallbackHandlerTest.java.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/ResilientIntentFallbackHandler.java
- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouter.java
- frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/ResilientIntentFallbackHandlerTest.java
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test
END_VERIFY_COMMANDS
