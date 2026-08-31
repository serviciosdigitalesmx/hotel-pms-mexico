# A.SPEC AI-0060 — Repair failing frontend test suite i18n mock and component query assertions

ID: AI-0060
Mode: WRITE
RISK: MEDIUM

## WHY
AI-0059 verification failed due to 24 failing frontend test files caused by i18n key resolution mismatches and DOM element query assertions. Repairing these test files will establish a fully green frontend test suite.

## WHAT
Update frontend test setup and test files (including SettingsSystem.test.tsx and related component tests) to resolve translation key mocks and accessible role queries correctly.

## SCOPE
- frontend/src/**/*.test.tsx
- frontend/src/**/*.test.ts
- frontend/src/test/**/*

## OUT OF SCOPE
- Backend microservices
- Flyway migrations and DB schema
- Production deployment configuration

## CONTRACT
- npm --prefix frontend test exits with 0 and all tests pass
- npm --prefix frontend run build completes cleanly

## INVARIANTS
- Do not suppress tests with it.skip or remove assertions without valid replacements
- Do not modify application runtime business logic unless correcting component testability attributes

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
