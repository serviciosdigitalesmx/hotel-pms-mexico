# AI-0147 Verification Result

- Result: PASS
- Hypervelocity lane: isolated verification worktree

## Verification log
```text
$ ./gradlew :frontdesk-service:test --no-daemon
exit=0
To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/9.3.1/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build 
> Task :common-web-lib:compileJava
> Task :common-web-lib:processResources NO-SOURCE
> Task :common-web-lib:classes
> Task :common-web-lib:jar
> Task :internal-auth-lib:compileJava
> Task :pdf-template-engine:compileJava
> Task :frontdesk-service:compileJava
> Task :frontdesk-service:processResources
> Task :frontdesk-service:classes
> Task :internal-auth-lib:compileTestFixturesJava
> Task :frontdesk-service:compileTestJava
> Task :frontdesk-service:processTestResources
> Task :frontdesk-service:testClasses
> Task :internal-auth-lib:processResources NO-SOURCE
> Task :internal-auth-lib:classes
> Task :internal-auth-lib:jar
> Task :internal-auth-lib:processTestFixturesResources NO-SOURCE
> Task :internal-auth-lib:testFixturesClasses
> Task :internal-auth-lib:testFixturesJar
> Task :pdf-template-engine:processResources
> Task :pdf-template-engine:classes
> Task :pdf-template-engine:jar
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
> Task :frontdesk-service:test

2026-08-22T07:07:21.760-06:00  INFO 15936 --- [frontdesk-service] [ionShutdownHook] [                                                 ] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-08-22T07:07:22.140-06:00  INFO 15936 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-08-22T07:07:22.142-06:00  INFO 15936 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
2026-08-22T07:07:22.158-06:00  INFO 15936 --- [frontdesk-service] [ionShutdownHook] [                                                 ] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-08-22T07:07:22.158-06:00  INFO 15936 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Shutdown initiated...
2026-08-22T07:07:22.159-06:00  INFO 15936 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Shutdown completed.

> Task :frontdesk-service:jacocoTestReport
> Task :frontdesk-service:jacocoTestCoverageVerification

BUILD SUCCESSFUL in 2m 3s
16 actionable tasks: 16 executed
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.3.1/userguide/configuration_cache_enabling.html


```
