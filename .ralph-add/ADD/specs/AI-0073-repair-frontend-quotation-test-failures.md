# A.SPEC AI-0073 — Repair frontend quotation test failures

ID: AI-0073
Mode: WRITE
RISK: LOW

## WHY
AI-0072 resulted in VERIFY_FAIL due to failing tests or missing mocks in Quotations.test.tsx and QuotationForm.test.tsx.

## WHAT
Fix mock setup, API response structures, and Vitest assertions in Quotations.test.tsx and QuotationForm.test.tsx to ensure all tests pass deterministically.

## SCOPE
- frontend/src/pages/Quotations.test.tsx
- frontend/src/pages/Quotations/QuotationForm.test.tsx
- frontend/src/pages/Quotations.tsx
- frontend/src/pages/Quotations/QuotationForm.tsx

## OUT OF SCOPE
- backend microservices
- database migrations
- unrelated frontend modules

## CONTRACT
- npm --prefix frontend test executes successfully and all quotation tests pass cleanly.

## INVARIANTS
- Do not delete assertions or skip test cases.
- Maintain existing quotation UI feature requirements and component structures.

## VERIFICATION
- npm --prefix frontend test

## ROLLBACK
git checkout -- frontend/src/pages/Quotations.test.tsx frontend/src/pages/Quotations/QuotationForm.test.tsx frontend/src/pages/Quotations.tsx frontend/src/pages/Quotations/QuotationForm.tsx

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/Quotations.test.tsx
- frontend/src/pages/Quotations/QuotationForm.test.tsx
- frontend/src/pages/Quotations.tsx
- frontend/src/pages/Quotations/QuotationForm.tsx
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend test
END_VERIFY_COMMANDS
