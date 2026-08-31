# A.SPEC AI-0066 — Repair RateCalendar and RoomSelection frontend tests

ID: AI-0066
Mode: WRITE
RISK: MEDIUM

## WHY
AI-0065 failed auto-verification on RateCalendar.test.tsx and RoomSelection.test.tsx. Standardizing component context providers and test mocks will restore frontend test suite health.

## WHAT
Repair missing context providers (Router/QueryClient), mocked API responses, and test assertions in RateCalendar.test.tsx and RoomSelection.test.tsx.

## SCOPE
- frontend/src/pages/Rates/RateCalendar.test.tsx
- frontend/src/pages/Reservations/RoomSelection.test.tsx
- frontend/src/pages/Rates/RateCalendar.tsx
- frontend/src/pages/Reservations/RoomSelection.tsx

## OUT OF SCOPE
- Backend microservices
- Database schema or Flyway migrations
- RBAC or security configurations

## CONTRACT
- npm --prefix frontend test passes with 0 failures.
- npm --prefix frontend run build completes cleanly.

## INVARIANTS
- No change to package.json or package-lock.json.
- No alteration of backend microservices or global application routing.

## VERIFICATION
- npm --prefix frontend test
- npm --prefix frontend run build

## ROLLBACK
git checkout -- frontend/src/pages/Rates/RateCalendar.test.tsx frontend/src/pages/Reservations/RoomSelection.test.tsx frontend/src/pages/Rates/RateCalendar.tsx frontend/src/pages/Reservations/RoomSelection.tsx

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/Rates/RateCalendar.test.tsx
- frontend/src/pages/Reservations/RoomSelection.test.tsx
- frontend/src/pages/Rates/RateCalendar.tsx
- frontend/src/pages/Reservations/RoomSelection.tsx
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend test
- npm --prefix frontend run build
END_VERIFY_COMMANDS
