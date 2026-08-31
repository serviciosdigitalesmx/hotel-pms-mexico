# A.SPEC AI-0062 — Fix failing frontend unit tests for currency formatting and settings appearance

ID: AI-0062
Mode: WRITE
RISK: LOW

## WHY
AI-0061 verification revealed 12 failing test files in the frontend test suite, primarily due to currency symbol/formatting expectation mismatches (e.g., MX$ vs €) and i18n/appearance settings state assertions.

## WHAT
Update currency mock defaults / format expectations in RoomTypeList, RateSeasonManagerModal, SettingsAppearance, and related component test suites so that currency formatting and active language radio state match the system implementation.

## SCOPE
- frontend/src/pages/Rooms/RoomTypeList.test.tsx
- frontend/src/pages/Rooms/RateSeasonManagerModal.test.tsx
- frontend/src/pages/Settings/SettingsAppearance.test.tsx
- frontend/src/pages/Rooms/RoomTypeList.tsx
- frontend/src/pages/Rooms/RateSeasonManagerModal.tsx
- frontend/src/pages/Settings/SettingsAppearance.tsx
- frontend/src/context/CurrencyContext.tsx
- frontend/src/utils/formatters.ts

## OUT OF SCOPE
- Backend microservices
- Database schema or Flyway migrations
- package.json or root configuration files

## CONTRACT
- All frontend Vitest unit tests pass successfully via `npm --prefix frontend test`.
- Component functionality and UI formatting behavior remain consistent and accurate.

## INVARIANTS
- Preserve existing application structure and functionality.
- Do not mutate files outside frontend/src.

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
