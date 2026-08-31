# AI-0048 Verification Result

- Result: FAIL
- Source/workspace mutation: none detected
- Commands:
  - `./gradlew test --no-daemon`

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
$ ./gradlew test --no-daemon
exit=1
riDataSource       : HikariPool-2 - Shutdown completed.

> Task :frontdesk-service:jacocoTestReport
> Task :guest-service:compileJava
> Task :guest-service:processResources
> Task :guest-service:classes
> Task :frontdesk-service:jacocoTestCoverageVerification

> Task :guest-service:compileTestJava FAILED
/Users/usuario/Desktop/HOTEL-PMS/guest-service/src/test/java/com/hotelpms/guest/controller/GuestControllerTest.java:90: error: constructor GuestResponse in record GuestResponse cannot be applied to given types;
        guestResponse = new GuestResponse(
                        ^
  required: UUID,String,String,String,String,String,String,String,LocalDate,String,String,String,String,String,String,String,String,String,String,String,String,String,String,List<IdentityDocumentResponseDTO>,LocalDate,LocalDateTime,LocalDateTime
  found:    UUID,String,String,String,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,List<Object>,<null>,<null>,<null>
  reason: actual and formal argument lists differ in length
/Users/usuario/Desktop/HOTEL-PMS/guest-service/src/test/java/com/hotelpms/guest/controller/GuestControllerTest.java:104: error: constructor GuestRequest in record GuestRequest cannot be applied to given types;
        final GuestRequest request = new GuestRequest(
                                     ^
  required: String,String,String,String,String,String,String,LocalDate,String,String,String,String,String,String,String,String,String,String,String,String,String,String
  found:    String,String,String,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>
  reason: actual and formal argument lists differ in length
/Users/usuario/Desktop/HOTEL-PMS/guest-service/src/test/java/com/hotelpms/guest/controller/GuestControllerTest.java:153: error: constructor GuestRequest in record GuestRequest cannot be applied to given types;
        final GuestRequest request = new GuestRequest(
                                     ^
  required: String,String,String,String,String,String,String,LocalDate,String,String,String,String,String,String,String,String,String,String,String,String,String,String
  found:    String,String,String,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>
  reason: actual and formal argument lists differ in length
/Users/usuario/Desktop/HOTEL-PMS/guest-service/src/test/java/com/hotelpms/guest/service/impl/GuestServiceImplTest.java:127: error: constructor GuestRequest in record GuestRequest cannot be applied to given types;
        guestRequest = new GuestRequest(
                       ^
  required: String,String,String,String,String,String,String,LocalDate,String,String,String,String,String,String,String,String,String,String,String,String,String,String
  found:    String,String,String,String,String,String,String,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>
  reason: actual and formal argument lists differ in length
/Users/usuario/Desktop/HOTEL-PMS/guest-service/src/test/java/com/hotelpms/guest/service/impl/GuestServiceImplTest.java:139: error: constructor GuestResponse in record GuestResponse cannot be applied to given types;
        guestResponse = new GuestResponse(
                        ^
  required: UUID,String,String,String,String,String,String,String,LocalDate,String,String,String,String,String,String,String,String,String,String,String,String,String,String,List<IdentityDocumentResponseDTO>,LocalDate,LocalDateTime,LocalDateTime
  found:    UUID,String,String,String,String,String,String,String,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,List<Object>,<null>,LocalDateTime,LocalDateTime
  reason: actual and formal argument lists differ in length
/Users/usuario/Desktop/HOTEL-PMS/guest-service/src/test/java/com/hotelpms/guest/service/impl/GuestServiceImplTest.java:187: error: constructor GuestRequest in record GuestRequest cannot be applied to given types;
        final GuestRequest requestWithAddress = new GuestRequest(
                                                ^
  required: String,String,String,String,String,String,String,LocalDate,String,String,String,String,String,String,String,String,String,String,String,String,String,String
  found:    String,String,String,String,String,String,String,<null>,<null>,<null>,<null>,<null>,<null>,String,String,String
  reason: actual and formal argument lists differ in length
/Users/usuario/Desktop/HOTEL-PMS/guest-service/src/test/java/com/hotelpms/guest/service/impl/GuestServiceImplTest.java:209: error: constructor GuestRequest in record GuestRequest cannot be applied to given types;
        final GuestRequest requestWithBadComune = new GuestRequest(
                                                  ^
  required: String,String,String,String,String,String,String,LocalDate,String,String,String,String,String,String,String,String,String,String,String,String,String,String
  found:    String,String,String,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,String,String
  reason: actual and formal argument lists differ in length
/Users/usuario/Desktop/HOTEL-PMS/guest-service/src/test/java/com/hotelpms/guest/service/impl/GuestServiceImplTest.java:223: error: constructor GuestRequest in record GuestRequest cannot be applied to given types;
        final GuestRequest requestWithoutProvincia = new GuestRequest(
                                                     ^
  required: String,String,String,String,String,String,String,LocalDate,String,String,String,String,String,String,String,String,String,String,String,String,String,String
  found:    String,String,String,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,String,<null>
  reason: actual and formal argument lists differ in length
8 errors

[Incubating] Problems report is available at: file:///Users/usuario/Desktop/HOTEL-PMS/build/reports/problems/problems-report.html

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':guest-service:compileTestJava'.
> Compilation failed; see the compiler output below.
  /Users/usuario/Desktop/HOTEL-PMS/guest-service/src/test/java/com/hotelpms/guest/controller/GuestControllerTest.java:90: error: constructor GuestResponse in record GuestResponse cannot be applied to given types;
          guestResponse = new GuestResponse(
                          ^
    required: UUID,String,String,String,String,String,String,String,LocalDate,String,String,String,String,String,String,String,String,String,String,String,String,String,String,List<IdentityDocumentResponseDTO>,LocalDate,LocalDateTime,LocalDateTime
    found:    UUID,String,String,String,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,List<Object>,<null>,<null>,<null>
    reason: actual and formal argument lists differ in length
  /Users/usuario/Desktop/HOTEL-PMS/guest-service/src/test/java/com/hotelpms/guest/controller/GuestControllerTest.java:104: error: constructor GuestRequest in record GuestRequest cannot be applied to given types;
          final GuestRequest request = new GuestRequest(
                                       ^
    required: String,String,String,String,String,String,String,LocalDate,String,String,String,String,String,String,String,String,String,String,String,String,String,String
    found:    String,String,String,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>
    reason: actual and formal argument lists differ in length
  /Users/usuario/Desktop/HOTEL-PMS/guest-service/src/test/java/com/hotelpms/guest/controller/GuestControllerTest.java:153: error: constructor GuestRequest in record GuestRequest cannot be applied to given types;
          final GuestRequest request = new GuestRequest(
                                       ^
    required: String,String,String,String,String,String,String,LocalDate,String,String,String,String,String,String,String,String,String,String,String,String,String,String
    found:    String,String,String,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>
    reason: actual and formal argument lists differ in length
  /Users/usuario/Desktop/HOTEL-PMS/guest-service/src/test/java/com/hotelpms/guest/service/impl/GuestServiceImplTest.java:127: error: constructor GuestRequest in record GuestRequest cannot be applied to given types;
          guestRequest = new GuestRequest(
                         ^
    required: String,String,String,String,String,String,String,LocalDate,String,String,String,String,String,String,String,String,String,String,String,String,String,String
    found:    String,String,String,String,String,String,String,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>
    reason: actual and formal argument lists differ in length
  /Users/usuario/Desktop/HOTEL-PMS/guest-service/src/test/java/com/hotelpms/guest/service/impl/GuestServiceImplTest.java:139: error: constructor GuestResponse in record GuestResponse cannot be applied to given types;
          guestResponse = new GuestResponse(
                          ^
    required: UUID,String,String,String,String,String,String,String,LocalDate,String,String,String,String,String,String,String,String,String,String,String,String,String,String,List<IdentityDocumentResponseDTO>,LocalDate,LocalDateTime,LocalDateTime
    found:    UUID,String,String,String,String,String,String,String,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,List<Object>,<null>,LocalDateTime,LocalDateTime
    reason: actual and formal argument lists differ in length
  /Users/usuario/Desktop/HOTEL-PMS/guest-service/src/test/java/com/hotelpms/guest/service/impl/GuestServiceImplTest.java:187: error: constructor GuestRequest in record GuestRequest cannot be applied to given types;
          final GuestRequest requestWithAddress = new GuestRequest(
                                                  ^
    required: String,String,String,String,String,String,String,LocalDate,String,String,String,String,String,String,String,String,String,String,String,String,String,String
    found:    String,String,String,String,String,String,String,<null>,<null>,<null>,<null>,<null>,<null>,String,String,String
    reason: actual and formal argument lists differ in length
  /Users/usuario/Desktop/HOTEL-PMS/guest-service/src/test/java/com/hotelpms/guest/service/impl/GuestServiceImplTest.java:209: error: constructor GuestRequest in record GuestRequest cannot be applied to given types;
          final GuestRequest requestWithBadComune = new GuestRequest(
                                                    ^
    required: String,String,String,String,String,String,String,LocalDate,String,String,String,String,String,String,String,String,String,String,String,String,String,String
    found:    String,String,String,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,String,String
    reason: actual and formal argument lists differ in length
  /Users/usuario/Desktop/HOTEL-PMS/guest-service/src/test/java/com/hotelpms/guest/service/impl/GuestServiceImplTest.java:223: error: constructor GuestRequest in record GuestRequest cannot be applied to given types;
          final GuestRequest requestWithoutProvincia = new GuestRequest(
                                                       ^
    required: String,String,String,String,String,String,String,LocalDate,String,String,String,String,String,String,String,String,String,String,String,String,String,String
    found:    String,String,String,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,<null>,String,<null>
    reason: actual and formal argument lists differ in length
  8 errors

* Try:
> Check your code and dependencies to fix the compilation error(s)
> Run with --scan to get full insights from a Build Scan (powered by Develocity).

BUILD FAILED in 2m 8s
55 actionable tasks: 31 executed, 24 up-to-date


```
