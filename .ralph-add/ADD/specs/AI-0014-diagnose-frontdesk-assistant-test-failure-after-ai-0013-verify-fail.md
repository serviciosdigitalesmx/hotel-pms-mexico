# A.SPEC AI-0014 — Diagnose frontdesk assistant test failure after AI-0013 VERIFY_FAIL

ID: AI-0014
Mode: VERIFY
RISK: LOW

## WHY
AI-0013 failed verification when modifying LocalIntentRouterTest. Running deterministic test verification against frontdesk-service captures the exact failure diagnostic needed for a repair step.

## WHAT
Execute LocalIntentRouterTest in frontdesk-service to inspect test execution results and identify failure causes.

## SCOPE
- frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouterTest.java

## OUT OF SCOPE
- Source code edits during diagnostic phase
- Changes to other service modules or DB schemas

## CONTRACT
- Run local Gradle test task for LocalIntentRouterTest deterministically
- Expose stdout/stderr test output without modifying repository files

## INVARIANTS
- No source or test code is altered during VERIFY mode
- Build sandbox remains clean and reproducible

## VERIFICATION
- ./gradlew :frontdesk-service:test --tests com.hotelpms.frontdesk.assistant.engine.LocalIntentRouterTest

## ROLLBACK
No code changes made; rollback is not required for VERIFY mode.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test --tests com.hotelpms.frontdesk.assistant.engine.LocalIntentRouterTest
END_VERIFY_COMMANDS
