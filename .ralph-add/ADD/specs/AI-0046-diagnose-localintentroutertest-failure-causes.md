# A.SPEC AI-0046 — Diagnose LocalIntentRouterTest failure causes

ID: AI-0046
Mode: READ_ONLY
RISK: LOW

## WHY
9 unit tests in LocalIntentRouterTest failed with IllegalStateException at line 236 during verification. Inspecting the test file around line 236 and the underlying LocalIntentRouter state handling is required to prepare a precise fix.

## WHAT
Inspect LocalIntentRouterTest.java context around line 236 and the associated implementation in frontdesk-service to pinpoint why IllegalStateException is thrown.

## SCOPE
- frontdesk-service/src/test/java/**/LocalIntentRouterTest.java
- frontdesk-service/src/main/java/**/LocalIntentRouter.java

## OUT OF SCOPE
- Modifying source or test files during this step
- Modules outside frontdesk-service

## CONTRACT
- Examine test line 236 assertions and router execution flow to determine the root cause of the IllegalStateException.

## INVARIANTS
- No workspace files or build artifacts are modified in READ_ONLY mode.

## VERIFICATION
- Locate LocalIntentRouterTest.java line 236 and the associated method calls in LocalIntentRouter.java.

## ROLLBACK
No rollback needed for READ_ONLY mode.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
END_VERIFY_COMMANDS
