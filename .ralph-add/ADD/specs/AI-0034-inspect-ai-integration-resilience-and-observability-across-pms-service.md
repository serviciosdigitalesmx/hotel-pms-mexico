# A.SPEC AI-0034 — inspect AI integration resilience and observability across PMS services

ID: AI-0034
Mode: READ_ONLY
RISK: LOW

## WHY
Inspect existing AI integration implementations, circuit breakers, timeout/retry policies, fallbacks, and token tracking across services to determine if further resilience steps are required.

## WHAT
Examine AI service configurations, client wrappers, resilience decorators, and test coverage in backend services.

## SCOPE
- frontdesk-service/src
- guest-service/src
- common-web-lib/src
- config-service/src

## OUT OF SCOPE
- Editing application source or configuration files
- Executing build or test tasks during inspection

## CONTRACT
- Perform read-only inspection of repository files to evaluate AI production readiness and identify remaining gaps.

## INVARIANTS
- No source or workspace mutation shall occur during this step.

## VERIFICATION
- Workspace status remains unchanged.

## ROLLBACK
No changes made; no rollback required.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
END_VERIFY_COMMANDS
