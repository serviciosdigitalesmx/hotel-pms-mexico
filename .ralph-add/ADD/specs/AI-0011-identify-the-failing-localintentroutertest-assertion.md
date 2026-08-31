# A.SPEC AI-0011 — Identify the failing LocalIntentRouterTest assertion

ID: AI-0011
Mode: VERIFY
RISK: LOW

## WHY
AI-0010 failed because the report only proves that tests failed; it does not expose the failing assertion or root cause.

## WHAT
Extract the concrete failure details from the deterministic Gradle test result artifacts already produced.

## SCOPE
- frontdesk-service/build/test-results/test
- frontdesk-service/build/reports/tests/test

## OUT OF SCOPE
- Source changes
- Test changes
- Dependency changes
- Re-running the same Gradle command without new diagnostics

## CONTRACT
- The result identifies the exact failing test and assertion, or reports that the artifacts are unavailable.

## INVARIANTS
- No source files are modified
- No services, databases, secrets, or external infrastructure are changed

## VERIFICATION
- Command exits successfully when matching test-result evidence exists
- Output includes the failing test name and failure message or clearly indicates missing artifacts

## ROLLBACK
No rollback required; this step is read-only.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- rg -n -i '<failure|<error|LocalIntentRouterTest|expected|actual' frontdesk-service/build/test-results/test frontdesk-service/build/reports/tests/test
END_VERIFY_COMMANDS
