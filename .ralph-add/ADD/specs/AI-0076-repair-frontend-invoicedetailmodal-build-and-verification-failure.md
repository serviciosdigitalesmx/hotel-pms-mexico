# A.SPEC AI-0076 — Repair frontend InvoiceDetailModal build and verification failure

ID: AI-0076
Mode: WRITE
RISK: MEDIUM

## WHY
AI-0075 resulted in VERIFY_FAIL on frontend/src/pages/Billing/InvoiceDetailModal.tsx. We need to fix TypeScript, JSX, or prop type errors so that the frontend builds cleanly.

## WHAT
Repair frontend/src/pages/Billing/InvoiceDetailModal.tsx to resolve build and verification issues while maintaining full invoice details, fiscal data, and payment breakdown functionality.

## SCOPE
- frontend/src/pages/Billing/InvoiceDetailModal.tsx

## OUT OF SCOPE
- Backend billing-service code
- Database schema and Flyway migrations
- RBAC or security library changes

## CONTRACT
- frontend/src/pages/Billing/InvoiceDetailModal.tsx must pass npm --prefix frontend run build with zero TypeScript or JSX errors.
- Invoice details modal displays invoice line items, tax details, customer fiscal data, and status correctly.

## INVARIANTS
- Do not remove pre-existing functionality or components in the billing workflow.
- Do not mutate files outside allowed_paths.

## VERIFICATION
- npm --prefix frontend run build

## ROLLBACK
git checkout -- frontend/src/pages/Billing/InvoiceDetailModal.tsx

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/Billing/InvoiceDetailModal.tsx
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend run build
END_VERIFY_COMMANDS
