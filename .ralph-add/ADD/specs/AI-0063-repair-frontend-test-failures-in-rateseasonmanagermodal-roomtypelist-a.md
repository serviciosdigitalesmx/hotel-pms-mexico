# A.SPEC AI-0063 — Repair frontend test failures in RateSeasonManagerModal, RoomTypeList, and SettingsAppearance

ID: AI-0063
Mode: WRITE
RISK: LOW

## WHY
AI-0062 resulted in VERIFY_FAIL when updating frontend test suites. The test assertions, async element waiting, or mock setups in RateSeasonManagerModal, RoomTypeList, and SettingsAppearance need to be properly aligned with the React/Vitest component implementations so all tests pass cleanly.

## WHAT
Fix mock responses, query matchers, and async rendering assertions in RateSeasonManagerModal.test.tsx, RoomTypeList.test.tsx, and SettingsAppearance.test.tsx.

## SCOPE
- frontend/src/pages/Rooms/RateSeasonManagerModal.test.tsx
- frontend/src/pages/Rooms/RoomTypeList.test.tsx
- frontend/src/pages/Settings/SettingsAppearance.test.tsx

## OUT OF SCOPE
- Backend microservices
- Database schema or Flyway migrations
- Any files outside the three specified frontend test files

## CONTRACT
- All test cases in frontend/src/pages/Rooms/RateSeasonManagerModal.test.tsx, RoomTypeList.test.tsx, and SettingsAppearance.test.tsx pass successfully when running npm --prefix frontend test.

## INVARIANTS
- Do not disable tests using test.skip or fit/it.only.
- Do not mutate application source code in frontend/src/pages/ (only modify test files).
- Ensure mock data accurately reflects domain contracts.

## VERIFICATION
- npm --prefix frontend test

## ROLLBACK
Git checkout or restore the test files to their state before AI-0062.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/Rooms/RateSeasonManagerModal.test.tsx
- frontend/src/pages/Rooms/RoomTypeList.test.tsx
- frontend/src/pages/Settings/SettingsAppearance.test.tsx
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend test
END_VERIFY_COMMANDS
