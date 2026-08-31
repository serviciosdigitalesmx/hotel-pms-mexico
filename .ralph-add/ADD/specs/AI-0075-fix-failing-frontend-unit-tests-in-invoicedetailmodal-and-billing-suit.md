# A.SPEC AI-0075 — Fix failing frontend unit tests in InvoiceDetailModal and Billing suite

ID: AI-0075
Mode: WRITE
RISK: MEDIUM

## WHY
AI-0074 verification identified 5 failing test files (28 failed assertions) in the frontend test suite, primarily caused by FatturaPA download button elements in InvoiceDetailModal and related Billing tests not matching component state conditions.

## WHAT
Fix InvoiceDetailModal component rendering condition and test mocks so that FatturaPA XML buttons and modal details render deterministically as expected by test specs.

## SCOPE
- frontend/src/pages/Billing/InvoiceDetailModal.tsx
- frontend/src/pages/Billing/InvoiceDetailModal.test.tsx
- frontend/src/pages/Billing/InvoiceList.test.tsx

## OUT OF SCOPE
- Backend microservices
- Database schema and Flyway scripts
- Production deployment or auth configurations

## CONTRACT
- InvoiceDetailModal and related Billing tests pass cleanly with `npm --prefix frontend test`
- No regression in backend or existing pass-rate of other frontend components

## INVARIANTS
- All existing backend tests continue to pass
- No worktree discarding or destructive changes

## VERIFICATION
- npm --prefix frontend test

## ROLLBACK
git checkout frontend/src/pages/Billing/

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/Billing/
- frontend/src/components/
- frontend/src/services/
- frontend/src/locales/
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend test
END_VERIFY_COMMANDS
