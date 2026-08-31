# A.SPEC AI-0065 — Fix frontend i18n language safety fallbacks and rate calendar test assertions

ID: AI-0065
Mode: WRITE
RISK: MEDIUM

## WHY
AI-0064 verification failed because `RoomSelection.tsx` accessed `i18n.language` without optional chaining/fallback when `i18n` is mocked in unit tests, and currency string formatting assertions in `RateCalendar.test.tsx` failed due to locale/spacing mismatch.

## WHAT
Safeguard `i18n?.language` calls with default fallbacks (e.g. `'es'` or `'en'`) across frontend components and normalize currency/i18n matchers in affected frontend test files.

## SCOPE
- frontend/src/pages/Reservations/RoomSelection.tsx
- frontend/src/pages/Reservations/RoomSelection.test.tsx
- frontend/src/pages/Rates/RateCalendar.tsx
- frontend/src/pages/Rates/RateCalendar.test.tsx

## OUT OF SCOPE
- backend services
- database migrations
- RBAC / internal-auth-lib

## CONTRACT
- All components using `i18n.language` for `Intl.NumberFormat` or date formatting must handle `i18n?.language` being undefined gracefully
- All vitest test suites in `frontend` pass with zero failures

## INVARIANTS
- Do not alter user-facing UI behavior or application business logic
- Do not modify package dependencies

## VERIFICATION
- npm --prefix frontend test

## ROLLBACK
git checkout -- frontend/src/

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/Reservations/
- frontend/src/pages/Rates/
- frontend/src/components/
- frontend/src/utils/
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend test
END_VERIFY_COMMANDS
