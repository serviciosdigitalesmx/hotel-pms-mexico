# A.SPEC FRONTEND-0001 — Repair HotelProfile Vitest failures caused by fiscal validation contract mismatch

ID: FRONTEND-0001
Mode: WRITE
RISK: LOW

## WHY
The latest run has 15 failures in HotelProfile. The concrete mismatch is that the implementation emits common:err_invalid_fiscal_code while the test expects common:err_invalid_vat; the backend-detail toast assertion also needs deterministic isolation.

## WHAT
Align the fiscal validation message with the existing frontend test and real translation contract, preserve backend detail propagation, and make only the minimum test-isolation adjustment required by the observed failures.

## SCOPE
- HotelProfile VAT validation
- HotelProfile save error toast
- Focused Vitest regression verification

## OUT OF SCOPE
- Database or Flyway changes
- Secrets
- Backend API changes
- Other frontend pages
- Production deployment

## CONTRACT
- Malformed VAT blocks updateHotelSettings
- Validation renders the established fiscal-code error
- Backend detail is shown verbatim in the error toast
- Existing HotelProfile behavior remains unchanged

## INVARIANTS
- No applied migration is edited
- No user-owned work is discarded
- No network or production state is changed
- Vitest runs with threads and one worker

## VERIFICATION
- HotelProfile.test.tsx passes completely
- No new TypeScript errors are introduced in the touched files

## ROLLBACK
Revert only the changes in frontend/src/pages/HotelProfile.tsx and frontend/src/pages/HotelProfile.test.tsx.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/HotelProfile.tsx
- frontend/src/pages/HotelProfile.test.tsx
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend exec -- vitest run --root frontend src/pages/HotelProfile.test.tsx --pool=threads --maxWorkers=1 --reporter=dot
END_VERIFY_COMMANDS
