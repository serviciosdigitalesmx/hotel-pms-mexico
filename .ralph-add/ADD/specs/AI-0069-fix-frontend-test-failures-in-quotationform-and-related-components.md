# A.SPEC AI-0069 — Fix frontend test failures in QuotationForm and related components

ID: AI-0069
Mode: WRITE
RISK: MEDIUM

## WHY
Verification AI-0068 showed 7 failing test files (37 tests) in the frontend due to currency formatting mismatches (MX$ vs €) and related test expectation mismatches. Fixing these ensures clean test suite execution.

## WHAT
Update currency expectations and component state assertions in frontend tests so they match the configured default currency (MXN) and current UI specs.

## SCOPE
- frontend/src/**/*.test.tsx
- frontend/src/**/*.test.ts
- frontend/src/pages/Quotations/QuotationForm.test.tsx

## OUT OF SCOPE
- Backend microservices
- Database schema or migrations
- Production build configuration

## CONTRACT
- npm --prefix frontend test exits with code 0
- npm --prefix frontend run build exits with code 0

## INVARIANTS
- No destructive workspace changes
- All pre-existing functional behavior retained

## VERIFICATION
- npm --prefix frontend test
- npm --prefix frontend run build

## ROLLBACK
git checkout -- frontend/src

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend test
- npm --prefix frontend run build
END_VERIFY_COMMANDS
