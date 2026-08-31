# A.SPEC AI-0059 — verify frontend test suite and production build

ID: AI-0059
Mode: VERIFY
RISK: LOW

## WHY
AI-0058 failed because --watchAll=false is a Jest CLI flag invalid in Vitest. Executing vitest run via npm --prefix frontend test without invalid flags will verify the actual frontend test suite status and production build.

## WHAT
Run frontend test suite with vitest and verify TypeScript compilation and Vite production build pass cleanly without errors.

## SCOPE
- frontend test suite execution via Vitest
- frontend production build via Vite

## OUT OF SCOPE
- frontend source changes
- backend Gradle builds
- database migrations or Docker container executions

## CONTRACT
- npm --prefix frontend test exits with code 0
- npm --prefix frontend run build exits with code 0

## INVARIANTS
- Worktree and source code remain completely untouched
- No external network or production systems accessed

## VERIFICATION
- npm --prefix frontend test
- npm --prefix frontend run build

## ROLLBACK
No code changes were made; no rollback required.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend test
- npm --prefix frontend run build
END_VERIFY_COMMANDS
