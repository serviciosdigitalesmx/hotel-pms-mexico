# A.SPEC AI-0028 — Inspect frontdesk-service test failure details and stack trace

ID: AI-0028
Mode: READ_ONLY
RISK: LOW

## WHY
The verification run for :frontdesk-service failed with 1 failing test out of 408 tests. We need to inspect the generated test report XML files to identify the failing test method, stack trace, and root cause.

## WHAT
Read test results under frontdesk-service/build/test-results/test/ and corresponding test sources to pinpoint the broken test assertion.

## SCOPE
- frontdesk-service/build/test-results/test/
- frontdesk-service/src/test/

## OUT OF SCOPE
- Mutating source or test files in any module
- Executing build or test runner commands

## CONTRACT
- Identify the exact class and method name of the single failing test in frontdesk-service along with its failure message and expected vs actual state.

## INVARIANTS
- Zero mutation of workspace files or configuration.

## VERIFICATION
- Verify that the failing test name and failure details are accurately extracted from test results.

## ROLLBACK
N/A for read-only diagnostics.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
END_VERIFY_COMMANDS
