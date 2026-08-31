# AI-0037 Verification Result

- Result: FAIL
- Source/workspace mutation: none detected
- Commands:
  - `./gradlew test`

## Local build manifest
- api-gateway/build.gradle.kts
- auth-service/build.gradle.kts
- billing-service/build.gradle.kts
- billing-service/gradlew
- billing-service/settings.gradle.kts
- build.gradle.kts
- common-web-lib/build.gradle.kts
- config-service/build.gradle.kts
- fb-service/build.gradle.kts
- fb-service/settings.gradle.kts
- frontdesk-service/build.gradle.kts
- frontend/package-lock.json
- frontend/package.json
- gradlew
- guest-service/build.gradle.kts
- guest-service/gradlew
- guest-service/settings.gradle.kts
- internal-auth-lib/build.gradle.kts
- notification-service/build.gradle.kts
- pdf-template-engine/build.gradle.kts
- settings.gradle.kts

## Verification log tail
```text
$ ./gradlew test
exit=1
> Task :api-gateway:compileJava UP-TO-DATE
> Task :api-gateway:processResources UP-TO-DATE
> Task :api-gateway:classes UP-TO-DATE
> Task :api-gateway:compileTestJava UP-TO-DATE
> Task :api-gateway:processTestResources NO-SOURCE
> Task :api-gateway:testClasses UP-TO-DATE
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
> Task :api-gateway:test
> Task :common-web-lib:compileJava UP-TO-DATE
> Task :internal-auth-lib:compileJava UP-TO-DATE
> Task :api-gateway:jacocoTestReport
> Task :auth-service:compileJava
> Task :auth-service:processResources
> Task :auth-service:classes
> Task :internal-auth-lib:compileTestFixturesJava UP-TO-DATE
> Task :api-gateway:jacocoTestCoverageVerification
> Task :auth-service:compileTestJava
> Task :auth-service:processTestResources NO-SOURCE
> Task :auth-service:testClasses
> Task :common-web-lib:processResources NO-SOURCE
> Task :common-web-lib:classes UP-TO-DATE
> Task :common-web-lib:jar UP-TO-DATE
> Task :internal-auth-lib:processResources NO-SOURCE
> Task :internal-auth-lib:classes UP-TO-DATE
> Task :internal-auth-lib:jar UP-TO-DATE
> Task :internal-auth-lib:processTestFixturesResources NO-SOURCE
> Task :internal-auth-lib:testFixturesClasses UP-TO-DATE
> Task :internal-auth-lib:testFixturesJar UP-TO-DATE
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended

> Task :auth-service:test

UserManagementServiceImplTest > listUsersShouldReturnMappedResponse() FAILED
    org.opentest4j.AssertionFailedError at UserManagementServiceImplTest.java:91

UserManagementServiceImplTest > createUserShouldThrowWhenEmailAlreadyExists() FAILED
    org.opentest4j.AssertionFailedError at UserManagementServiceImplTest.java:139

UserManagementServiceImplTest > createUserShouldThrowWhenUsernameAlreadyExists() FAILED
    org.opentest4j.AssertionFailedError at UserManagementServiceImplTest.java:128

UserManagementServiceImplTest > createUserShouldPersistAndReturnMustChangePasswordTrue() FAILED
    org.mockito.exceptions.misusing.UnnecessaryStubbingException at MockitoExtension.java:197

UserManagementServiceImplTest > listUsersShouldReturnEmptyListWhenNoUsers() FAILED
    org.mockito.exceptions.misusing.UnnecessaryStubbingException at MockitoExtension.java:197

80 tests completed, 5 failed

> Task :auth-service:test FAILED

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':auth-service:test'.
> There were failing tests. See the report at: file:///Users/usuario/Desktop/HOTEL-PMS/auth-service/build/reports/tests/test/index.html

* Try:
> Run with --scan to get full insights from a Build Scan (powered by Develocity).

BUILD FAILED in 30s
16 actionable tasks: 7 executed, 9 up-to-date


```
