# A.SPEC AI-0072 — Fix failing frontend unit tests for currency formatting and quotation specs

ID: AI-0072
Mode: WRITE
RISK: LOW

## WHY
AI-0071 verification failed because 7 frontend test files (37 tests) failed due to currency formatting mismatches (e.g., MX$ vs €) and outdated test assertions.

## WHAT
Fix the failing frontend unit test assertions (e.g., in QuotationForm.test.tsx and related frontend test files) to align with the current currency formatting utilities and component rendering behavior.

## SCOPE
- frontend/src

## OUT OF SCOPE
- backend services
- database migrations
- infrastructure

## CONTRACT
- npm --prefix frontend test completes with 0 test failures.

## INVARIANTS
- Do not break existing passing frontend tests or change backend functionality.
- Preserve existing worktree changes and component logic.

## VERIFICATION
- npm --prefix frontend test

## ROLLBACK
git checkout -- frontend/src

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend test
END_VERIFY_COMMANDS
