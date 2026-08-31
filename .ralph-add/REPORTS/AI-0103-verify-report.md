# AI-0103 Verification Result

- Result: PASS
- Hypervelocity lane: isolated verification worktree

## Verification log
```text
$ ./gradlew :api-gateway:test :auth-service:test :frontdesk-service:test :guest-service:test --no-daemon
exit=0
To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/9.3.1/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build 
> Task :api-gateway:compileJava
> Task :api-gateway:processResources
> Task :api-gateway:classes
> Task :api-gateway:compileTestJava
> Task :api-gateway:processTestResources NO-SOURCE
> Task :api-gateway:testClasses
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
> Task :api-gateway:test
> Task :common-web-lib:compileJava
> Task :internal-auth-lib:compileJava
> Task :api-gateway:jacocoTestReport
> Task :auth-service:compileJava
> Task :auth-service:processResources
> Task :auth-service:classes
> Task :api-gateway:jacocoTestCoverageVerification
> Task :internal-auth-lib:compileTestFixturesJava
> Task :auth-service:compileTestJava
> Task :auth-service:processTestResources NO-SOURCE
> Task :auth-service:testClasses
> Task :common-web-lib:processResources NO-SOURCE
> Task :common-web-lib:classes
> Task :common-web-lib:jar
> Task :internal-auth-lib:processResources NO-SOURCE
> Task :internal-auth-lib:classes
> Task :internal-auth-lib:jar
> Task :internal-auth-lib:processTestFixturesResources NO-SOURCE
> Task :internal-auth-lib:testFixturesClasses
> Task :internal-auth-lib:testFixturesJar
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
> Task :auth-service:test
> Task :auth-service:jacocoTestReport
> Task :pdf-template-engine:compileJava
> Task :auth-service:jacocoTestCoverageVerification
> Task :frontdesk-service:compileJava
> Task :frontdesk-service:processResources
> Task :frontdesk-service:classes
> Task :frontdesk-service:compileTestJava
> Task :frontdesk-service:processTestResources
> Task :frontdesk-service:testClasses
> Task :pdf-template-engine:processResources
> Task :pdf-template-engine:classes
> Task :pdf-template-engine:jar
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
> Task :frontdesk-service:test

2026-08-22T04:31:12.985-06:00  INFO 91110 --- [frontdesk-service] [ionShutdownHook] [                                                 ] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-08-22T04:31:13.349-06:00  INFO 91110 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-08-22T04:31:13.350-06:00  INFO 91110 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
2026-08-22T04:31:13.372-06:00  INFO 91110 --- [frontdesk-service] [ionShutdownHook] [                                                 ] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-08-22T04:31:13.372-06:00  INFO 91110 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Shutdown initiated...
2026-08-22T04:31:13.373-06:00  INFO 91110 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Shutdown completed.

> Task :frontdesk-service:jacocoTestReport
> Task :guest-service:compileJava
> Task :guest-service:processResources
> Task :guest-service:classes
> Task :frontdesk-service:jacocoTestCoverageVerification
> Task :guest-service:compileTestJava
> Task :guest-service:processTestResources
> Task :guest-service:testClasses
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
2026-08-22T04:31:40.986-06:00  INFO 91186 --- [guest-service] [ionShutdownHook] [                                                 ] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-08-22T04:31:41.289-06:00  INFO 91186 --- [guest-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-08-22T04:31:41.291-06:00  INFO 91186 --- [guest-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
> Task :guest-service:test
> Task :guest-service:jacocoTestReport
> Task :guest-service:jacocoTestCoverageVerification

BUILD SUCCESSFUL in 1m 42s
35 actionable tasks: 35 executed
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.3.1/userguide/configuration_cache_enabling.html


```
