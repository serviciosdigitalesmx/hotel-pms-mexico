# A.SPEC AI-0054 — Verify frontend unit tests and TypeScript build

ID: AI-0054
Mode: VERIFY
RISK: LOW

## WHY
Validate that frontend unit tests pass and TypeScript compilation and Vite build succeed before introducing further V1 features.

## WHAT
Execute Vitest unit tests and production build for the frontend project.

## SCOPE
- frontend unit tests execution via vitest
- frontend typescript project compilation and vite build verification

## OUT OF SCOPE
- modifying frontend source code or dependencies
- e2e playwright tests execution
- backend gradle build verification

## CONTRACT
- Run vitest unit tests without modifying source files
- Verify TypeScript compilation and Vite bundler succeed with exit code 0

## INVARIANTS
- No repository modifications or git state changes
- No database, redis, or Docker state mutations

## VERIFICATION
- npm --prefix frontend run test exits with status code 0
- npm --prefix frontend run build exits with status code 0

## ROLLBACK
N/A for non-mutating VERIFY mode

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend run test
- npm --prefix frontend run build
END_VERIFY_COMMANDS
