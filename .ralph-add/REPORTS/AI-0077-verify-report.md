# AI-0077 Verification Result

- Result: FAIL
- Source mutation: none detected
- Commands:
  - `./gradlew test`
  - `npm --prefix frontend test`

## Local build manifest
Build system: Gradle root
Wrapper: ./gradlew
Docker Compose: present
Top-level directories: .add, .argos, .backups, .github, api-gateway, auth-service, billing-service, common-web-lib, config, config-service, docker, docs, fb-service, frontdesk-service, frontend, gradle, guest-service, internal-auth-lib, notification-service, pdf-template-engine, scripts

## Verification log tail
```text
$ ./gradlew test
exit=0
> Task :api-gateway:compileJava UP-TO-DATE
> Task :api-gateway:processResources UP-TO-DATE
> Task :api-gateway:classes UP-TO-DATE
> Task :api-gateway:compileTestJava UP-TO-DATE
> Task :api-gateway:processTestResources NO-SOURCE
> Task :api-gateway:testClasses UP-TO-DATE
> Task :api-gateway:test UP-TO-DATE
> Task :api-gateway:jacocoTestReport UP-TO-DATE
> Task :api-gateway:jacocoTestCoverageVerification UP-TO-DATE
> Task :common-web-lib:compileJava UP-TO-DATE
> Task :internal-auth-lib:compileJava UP-TO-DATE
> Task :auth-service:compileJava UP-TO-DATE
> Task :auth-service:processResources UP-TO-DATE
> Task :auth-service:classes UP-TO-DATE
> Task :internal-auth-lib:compileTestFixturesJava UP-TO-DATE
> Task :auth-service:compileTestJava UP-TO-DATE
> Task :auth-service:processTestResources NO-SOURCE
> Task :auth-service:testClasses UP-TO-DATE
> Task :common-web-lib:processResources NO-SOURCE
> Task :common-web-lib:classes UP-TO-DATE
> Task :common-web-lib:jar UP-TO-DATE
> Task :internal-auth-lib:processResources NO-SOURCE
> Task :internal-auth-lib:classes UP-TO-DATE
> Task :internal-auth-lib:jar UP-TO-DATE
> Task :internal-auth-lib:processTestFixturesResources NO-SOURCE
> Task :internal-auth-lib:testFixturesClasses UP-TO-DATE
> Task :internal-auth-lib:testFixturesJar UP-TO-DATE
> Task :auth-service:test UP-TO-DATE
> Task :auth-service:jacocoTestReport UP-TO-DATE
> Task :auth-service:jacocoTestCoverageVerification UP-TO-DATE
> Task :pdf-template-engine:compileJava UP-TO-DATE
> Task :billing-service:compileJava UP-TO-DATE
> Task :billing-service:processResources UP-TO-DATE
> Task :billing-service:classes UP-TO-DATE
> Task :billing-service:compileTestJava UP-TO-DATE
> Task :billing-service:processTestResources UP-TO-DATE
> Task :billing-service:testClasses UP-TO-DATE
> Task :pdf-template-engine:processResources UP-TO-DATE
> Task :pdf-template-engine:classes UP-TO-DATE
> Task :pdf-template-engine:jar UP-TO-DATE
> Task :billing-service:test UP-TO-DATE
> Task :billing-service:jacocoTestReport UP-TO-DATE
> Task :billing-service:jacocoTestCoverageVerification UP-TO-DATE
> Task :common-web-lib:compileTestJava UP-TO-DATE
> Task :common-web-lib:processTestResources NO-SOURCE
> Task :common-web-lib:testClasses UP-TO-DATE
> Task :common-web-lib:test UP-TO-DATE
> Task :common-web-lib:jacocoTestReport UP-TO-DATE
> Task :common-web-lib:jacocoTestCoverageVerification UP-TO-DATE
> Task :config-service:compileJava UP-TO-DATE
> Task :config-service:processResources UP-TO-DATE
> Task :config-service:classes UP-TO-DATE
> Task :config-service:compileTestJava UP-TO-DATE
> Task :config-service:processTestResources NO-SOURCE
> Task :config-service:testClasses UP-TO-DATE
> Task :config-service:test UP-TO-DATE
> Task :config-service:jacocoTestReport UP-TO-DATE
> Task :config-service:jacocoTestCoverageVerification UP-TO-DATE
> Task :fb-service:compileJava UP-TO-DATE
> Task :fb-service:processResources UP-TO-DATE
> Task :fb-service:classes UP-TO-DATE
> Task :fb-service:compileTestJava UP-TO-DATE
> Task :fb-service:processTestResources UP-TO-DATE
> Task :fb-service:testClasses UP-TO-DATE
> Task :fb-service:test UP-TO-DATE
> Task :fb-service:jacocoTestReport UP-TO-DATE
> Task :fb-service:jacocoTestCoverageVerification UP-TO-DATE
> Task :frontdesk-service:compileJava UP-TO-DATE
> Task :frontdesk-service:processResources UP-TO-DATE
> Task :frontdesk-service:classes UP-TO-DATE
> Task :frontdesk-service:compileTestJava UP-TO-DATE
> Task :frontdesk-service:processTestResources UP-TO-DATE
> Task :frontdesk-service:testClasses UP-TO-DATE
> Task :frontdesk-service:test UP-TO-DATE
> Task :frontdesk-service:jacocoTestReport UP-TO-DATE
> Task :frontdesk-service:jacocoTestCoverageVerification UP-TO-DATE
> Task :guest-service:compileJava UP-TO-DATE
> Task :guest-service:processResources UP-TO-DATE
> Task :guest-service:classes UP-TO-DATE
> Task :guest-service:compileTestJava UP-TO-DATE
> Task :guest-service:processTestResources UP-TO-DATE
> Task :guest-service:testClasses UP-TO-DATE
> Task :guest-service:test UP-TO-DATE
> Task :guest-service:jacocoTestReport UP-TO-DATE
> Task :guest-service:jacocoTestCoverageVerification UP-TO-DATE
> Task :internal-auth-lib:compileTestJava UP-TO-DATE
> Task :internal-auth-lib:processTestResources NO-SOURCE
> Task :internal-auth-lib:testClasses UP-TO-DATE
> Task :internal-auth-lib:test UP-TO-DATE
> Task :internal-auth-lib:jacocoTestReport UP-TO-DATE
> Task :internal-auth-lib:jacocoTestCoverageVerification UP-TO-DATE
> Task :notification-service:compileJava UP-TO-DATE
> Task :notification-service:processResources UP-TO-DATE
> Task :notification-service:classes UP-TO-DATE
> Task :notification-service:compileTestJava UP-TO-DATE
> Task :notification-service:processTestResources UP-TO-DATE
> Task :notification-service:testClasses UP-TO-DATE
> Task :notification-service:test UP-TO-DATE
> Task :notification-service:jacocoTestReport UP-TO-DATE
> Task :notification-service:jacocoTestCoverageVerification UP-TO-DATE
> Task :pdf-template-engine:compileTestJava UP-TO-DATE
> Task :pdf-template-engine:processTestResources UP-TO-DATE
> Task :pdf-template-engine:testClasses UP-TO-DATE
> Task :pdf-template-engine:test UP-TO-DATE
> Task :pdf-template-engine:jacocoTestReport UP-TO-DATE
> Task :pdf-template-engine:jacocoTestCoverageVerification UP-TO-DATE

BUILD SUCCESSFUL in 6s
75 actionable tasks: 75 up-to-date
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.3.1/userguide/configuration_cache_enabling.html


$ npm --prefix frontend test
BLOCKED_BY_SUPERVISOR

```
