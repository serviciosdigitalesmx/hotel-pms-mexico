# AI-0016 Verification Result

- Result: FAIL
- Source/workspace mutation: none detected
- Commands:
  - `./gradlew test --tests "*LocalIntentRouterTest*"`

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
$ ./gradlew test --tests "*LocalIntentRouterTest*"
exit=1
> Task :api-gateway:compileJava
> Task :api-gateway:processResources
> Task :api-gateway:classes
> Task :api-gateway:compileTestJava
> Task :api-gateway:processTestResources NO-SOURCE
> Task :api-gateway:testClasses
> Task :api-gateway:test FAILED

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':api-gateway:test'.
> No tests found for given includes: [*LocalIntentRouterTest*](--tests filter)

* Try:
> Run with --scan to get full insights from a Build Scan (powered by Develocity).

BUILD FAILED in 12s
4 actionable tasks: 4 executed


```
