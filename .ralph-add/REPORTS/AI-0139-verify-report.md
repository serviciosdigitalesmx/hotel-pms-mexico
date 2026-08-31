# AI-0139 Verification Result

- Result: PASS
- Hypervelocity lane: isolated verification worktree

## Verification log
```text
$ ./gradlew :auth-service:test :guest-service:test --no-daemon
exit=0
To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/9.3.1/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build 
> Task :common-web-lib:compileJava
> Task :internal-auth-lib:compileJava
> Task :auth-service:compileJava
> Task :auth-service:processResources
> Task :auth-service:classes
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
> Task :guest-service:compileJava
> Task :guest-service:processResources
> Task :guest-service:classes
> Task :auth-service:jacocoTestCoverageVerification
> Task :guest-service:compileTestJava
> Task :guest-service:processTestResources
> Task :guest-service:testClasses
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
> Task :guest-service:test

2026-08-22T06:23:55.945-06:00  INFO 11042 --- [guest-service] [ionShutdownHook] [                                                 ] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-08-22T06:23:56.214-06:00  INFO 11042 --- [guest-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-08-22T06:23:56.222-06:00  INFO 11042 --- [guest-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.

> Task :guest-service:jacocoTestReport
> Task :guest-service:jacocoTestCoverageVerification

BUILD SUCCESSFUL in 1m 32s
19 actionable tasks: 19 executed
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.3.1/userguide/configuration_cache_enabling.html


```
