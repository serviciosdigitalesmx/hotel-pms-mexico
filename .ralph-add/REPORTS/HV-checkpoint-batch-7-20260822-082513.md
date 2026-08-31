# Hypervelocity checkpoint batch-7

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
08:26:16.778 [SpringApplicationShutdownHook] INFO  [] o.s.o.j.LocalContainerEntityManagerFactoryBean - Closing JPA EntityManagerFactory for persistence unit 'default'
08:26:17.132 [SpringApplicationShutdownHook] INFO  [] com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Shutdown initiated...
08:26:17.134 [SpringApplicationShutdownHook] INFO  [] com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Shutdown completed.
> Task :billing-service:test

> Task :common-web-lib:compileTestJava
[ant:jacocoReport] Note: /Users/usuario/.ralph-hotel/hypervelocity-worktrees/checkpoint-batch-7-4uj366c9/common-web-lib/src/test/java/com/hotelpms/commonweb/exception/ProblemDetailAdviceTest.java uses or overrides a deprecated API.
[ant:jacocoReport] Note: Recompile with -Xlint:deprecation for details.

> Task :common-web-lib:processTestResources NO-SOURCE
> Task :common-web-lib:testClasses
> Task :billing-service:jacocoTestReport
> Task :common-web-lib:test
> Task :billing-service:jacocoTestCoverageVerification
> Task :common-web-lib:jacocoTestReport
> Task :config-service:compileJava
> Task :config-service:processResources
> Task :config-service:classes
> Task :common-web-lib:jacocoTestCoverageVerification
> Task :config-service:compileTestJava
> Task :config-service:processTestResources NO-SOURCE
> Task :config-service:testClasses
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
2026-08-22T08:26:26.125-06:00  INFO 25795 --- [config-service] [ionShutdownHook] o.s.b.w.e.tomcat.GracefulShutdown        : Commencing graceful shutdown. Waiting for active requests to complete
2026-08-22T08:26:26.128-06:00  INFO 25795 --- [config-service] [tomcat-shutdown] o.s.b.w.e.tomcat.GracefulShutdown        : Graceful shutdown complete
2026-08-22T08:26:26.266-06:00  INFO 25795 --- [config-service] [ionShutdownHook] o.s.b.w.e.tomcat.GracefulShutdown        : Commencing graceful shutdown. Waiting for active requests to complete
2026-08-22T08:26:26.268-06:00  INFO 25795 --- [config-service] [tomcat-shutdown] o.s.b.w.e.tomcat.GracefulShutdown        : Graceful shutdown complete
> Task :config-service:test
> Task :config-service:jacocoTestReport
> Task :fb-service:compileJava
> Task :config-service:jacocoTestCoverageVerification
> Task :fb-service:processResources
> Task :fb-service:classes
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

2026-08-22T08:27:19.759-06:00  INFO 25840 --- [frontdesk-service] [ionShutdownHook] [                                                 ] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-08-22T08:27:20.149-06:00  INFO 25840 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-08-22T08:27:20.157-06:00  INFO 25840 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
2026-08-22T08:27:20.183-06:00  INFO 25840 --- [frontdesk-service] [ionShutdownHook] [                                                 ] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-08-22T08:27:20.184-06:00  INFO 25840 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Shutdown initiated...
2026-08-22T08:27:20.185-06:00  INFO 25840 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Shutdown completed.

> Task :frontdesk-service:jacocoTestReport
> Task :guest-service:compileJava
> Task :guest-service:processResources
> Task :guest-service:classes
> Task :frontdesk-service:jacocoTestCoverageVerification
> Task :guest-service:compileTestJava
> Task :guest-service:processTestResources
> Task :guest-service:testClasses
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
2026-08-22T08:27:49.575-06:00  INFO 25954 --- [guest-service] [ionShutdownHook] [                                                 ] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-08-22T08:27:50.004-06:00  INFO 25954 --- [guest-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-08-22T08:27:50.006-06:00  INFO 25954 --- [guest-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
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

[Incubating] Problems report is available at: file:///Users/usuario/.ralph-hotel/hypervelocity-worktrees/checkpoint-batch-7-4uj366c9/build/reports/problems/problems-report.html

BUILD SUCCESSFUL in 2m 55s
75 actionable tasks: 75 executed
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.3.1/userguide/configuration_cache_enabling.html


$ npm --prefix frontend test
TIMEOUT

$ npm --prefix frontend run build
exit=0

> frontend@0.0.0 build
> tsc -b && vite build

vite v8.2.1 building client environment for production...
[2K
transforming...Browserslist: browsers data (caniuse-lite) is 6 months old. Please run:
  npx update-browserslist-db@latest
  Why you should do it regularly: https://github.com/browserslist/update-db#readme
✓ 1516 modules transformed.
rendering chunks...
computing gzip size...
dist/index.html                                               0.73 kB │ gzip:  0.41 kB
dist/assets/inter-vietnamese-400-normal-DMkecbls.woff2        4.97 kB
dist/assets/inter-vietnamese-300-normal-Bdr24Bqb.woff2        5.06 kB
dist/assets/inter-vietnamese-600-normal-Cc8MFFhd.woff2        5.10 kB
dist/assets/inter-vietnamese-700-normal-DlLaEgI2.woff2        5.10 kB
dist/assets/inter-vietnamese-500-normal-DOriooB6.woff2        5.11 kB
dist/assets/inter-greek-ext-400-normal-DGGRlc-M.woff2         5.26 kB
dist/assets/inter-greek-ext-300-normal-l2DDyC6M.woff2         5.34 kB
dist/assets/inter-greek-ext-500-normal-C4iEst2y.woff2         5.42 kB
dist/assets/inter-greek-ext-600-normal-DRtmH8MT.woff2         5.43 kB
dist/assets/inter-greek-ext-700-normal-qfdV9bQt.woff2         5.44 kB
dist/assets/outfit-latin-ext-500-normal-zeox_O30.woff2        6.24 kB
dist/assets/outfit-latin-ext-400-normal-5tcqmc2S.woff2        6.38 kB
dist/assets/outfit-latin-ext-800-normal-DRv2ic_2.woff2        6.47 kB
dist/assets/outfit-latin-ext-700-normal-CI4iH74K.woff2        6.48 kB
dist/assets/inter-vietnamese-400-normal-Bbgyi5SW.woff         6.50 kB
dist/assets/outfit-latin-ext-600-normal-B85nYjL1.woff2        6.52 kB
dist/assets/inter-vietnamese-300-normal-DDGmYYdT.woff         6.58 kB
dist/assets/inter-vietnamese-500-normal-mJboJaSs.woff         6.59 kB
dist/assets/inter-vietnamese-700-normal-BZaoP0fm.woff         6.63 kB
dist/assets/inter-vietnamese-600-normal-BuLX-rYi.woff         6.64 kB
dist/assets/inter-greek-ext-400-normal-KugGGMne.woff          7.06 kB
dist/assets/inter-greek-ext-300-normal-DLbbeei1.woff          7.12 kB
dist/assets/inter-greek-ext-500-normal-2j5mBUwD.woff          7.19 kB
dist/assets/inter-greek-ext-600-normal-B8X0CLgF.woff          7.21 kB
dist/assets/inter-greek-ext-700-normal-BoQ6DsYi.woff          7.21 kB
dist/assets/inter-cyrillic-400-normal-obahsSVq.woff2          7.71 kB
dist/assets/inter-greek-400-normal-B4URO6DV.woff2             7.77 kB
dist/assets/inter-cyrillic-300-normal-BnqRxXuy.woff2          7.81 kB
dist/assets/inter-cyrillic-500-normal-BasfLYem.woff2          7.90 kB
dist/assets/inter-cyrillic-700-normal-CjBOestx.woff2          7.90 kB
dist/assets/inter-greek-500-normal-BIZE56-Y.woff2             7.92 kB
dist/assets/inter-greek-700-normal-C3JjAnD8.woff2             7.92 kB
dist/assets/inter-greek-600-normal-plRanbMR.woff2             7.94 kB
dist/assets/inter-cyrillic-600-normal-CWCymEST.woff2          7.97 kB
dist/assets/inter-greek-300-normal-DmGD3g_f.woff2             8.00 kB
dist/assets/outfit-latin-ext-500-normal-DrCvqoFD.woff         8.49 kB
dist/assets/outfit-latin-ext-400-normal-DHm7mdGe.woff         8.64 kB
dist/assets/outfit-latin-ext-600-normal-CWJcPgd7.woff         8.75 kB
dist/assets/outfit-latin-ext-800-normal-DyhPHUt-.woff         8.76 kB
dist/assets/outfit-latin-ext-700-normal-fjS8-Gm7.woff         8.78 kB
dist/assets/inter-cyrillic-400-normal-HOLc17fK.woff           9.78 kB
dist/assets/inter-cyrillic-300-normal-LR1W_oT8.woff           9.81 kB
dist/assets/inter-cyrillic-700-normal-DrXBdSj3.woff           9.91 kB
dist/assets/inter-greek-400-normal-q2sYcFCs.woff              9.92 kB
dist/assets/inter-cyrillic-600-normal-4D_pXhcN.woff           9.93 kB
dist/assets/inter-cyrillic-500-normal-CxZf_p3X.woff           9.94 kB
dist/assets/inter-greek-500-normal-Xzm54t5V.woff              9.98 kB
dist/assets/inter-greek-700-normal-BUv2fZ6O.woff              9.98 kB
dist/assets/inter-greek-300-normal-BrhSP0vQ.woff              9.99 kB
dist/assets/inter-greek-600-normal-BZpKdvQh.woff             10.03 kB
dist/assets/inter-cyrillic-ext-400-normal-BQZuk6qB.woff2     10.23 kB
dist/assets/inter-cyrillic-ext-300-normal-CgCALhwJ.woff2     10.35 kB
dist/assets/inter-cyrillic-ext-500-normal-B0yAr1jD.woff2     10.43 kB
dist/assets/inter-cyrillic-ext-600-normal-Dfes3d0z.woff2     10.48 kB
dist/assets/inter-cyrillic-ext-700-normal-BjwYoWNd.woff2     10.49 kB
dist/assets/inter-cyrillic-ext-400-normal-DQukG94-.woff      13.33 kB
dist/assets/inter-cyrillic-ext-300-normal-RId2JxDB.woff      13.40 kB
dist/assets/inter-cyrillic-ext-700-normal-LO58E6JB.woff      13.40 kB
dist/assets/inter-cyrillic-ext-500-normal-BmqWE9Dz.woff      13.45 kB
dist/assets/inter-cyrillic-ext-600-normal-Bcila6Z-.woff      13.46 kB
dist/assets/outfit-latin-500-normal-DKnIMDSk.woff2           13.52 kB
dist/assets/outfit-latin-400-normal-BGsTXAXT.woff2           14.03 kB
dist/assets/outfit-latin-800-normal-CQna6-G7.woff2           14.04 kB
dist/assets/outfit-latin-700-normal-Cu9v6i1X.woff2           14.06 kB
dist/assets/outfit-latin-600-normal-B7SfZ07L.woff2           14.14 kB
dist/assets/outfit-latin-500-normal-ClnHRwRh.woff            17.68 kB
dist/assets/outfit-latin-400-normal-DMwTpYkH.woff            18.23 kB
dist/assets/outfit-latin-700-normal-D4itBLBr.woff            18.34 kB
dist/assets/outfit-latin-800-normal-BRHLSPcU.woff            18.39 kB
dist/assets/outfit-latin-600-normal-BEfTtDA7.woff            18.41 kB
dist/assets/inter-latin-400-normal-C38fXH4l.woff2            23.66 kB
dist/assets/inter-latin-300-normal-BVlfKGgI.woff2            23.91 kB
dist/assets/inter-latin-500-normal-Cerq10X2.woff2            24.27 kB
dist/assets/inter-latin-700-normal-Yt3aPRUw.woff2            24.35 kB
dist/assets/inter-latin-600-normal-LgqL8muc.woff2            24.45 kB
dist/assets/inter-latin-400-normal-CyCys3Eg.woff             30.69 kB
dist/assets/inter-latin-300-normal-i8F0SvXL.woff             31.01 kB
dist/assets/inter-latin-600-normal-CiBQ2DWP.woff             31.26 kB
dist/assets/inter-latin-500-normal-BL9OpVg8.woff             31.28 kB
dist/assets/inter-latin-700-normal-BLAVimhd.woff             31.32 kB
dist/assets/inter-latin-ext-400-normal-C1nco2VV.woff2        35.00 kB
dist/assets/inter-latin-ext-300-normal-CPgO9Ksf.woff2        35.88 kB
dist/assets/inter-latin-ext-500-normal-CV4jyFjo.woff2        36.02 kB
dist/assets/inter-latin-ext-700-normal-Ca8adRJv.woff2        36.24 kB
dist/assets/inter-latin-ext-600-normal-D2bJ5OIk.woff2        36.26 kB
dist/assets/inter-latin-ext-400-normal-77YHD8bZ.woff         47.56 kB
dist/assets/inter-latin-ext-500-normal-BxGbmqWO.woff         48.49 kB
dist/assets/inter-latin-ext-300-normal-Dp1L8vcn.woff         48.60 kB
dist/assets/inter-latin-ext-700-normal-TidjK2hL.woff         48.63 kB
dist/assets/inter-latin-ext-600-normal-CIVaiw4L.woff         48.66 kB
dist/assets/material-symbols-outlined-DTCSuhiZ.woff2      3,915.91 kB
dist/assets/CalendarPlanning-COks4oDc.css                    10.62 kB │ gzip:  2.42 kB
dist/assets/index-BXlIQohX.css                               51.18 kB │ gzip:  9.40 kB
dist/assets/errorMessage-eCQNYHno.js                          0.17 kB │ gzip:  0.16 kB
dist/assets/passwordPolicy-B-6Lzf6f.js                        0.21 kB │ gzip:  0.15 kB
dist/assets/M3Card-CGtAqvSy.js                                0.43 kB │ gzip:  0.26 kB
dist/assets/userService-DCStX85W.js                           0.44 kB │ gzip:  0.23 kB
dist/assets/rateSeasonService-C5nQSYDe.js                     0.48 kB │ gzip:  0.27 kB
dist/assets/guestService-798lX1i0.js                          0.60 kB │ gzip:  0.34 kB
dist/assets/M3StatusChip-Crni-VQL.js                          0.60 kB │ gzip:  0.35 kB
dist/assets/fbService-CxqzHT0e.js                             0.61 kB │ gzip:  0.28 kB
dist/assets/PasswordVisibilityToggle-AVlljMw9.js              0.62 kB │ gzip:  0.39 kB
dist/assets/M3TableActionLink-n7xQBw27.js                     0.64 kB │ gzip:  0.36 kB
dist/assets/billingReportService-D-RR_Y9Z.js                  0.73 kB │ gzip:  0.50 kB
dist/assets/reservationService-Cg5V356C.js                    0.76 kB │ gzip:  0.39 kB
dist/assets/SettingsPageHeader-BUZSRpsa.js                    0.77 kB │ gzip:  0.42 kB
dist/assets/inventoryService-CdDdeaIp.js                      0.86 kB │ gzip:  0.37 kB
dist/assets/quotationService-DeAV7uzD.js                      0.97 kB │ gzip:  0.44 kB
dist/assets/M3Table-BclZLXJu.js                               1.01 kB │ gzip:  0.47 kB
dist/assets/M3SegmentedRow-UW5PeTtF.js                        1.31 kB │ gzip:  0.71 kB
dist/assets/billingService-CNiCAPFQ.js                        1.33 kB │ gzip:  0.56 kB
dist/assets/SettingsProfile-Bxig_gZY.js                       1.33 kB │ gzip:  0.64 kB
dist/assets/dashboardService-CMssjC3Z.js                      1.34 kB │ gzip:  0.68 kB
dist/assets/Login-dibxfxfL.js                                 1.58 kB │ gzip:  0.82 kB
dist/assets/M3Dialog-BVC3Epat.js                              1.71 kB │ gzip:  0.81 kB
dist/assets/M3TextField-CvsZYmie.js                           1.84 kB │ gzip:  0.91 kB
dist/assets/SettingsAccessibility-3gb5fzid.js                 2.46 kB │ gzip:  1.05 kB
dist/assets/Settings-DxTouz6x.js                              2.47 kB │ gzip:  1.00 kB
dist/assets/SettingsPassword-CXrrePpO.js                      3.02 kB │ gzip:  1.32 kB
dist/assets/SettingsAppearance-VIUMRyOp.js                    3.07 kB │ gzip:  1.36 kB
dist/assets/RoomSelection-C-mi2QC9.js                         3.62 kB │ gzip:  1.50 kB
dist/assets/CheckInForm-D5LTG8aR.js                           5.30 kB │ gzip:  2.18 kB
dist/assets/Dashboard-CR5s7k7L.js                             5.91 kB │ gzip:  2.10 kB
dist/assets/Housekeeping-C0UDikut.js                          6.04 kB │ gzip:  2.11 kB
dist/assets/OwnerDashboard-C-pVafAR.js                        6.81 kB │ gzip:  2.09 kB
dist/assets/WalkInCheckInForm-CyPqYifs.js                     7.35 kB │ gzip:  2.47 kB
dist/assets/Quotations-D6YlEKI7.js                            7.72 kB │ gzip:  2.56 kB
dist/assets/SettingsSystem-CP9KrTSY.js                        7.88 kB │ gzip:  2.49 kB
dist/assets/HotelProfile-Dsan0lwy.js                          8.36 kB │ gzip:  2.61 kB
dist/assets/ReservationForm-BHl_Vwc2.js                       9.85 kB │ gzip:  3.35 kB
dist/assets/Reservations---Og5GXN.js                         10.43 kB │ gzip:  3.46 kB
dist/assets/authStore-D_EzjumG.js                            10.53 kB │ gzip:  4.24 kB
dist/assets/GuestFieldSection-BHCf3Xr8.js                    10.76 kB │ gzip:  3.07 kB
dist/assets/QuotationForm-BqsLDu_j.js                        11.28 kB │ gzip:  3.81 kB
dist/assets/QuotationDetail-qlEd_32F.js                      11.86 kB │ gzip:  3.17 kB
dist/assets/AdminUsers-CnvTWbjv.js                           13.15 kB │ gzip:  3.29 kB
dist/assets/RateCalendar-1ayFwJFx.js                         13.50 kB │ gzip:  4.54 kB
dist/assets/Stays-BX0vmp8j.js                                14.00 kB │ gzip:  4.11 kB
dist/assets/Assistant-C4Ve1u0t.js                            15.87 kB │ gzip:  5.18 kB
dist/assets/Guests-Celwachj.js                               16.07 kB │ gzip:  4.53 kB
dist/assets/Billing-Bop-1ll2.js                              17.74 kB │ gzip:  4.89 kB
dist/assets/Restaurant-DFy7gu1u.js                           21.71 kB │ gzip:  5.61 kB
dist/assets/Rooms-DXeRKhkP.js                                25.46 kB │ gzip:  5.51 kB
dist/assets/it-ChNJib2b.js                                   32.13 kB │ gzip:  8.15 kB
dist/assets/schemas-Bo3zeGyq.js                              69.31 kB │ gzip: 18.48 kB
dist/assets/CalendarPlanning-Bi1Qzkzt.js                    195.75 kB │ gzip: 57.97 kB
dist/assets/api-C5oH9PS7.js                                 206.61 kB │ gzip: 66.32 kB
dist/assets/index-Tw1v8bwa.js                               284.35 kB │ gzip: 90.47 kB

✓ built in 1.25s


```
