# A.SPEC AI-0049 — Fix GuestRequest and GuestResponse record constructor signatures in guest-service tests

ID: AI-0049
Mode: WRITE
RISK: MEDIUM

## WHY
Verification of AI-0048 failed because GuestControllerTest and GuestServiceImplTest invoke outdated constructors for GuestRequest and GuestResponse records.

## WHAT
Update GuestRequest and GuestResponse constructor invocations in GuestControllerTest.java and GuestServiceImplTest.java to supply all required parameters matching the current record definitions.

## SCOPE
- guest-service/src/test/java/com/hotelpms/guest/controller/GuestControllerTest.java
- guest-service/src/test/java/com/hotelpms/guest/service/impl/GuestServiceImplTest.java

## OUT OF SCOPE
- guest-service record source definitions
- production application code
- other service modules

## CONTRACT
- All GuestRequest and GuestResponse record instantiations in GuestControllerTest and GuestServiceImplTest must match the exact field order and parameter count required by their Java records.
- No production code changes are permitted during this step.

## INVARIANTS
- All existing test assertions and behavior expectations in guest-service must remain intact.

## VERIFICATION
- ./gradlew :guest-service:test --no-daemon

## ROLLBACK
Git revert modifications to GuestControllerTest.java and GuestServiceImplTest.java.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- guest-service/src/test/java/com/hotelpms/guest/controller/GuestControllerTest.java
- guest-service/src/test/java/com/hotelpms/guest/service/impl/GuestServiceImplTest.java
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :guest-service:test --no-daemon
END_VERIFY_COMMANDS
