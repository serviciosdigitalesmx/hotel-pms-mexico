# AI-0005 Verification Result

- Result: FAIL
- Source/workspace mutation: none detected
- Commands:
  - `./mvnw -pl frontdesk-service -DskipTests compile`
  - `./mvnw -pl frontdesk-service test`

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
$ ./mvnw -pl frontdesk-service -DskipTests compile
exit=127
zsh:1: no such file or directory: ./mvnw


$ ./mvnw -pl frontdesk-service test
exit=127
zsh:1: no such file or directory: ./mvnw


```
