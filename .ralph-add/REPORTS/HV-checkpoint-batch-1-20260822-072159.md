# Hypervelocity checkpoint batch-1

- Result: FAIL
- Workspace: isolated worktree

## Commands
```text
$ ./gradlew test --no-daemon
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
> Task :billing-service:compileJava
> Task :billing-service:processResources
> Task :billing-service:classes
> Task :billing-service:compileTestJava
> Task :billing-service:processTestResources
> Task :billing-service:testClasses
> Task :pdf-template-engine:processResources
> Task :pdf-template-engine:classes
> Task :pdf-template-engine:jar
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
07:22:55.762 [SpringApplicationShutdownHook] INFO  [] o.s.o.j.LocalContainerEntityManagerFactoryBean - Closing JPA EntityManagerFactory for persistence unit 'default'
07:22:56.033 [SpringApplicationShutdownHook] INFO  [] com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Shutdown initiated...
07:22:56.034 [SpringApplicationShutdownHook] INFO  [] com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Shutdown completed.
> Task :billing-service:test

> Task :common-web-lib:compileTestJava
[ant:jacocoReport] Note: /Users/usuario/.ralph-hotel/hypervelocity-worktrees/checkpoint-batch-1-uluow81o/common-web-lib/src/test/java/com/hotelpms/commonweb/exception/ProblemDetailAdviceTest.java uses or overrides a deprecated API.
[ant:jacocoReport] Note: Recompile with -Xlint:deprecation for details.

> Task :common-web-lib:processTestResources NO-SOURCE
> Task :common-web-lib:testClasses
> Task :billing-service:jacocoTestReport
> Task :common-web-lib:test
> Task :common-web-lib:jacocoTestReport
> Task :billing-service:jacocoTestCoverageVerification
> Task :config-service:compileJava
> Task :config-service:processResources
> Task :config-service:classes
> Task :common-web-lib:jacocoTestCoverageVerification
> Task :config-service:compileTestJava
> Task :config-service:processTestResources NO-SOURCE
> Task :config-service:testClasses
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
2026-08-22T07:23:03.539-06:00  INFO 17302 --- [config-service] [ionShutdownHook] o.s.b.w.e.tomcat.GracefulShutdown        : Commencing graceful shutdown. Waiting for active requests to complete
2026-08-22T07:23:03.541-06:00  INFO 17302 --- [config-service] [tomcat-shutdown] o.s.b.w.e.tomcat.GracefulShutdown        : Graceful shutdown complete
2026-08-22T07:23:03.681-06:00  INFO 17302 --- [config-service] [ionShutdownHook] o.s.b.w.e.tomcat.GracefulShutdown        : Commencing graceful shutdown. Waiting for active requests to complete
2026-08-22T07:23:03.682-06:00  INFO 17302 --- [config-service] [tomcat-shutdown] o.s.b.w.e.tomcat.GracefulShutdown        : Graceful shutdown complete
> Task :config-service:test
> Task :config-service:jacocoTestReport
> Task :fb-service:compileJava
> Task :fb-service:processResources
> Task :fb-service:classes
> Task :config-service:jacocoTestCoverageVerification
> Task :fb-service:compileTestJava
> Task :fb-service:processTestResources
> Task :fb-service:testClasses
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
> Task :fb-service:test
> Task :fb-service:jacocoTestReport
> Task :frontdesk-service:compileJava
> Task :frontdesk-service:processResources
> Task :frontdesk-service:classes
> Task :fb-service:jacocoTestCoverageVerification
> Task :frontdesk-service:compileTestJava
> Task :frontdesk-service:processTestResources
> Task :frontdesk-service:testClasses
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
> Task :frontdesk-service:test

2026-08-22T07:23:54.906-06:00  INFO 17343 --- [frontdesk-service] [ionShutdownHook] [                                                 ] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-08-22T07:23:55.231-06:00  INFO 17343 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-08-22T07:23:55.233-06:00  INFO 17343 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
2026-08-22T07:23:55.248-06:00  INFO 17343 --- [frontdesk-service] [ionShutdownHook] [                                                 ] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-08-22T07:23:55.249-06:00  INFO 17343 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Shutdown initiated...
2026-08-22T07:23:55.250-06:00  INFO 17343 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Shutdown completed.

> Task :frontdesk-service:jacocoTestReport
> Task :guest-service:compileJava
> Task :guest-service:processResources
> Task :guest-service:classes
> Task :frontdesk-service:jacocoTestCoverageVerification
> Task :guest-service:compileTestJava
> Task :guest-service:processTestResources
> Task :guest-service:testClasses
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
2026-08-22T07:24:12.426-06:00  INFO 17410 --- [guest-service] [ionShutdownHook] [                                                 ] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-08-22T07:24:12.675-06:00  INFO 17410 --- [guest-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-08-22T07:24:12.677-06:00  INFO 17410 --- [guest-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
> Task :guest-service:test
> Task :internal-auth-lib:compileTestJava
> Task :internal-auth-lib:processTestResources NO-SOURCE
> Task :internal-auth-lib:testClasses
> Task :guest-service:jacocoTestReport
> Task :internal-auth-lib:test
> Task :guest-service:jacocoTestCoverageVerification
> Task :internal-auth-lib:jacocoTestReport
> Task :notification-service:compileJava
> Task :notification-service:processResources
> Task :notification-service:classes
> Task :internal-auth-lib:jacocoTestCoverageVerification
> Task :notification-service:compileTestJava
> Task :notification-service:processTestResources
> Task :notification-service:testClasses
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
> Task :notification-service:test
> Task :pdf-template-engine:compileTestJava
> Task :pdf-template-engine:processTestResources
> Task :pdf-template-engine:testClasses
> Task :notification-service:jacocoTestReport
> Task :pdf-template-engine:test
> Task :notification-service:jacocoTestCoverageVerification
> Task :pdf-template-engine:jacocoTestReport
> Task :pdf-template-engine:jacocoTestCoverageVerification

[Incubating] Problems report is available at: file:///Users/usuario/.ralph-hotel/hypervelocity-worktrees/checkpoint-batch-1-uluow81o/build/reports/problems/problems-report.html

BUILD SUCCESSFUL in 2m 20s
75 actionable tasks: 75 executed
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.3.1/userguide/configuration_cache_enabling.html


$ npm --prefix frontend test
TIMEOUT

$ npm --prefix frontend run build
TIMEOUT

```
