# A.SPEC AI-0068 — Verify frontend unit tests and production build

ID: AI-0068
Mode: VERIFY
RISK: LOW

## WHY
Ensure the React frontend component suite passes all tests and builds cleanly after backend verifications.

## WHAT
Run frontend test suite and Vite build script to verify zero regressions, component integrity, and bundle health across all frontend modules.

## SCOPE
- frontend test execution
- frontend production build verification

## OUT OF SCOPE
- backend service changes
- frontend code modifications
- dependency updates

## CONTRACT
- frontend Vitest suite executes successfully without errors
- frontend Vite build produces static production bundle without compilation errors

## INVARIANTS
- no source code or configuration files modified during verification
- no external network dependencies or API deployments triggered

## VERIFICATION
- npm --prefix frontend test exits with status 0
- npm --prefix frontend run build exits with status 0

## ROLLBACK
No rollback needed as VERIFY does not mutate source repository files.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend test
- npm --prefix frontend run build
END_VERIFY_COMMANDS
