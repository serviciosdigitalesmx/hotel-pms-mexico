# A.SPEC AI-0027 — Verify frontdesk-service test suite to diagnose LocalIntentRouterTest failure

ID: AI-0027
Mode: VERIFY
RISK: LOW

## WHY
AI-0026 failed verification during LocalIntentRouterTest execution. Running frontdesk-service tests with diagnostic logs will clarify the failure cause so a focused repair can be applied.

## WHAT
Execute Gradle test target for frontdesk-service module to capture detailed test execution and assertion output.

## SCOPE
- Run frontdesk-service module unit tests via gradlew wrapper
- Capture failure evidence for LocalIntentRouterTest

## OUT OF SCOPE
- Modifying source files or test files in this step
- Modifying other service modules

## CONTRACT
- Execution of test command must be deterministic and non-destructive
- Test report/output must identify failing test method and stack trace

## INVARIANTS
- No source code changes occur in VERIFY mode
- Only root gradlew wrapper from build manifest is executed

## VERIFICATION
- ./gradlew :frontdesk-service:test --info

## ROLLBACK
Git reset not needed as VERIFY mode makes no source modifications.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test --info
END_VERIFY_COMMANDS
