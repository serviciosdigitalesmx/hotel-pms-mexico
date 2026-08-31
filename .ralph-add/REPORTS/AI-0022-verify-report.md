# AI-0022 Verification Result

- Result: FAIL
- Source/workspace mutation: none detected
- Commands:
  - `./gradlew :frontdesk-service:test`

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
$ ./gradlew :frontdesk-service:test
exit=1
> Task :common-web-lib:compileJava UP-TO-DATE
> Task :common-web-lib:processResources NO-SOURCE
> Task :common-web-lib:classes UP-TO-DATE
> Task :common-web-lib:jar UP-TO-DATE
> Task :internal-auth-lib:compileJava UP-TO-DATE
> Task :pdf-template-engine:compileJava UP-TO-DATE
> Task :frontdesk-service:compileJava UP-TO-DATE
> Task :frontdesk-service:processResources UP-TO-DATE
> Task :frontdesk-service:classes UP-TO-DATE
> Task :internal-auth-lib:compileTestFixturesJava UP-TO-DATE
> Task :frontdesk-service:compileTestJava UP-TO-DATE
> Task :frontdesk-service:processTestResources UP-TO-DATE
> Task :frontdesk-service:testClasses UP-TO-DATE
> Task :internal-auth-lib:processResources NO-SOURCE
> Task :internal-auth-lib:classes UP-TO-DATE
> Task :internal-auth-lib:jar UP-TO-DATE
> Task :internal-auth-lib:processTestFixturesResources NO-SOURCE
> Task :internal-auth-lib:testFixturesClasses UP-TO-DATE
> Task :internal-auth-lib:testFixturesJar UP-TO-DATE
> Task :pdf-template-engine:processResources UP-TO-DATE
> Task :pdf-template-engine:classes UP-TO-DATE
> Task :pdf-template-engine:jar UP-TO-DATE
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended

> Task :frontdesk-service:test

LocalIntentRouterTest > repeatedConfirmationDoesNotDuplicateCheckIn() FAILED
    org.mockito.exceptions.verification.TooManyActualInvocations at LocalIntentRouterTest.java:232

LocalIntentRouterTest > unknownMessageUsesGroqFallbackUnchanged() FAILED
    org.mockito.exceptions.misusing.UnnecessaryStubbingException at MockitoExtension.java:197

2026-08-20T18:07:02.697-06:00  INFO 27666 --- [frontdesk-service] [ionShutdownHook] [                                                 ] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-08-20T18:07:03.025-06:00  INFO 27666 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-08-20T18:07:03.027-06:00  INFO 27666 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
2026-08-20T18:07:03.044-06:00  INFO 27666 --- [frontdesk-service] [ionShutdownHook] [                                                 ] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-08-20T18:07:03.044-06:00  INFO 27666 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Shutdown initiated...
2026-08-20T18:07:03.045-06:00  INFO 27666 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Shutdown completed.

> Task :frontdesk-service:test

408 tests completed, 2 failed

> Task :frontdesk-service:test FAILED

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':frontdesk-service:test'.
> There were failing tests. See the report at: file:///Users/usuario/Desktop/HOTEL-PMS/frontdesk-service/build/reports/tests/test/index.html

* Try:
> Run with --scan to get full insights from a Build Scan (powered by Develocity).

BUILD FAILED in 39s
14 actionable tasks: 1 executed, 13 up-to-date


```
