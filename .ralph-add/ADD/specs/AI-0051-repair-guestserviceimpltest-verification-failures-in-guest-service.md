# A.SPEC AI-0051 — Repair GuestServiceImplTest verification failures in guest-service

ID: AI-0051
Mode: WRITE
RISK: MEDIUM

## WHY
AI-0050 resulted in VERIFY_FAIL during test execution of GuestServiceImplTest.java. The broken test or underlying service code needs repair to restore a clean test baseline.

## WHAT
Fix failing test cases, mocks, or service logic in GuestServiceImplTest and guest-service to achieve clean compilation and test execution.

## SCOPE
- guest-service/src/test/java/com/hotelpms/guest/service/impl/GuestServiceImplTest.java
- guest-service/src/main/java/com/hotelpms/guest/

## OUT OF SCOPE
- Other services in the repository
- Database schema or Flyway migration scripts

## CONTRACT
- All guest-service unit tests must pass deterministically via Gradle execution.

## INVARIANTS
- Do not break existing guest management domain contracts or endpoints.

## VERIFICATION
- Execute `./gradlew :guest-service:test` and verify test suite passes with exit code 0.

## ROLLBACK
git checkout HEAD -- guest-service/

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- guest-service/src/test/java/com/hotelpms/guest/service/impl/GuestServiceImplTest.java
- guest-service/src/main/java/com/hotelpms/guest/
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :guest-service:test
END_VERIFY_COMMANDS
