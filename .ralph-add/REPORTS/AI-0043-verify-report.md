# AI-0043 Verification Result

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

> Task :frontdesk-service:compileTestJava FAILED
/Users/usuario/Desktop/HOTEL-PMS/frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/ResilientIntentFallbackHandlerTest.java:61: error: constructor RequestNotPermitted in class RequestNotPermitted cannot be applied to given types;
                () -> { throw new RequestNotPermitted(null); },
                              ^
  required: String,boolean
  found:    <null>
  reason: actual and formal argument lists differ in length
1 error

[Incubating] Problems report is available at: file:///Users/usuario/Desktop/HOTEL-PMS/build/reports/problems/problems-report.html

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':frontdesk-service:compileTestJava'.
> Compilation failed; see the compiler output below.
  /Users/usuario/Desktop/HOTEL-PMS/frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/ResilientIntentFallbackHandlerTest.java:61: error: constructor RequestNotPermitted in class RequestNotPermitted cannot be applied to given types;
                  () -> { throw new RequestNotPermitted(null); },
                                ^
    required: String,boolean
    found:    <null>
    reason: actual and formal argument lists differ in length
  1 error

* Try:
> Check your code and dependencies to fix the compilation error(s)
> Run with --scan to get full insights from a Build Scan (powered by Develocity).

BUILD FAILED in 1s
8 actionable tasks: 1 executed, 7 up-to-date


```
