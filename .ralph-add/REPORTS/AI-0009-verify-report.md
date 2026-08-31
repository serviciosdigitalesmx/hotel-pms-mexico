# AI-0009 Verification Result

- Result: FAIL
- Source/workspace mutation: none detected
- Commands:
  - `./gradlew :frontdesk-service:test --no-daemon`

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
$ ./gradlew :frontdesk-service:test --no-daemon
exit=1
To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/9.3.1/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build 
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

LocalIntentRouterTest > cancelClearsAnActiveFlow() FAILED
    java.lang.NullPointerException at LocalIntentRouterTest.java:208

LocalIntentRouterTest > isolatedDateAsksForItsMeaning() FAILED
    java.lang.NullPointerException at LocalIntentRouterTest.java:186

LocalIntentRouterTest > createsMissingGuestOnlyAfterConfirmationAndResumesCheckIn() FAILED
    java.lang.NullPointerException at LocalIntentRouterTest.java:171

LocalIntentRouterTest > guestServiceFailureIsNotReportedAsNoMatches() FAILED
    java.lang.NullPointerException at LocalIntentRouterTest.java:199

LocalIntentRouterTest > preservesContextWhenRequestedRoomTypeIsUnavailable() FAILED
    java.lang.NullPointerException at LocalIntentRouterTest.java:147

LocalIntentRouterTest > preservesKnownSlotsAndAsksOnlyForDates() FAILED
    java.lang.NullPointerException at LocalIntentRouterTest.java:100

LocalIntentRouterTest > asksForRoomSelectionWhenSeveralRealRoomsMatch() FAILED
    java.lang.NullPointerException at LocalIntentRouterTest.java:133

LocalIntentRouterTest > repeatedConfirmationDoesNotDuplicateCheckIn() FAILED
    java.lang.NullPointerException at LocalIntentRouterTest.java:222

LocalIntentRouterTest > unknownMessageUsesGroqFallbackUnchanged() FAILED
    org.mockito.exceptions.misusing.UnnecessaryStubbingException at MockitoExtension.java:197

LocalIntentRouterTest > doesNotSelectFirstGuestWhenThereAreMultipleMatches() FAILED
    java.lang.NullPointerException at LocalIntentRouterTest.java:116

2026-08-20T05:20:55.973-06:00  INFO 17878 --- [frontdesk-service] [ionShutdownHook] [                                                 ] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-08-20T05:20:56.420-06:00  INFO 17878 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-08-20T05:20:56.425-06:00  INFO 17878 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
2026-08-20T05:20:56.454-06:00  INFO 17878 --- [frontdesk-service] [ionShutdownHook] [                                                 ] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-08-20T05:20:56.454-06:00  INFO 17878 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Shutdown initiated...
2026-08-20T05:20:56.455-06:00  INFO 17878 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Shutdown completed.

> Task :frontdesk-service:test

405 tests completed, 10 failed

> Task :frontdesk-service:test FAILED

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':frontdesk-service:test'.
> There were failing tests. See the report at: file:///Users/usuario/Desktop/HOTEL-PMS/frontdesk-service/build/reports/tests/test/index.html

* Try:
> Run with --scan to get full insights from a Build Scan (powered by Develocity).

BUILD FAILED in 1m 9s
14 actionable tasks: 1 executed, 13 up-to-date


```
