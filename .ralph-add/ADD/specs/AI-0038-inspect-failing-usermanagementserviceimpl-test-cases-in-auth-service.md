# A.SPEC AI-0038 — Inspect failing UserManagementServiceImpl test cases in auth-service

ID: AI-0038
Mode: READ_ONLY
RISK: LOW

## WHY
The `./gradlew test` run failed 5 unit tests in UserManagementServiceImplTest inside auth-service with AssertionFailedError and UnnecessaryStubbingException. Inspecting the test and implementation sources is required to diagnose the root cause before applying a fix.

## WHAT
Examine UserManagementServiceImpl.java, UserManagementServiceImplTest.java, and associated DTOs/mappers in auth-service.

## SCOPE
- auth-service/src/main/java/com/hotelpms/auth/service/UserManagementServiceImpl.java
- auth-service/src/test/java/com/hotelpms/auth/service/UserManagementServiceImplTest.java

## OUT OF SCOPE
- Modifying application code, configuration, or test files.

## CONTRACT
- Gather context on the strict Mockito stubbing behavior and mock assertions in UserManagementServiceImplTest to formulate a targeted code fix.

## INVARIANTS
- Workspace and source control remain untouched during read-only inspection.

## VERIFICATION
- Source and test implementations are retrieved for diagnostic review.

## ROLLBACK
No rollback required for read-only actions.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- auth-service
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
END_VERIFY_COMMANDS
