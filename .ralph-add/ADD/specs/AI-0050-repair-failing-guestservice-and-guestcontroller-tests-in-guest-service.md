# A.SPEC AI-0050 — Repair failing GuestService and GuestController tests in guest-service

ID: AI-0050
Mode: WRITE
RISK: MEDIUM

## WHY
AI-0049 resulted in VERIFY_FAIL due to errors in guest-service unit tests. The test suite needs repair so that compilation and execution pass deterministically.

## WHAT
Fix compilation issues, mock expectations, or assertion failures in GuestControllerTest and GuestServiceImplTest.

## SCOPE
- guest-service/src/test/java/com/hotelpms/guest/controller/GuestControllerTest.java
- guest-service/src/test/java/com/hotelpms/guest/service/impl/GuestServiceImplTest.java
- guest-service/src/main/java/com/hotelpms/guest/

## OUT OF SCOPE
- Modifying unrelated services or shared libraries
- Database migrations or Flyway scripts
- Gradle build file structural changes outside guest-service dependencies

## CONTRACT
- ./gradlew :guest-service:test executes successfully with zero failures.

## INVARIANTS
- Guest service domain logic and public API endpoints remain backwards-compatible
- No hardcoded credentials or untrusted state introduced in tests

## VERIFICATION
- ./gradlew :guest-service:test

## ROLLBACK
git checkout -- guest-service/

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- guest-service/src/
- guest-service/build.gradle.kts
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :guest-service:test
END_VERIFY_COMMANDS
