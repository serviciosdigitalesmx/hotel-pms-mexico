# A.SPEC AI-0044 — Fix RequestNotPermitted instantiation in ResilientIntentFallbackHandlerTest

ID: AI-0044
Mode: WRITE
RISK: LOW

## WHY
AI-0043 verification failed because RequestNotPermitted in ResilientIntentFallbackHandlerTest was called with new RequestNotPermitted(null), which does not match the available constructor signature.

## WHAT
Update ResilientIntentFallbackHandlerTest to instantiate or mock RequestNotPermitted using valid constructor arguments or Resilience4j factory methods.

## SCOPE
- frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/ResilientIntentFallbackHandlerTest.java

## OUT OF SCOPE
- Production code under frontdesk-service/src/main
- Other services or modules

## CONTRACT
- frontdesk-service test suite must compile cleanly.
- ResilientIntentFallbackHandlerTest must properly simulate rate limiting exceptions and verify fallback handling.

## INVARIANTS
- No production code changes are introduced.
- Existing test assertions for intent fallback resilience remain valid.

## VERIFICATION
- ./gradlew :frontdesk-service:test

## ROLLBACK
git checkout -- frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/ResilientIntentFallbackHandlerTest.java

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/ResilientIntentFallbackHandlerTest.java
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test
END_VERIFY_COMMANDS
