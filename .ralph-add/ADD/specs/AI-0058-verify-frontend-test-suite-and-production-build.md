# A.SPEC AI-0058 — Verify frontend test suite and production build

ID: AI-0058
Mode: VERIFY
RISK: LOW

## WHY
Backend test suite (AI-0057) is verified PASS. To ensure full system stability for V1 readiness, we must verify that the React frontend builds cleanly and passes all frontend tests.

## WHAT
Run frontend test suite and frontend build check using npm in the frontend module.

## SCOPE
- Frontend test execution (`npm test`)
- Frontend build execution (`npm run build`)

## OUT OF SCOPE
- Source code modifications
- Backend microservices execution
- Database migrations or remote operations

## CONTRACT
- Frontend unit/component tests exit with status code 0
- Frontend production bundle compiles with status code 0 and no fatal build errors

## INVARIANTS
- No source files in the worktree are mutated
- No secrets or security configs are modified

## VERIFICATION
- npm --prefix frontend test -- --watchAll=false
- npm --prefix frontend run build

## ROLLBACK
N/A (read-only verification step)

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend test -- --watchAll=false
- npm --prefix frontend run build
END_VERIFY_COMMANDS
