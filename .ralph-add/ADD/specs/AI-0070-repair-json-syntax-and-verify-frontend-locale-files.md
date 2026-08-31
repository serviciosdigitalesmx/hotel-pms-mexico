# A.SPEC AI-0070 — Repair JSON syntax and verify frontend locale files

ID: AI-0070
Mode: WRITE
RISK: MEDIUM

## WHY
AI-0069 resulted in VERIFY_FAIL on English locale JSON files. We must repair JSON syntax/formatting in these locale files so frontend build passes deterministically.

## WHAT
Fix JSON syntax errors, missing trailing quotes/commas, or structure issues in quotations.json, reservations.json, and restaurant.json.

## SCOPE
- frontend/src/locales/en/quotations.json
- frontend/src/locales/en/reservations.json
- frontend/src/locales/en/restaurant.json

## OUT OF SCOPE
- Backend services
- Flyway database migrations
- Auth and RBAC changes

## CONTRACT
- All three modified JSON files must contain valid, parseable JSON.
- npm --prefix frontend run build must succeed without error.

## INVARIANTS
- No existing required translation keys are removed.
- No modifications are made outside allowed_paths.

## VERIFICATION
- npm --prefix frontend run build

## ROLLBACK
Discard uncommitted changes in frontend/src/locales/en/

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/locales/en/quotations.json
- frontend/src/locales/en/reservations.json
- frontend/src/locales/en/restaurant.json
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend run build
END_VERIFY_COMMANDS
