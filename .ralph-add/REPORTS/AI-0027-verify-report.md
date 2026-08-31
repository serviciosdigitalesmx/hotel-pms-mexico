# AI-0027 Verification Result

- Result: FAIL
- Source/workspace mutation: none detected
- Commands:
  - `./gradlew :frontdesk-service:test --info`

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
$ ./gradlew :frontdesk-service:test --info
exit=1
ayId=e9cd360c-6ce5-4cf1-bfcf-c726b553e32e | reservationId=16abf87a-ccce-4c37-b7c0-d04f482768ad | guestId=c5ab39dd-0a30-4f79-a6dc-68c6f9f5b237 | roomId=5b299411-2ef0-4df6-bab8-9bed95e60b41
    2026-08-20T18:12:38.919-06:00  INFO 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.service.impl.StayServiceImpl     : [STAY] SAGA_ROOM_OCCUPIED | stayId=e9cd360c-6ce5-4cf1-bfcf-c726b553e32e | roomId=5b299411-2ef0-4df6-bab8-9bed95e60b41
    2026-08-20T18:12:38.919-06:00 ERROR 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.s.impl.StayBillingCoordinator    : [STAY] INVOICE_CREATION_FAILED | stayId=e9cd360c-6ce5-4cf1-bfcf-c726b553e32e | reason=INVOICE_ALREADY_EXISTS_FOR_STAY

StayServiceImplTest > shouldRejectCheckInWhenReservationIsNoShow() STANDARD_OUT
    2026-08-20T18:12:38.921-06:00  INFO 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.service.impl.StayServiceImpl     : Processing check-in | reservationId=ffe687e4-d7d4-471b-92db-d833ecdbbb2b | walkIn=false
    2026-08-20T18:12:38.921-06:00  WARN 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.s.impl.StayCheckInValidator      : [STAY] CHECK_IN_FAILED | reservationId=ffe687e4-d7d4-471b-92db-d833ecdbbb2b | reason=INVALID_RESERVATION_STATUS | currentStatus=NO_SHOW

StayServiceImplTest > shouldCheckInSuccessfully() STANDARD_OUT
    2026-08-20T18:12:38.922-06:00  INFO 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.service.impl.StayServiceImpl     : Processing check-in | reservationId=548113c7-e009-4e5f-a347-cec6c9e7c547 | walkIn=false
    2026-08-20T18:12:38.922-06:00  INFO 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.service.impl.StayServiceImpl     : [STAY] CHECK_IN_SUCCESS | stayId=08007a85-15a2-464e-a18d-cb0e91fdcb3c | reservationId=548113c7-e009-4e5f-a347-cec6c9e7c547 | guestId=2509e0ec-081b-4d68-8442-a85b2e284c1a | roomId=c89f3ccd-aa07-463e-a7a2-8fd424e1dcd4
    2026-08-20T18:12:38.922-06:00  INFO 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.service.impl.StayServiceImpl     : [STAY] SAGA_ROOM_OCCUPIED | stayId=08007a85-15a2-464e-a18d-cb0e91fdcb3c | roomId=c89f3ccd-aa07-463e-a7a2-8fd424e1dcd4
    2026-08-20T18:12:38.922-06:00 ERROR 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.s.impl.StayBillingCoordinator    : [STAY] INVOICE_CREATION_FAILED | stayId=08007a85-15a2-464e-a18d-cb0e91fdcb3c | reason=BILLING_SERVICE_UNAVAILABLE

StayServiceImplTest > shouldRetryOnlyChargeWhenInvoiceAlreadyCreatedButChargeFailed() STANDARD_OUT
    2026-08-20T18:12:38.924-06:00  INFO 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.s.impl.StayBillingCoordinator    : [STAY] INVOICE_CREATED | stayId=c9ee376f-6cd4-416c-8579-5c385d2f5803 | invoiceId=ea87c7a6-6ec8-47ac-96d6-b828ac3ca3a4 | roomChargeId=938afade-acd9-420d-886e-83ef69b8c4e9

StayServiceImplTest > shouldSendCheckoutEmailWithoutAttachmentWhenBillingServiceCannotProducePdf() STANDARD_OUT
    2026-08-20T18:12:38.925-06:00  INFO 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.service.impl.StayServiceImpl     : Processing check-out for stay ID: 04ced941-6d1a-4335-aba0-3131f9fa24ce
    2026-08-20T18:12:38.925-06:00  INFO 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.service.impl.StayServiceImpl     : [STAY] CHECK_OUT_SUCCESS | stayId=04ced941-6d1a-4335-aba0-3131f9fa24ce | reservationId=ecacc56e-8ecb-4716-9547-3345f45d4071 | roomId=85a124b8-20ae-443a-9ec0-de0eebf09b4e

StayServiceImplTest > shouldThrowWhenCheckOutWalkInStayHasNoInvoiceId() STANDARD_OUT
    2026-08-20T18:12:38.926-06:00  INFO 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.service.impl.StayServiceImpl     : Processing check-out for stay ID: f55adede-62a4-4ba2-b096-c4491bd96ae1
    2026-08-20T18:12:38.926-06:00  WARN 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.service.impl.StayServiceImpl     : [STAY] CHECK_OUT_FAILED | stayId=f55adede-62a4-4ba2-b096-c4491bd96ae1 | reservationId=null | reason=BILLING_NOT_PAID

StayServiceImplTest > shouldUpdateReservationStatusToCheckedIn() STANDARD_OUT
    2026-08-20T18:12:38.927-06:00  INFO 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.service.impl.StayServiceImpl     : Processing check-in | reservationId=d8fc29c0-cf16-4012-890a-0ba3935d18d1 | walkIn=false
    2026-08-20T18:12:38.927-06:00  INFO 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.service.impl.StayServiceImpl     : [STAY] CHECK_IN_SUCCESS | stayId=d4ac0465-37d5-424c-ae01-54e081881a99 | reservationId=d8fc29c0-cf16-4012-890a-0ba3935d18d1 | guestId=a2e028ec-0da0-45c3-ad15-391aa6f42c35 | roomId=4f2a1158-4a9d-4acf-b175-670af6c6a592
    2026-08-20T18:12:38.928-06:00  INFO 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.service.impl.StayServiceImpl     : [STAY] SAGA_ROOM_OCCUPIED | stayId=d4ac0465-37d5-424c-ae01-54e081881a99 | roomId=4f2a1158-4a9d-4acf-b175-670af6c6a592
    2026-08-20T18:12:38.928-06:00 ERROR 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.s.impl.StayBillingCoordinator    : [STAY] INVOICE_CREATION_FAILED | stayId=d4ac0465-37d5-424c-ae01-54e081881a99 | reason=BILLING_SERVICE_UNAVAILABLE

StayServiceImplTest > shouldRejectCheckInWhenReservationIsCancelled() STANDARD_OUT
    2026-08-20T18:12:38.933-06:00  INFO 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.service.impl.StayServiceImpl     : Processing check-in | reservationId=e17c527e-9bc7-4899-a2f3-0750ffb022e0 | walkIn=false
    2026-08-20T18:12:38.934-06:00  WARN 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.s.impl.StayCheckInValidator      : [STAY] CHECK_IN_FAILED | reservationId=e17c527e-9bc7-4899-a2f3-0750ffb022e0 | reason=INVALID_RESERVATION_STATUS | currentStatus=CANCELLED

StayServiceImplTest > shouldOpenInvoiceInBillingServiceOnCheckIn() STANDARD_OUT
    2026-08-20T18:12:38.935-06:00  INFO 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.service.impl.StayServiceImpl     : Processing check-in | reservationId=4e44a803-704f-4798-b859-ae32e25858da | walkIn=false
    2026-08-20T18:12:38.935-06:00  INFO 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.service.impl.StayServiceImpl     : [STAY] CHECK_IN_SUCCESS | stayId=d748d7e7-50e1-4565-94d8-7a171ac95fb4 | reservationId=4e44a803-704f-4798-b859-ae32e25858da | guestId=9a50a382-3134-47e5-b5b3-02d39b2bceee | roomId=689bbbde-5331-4873-b246-b1ba639864ee
    2026-08-20T18:12:38.935-06:00  INFO 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.service.impl.StayServiceImpl     : [STAY] SAGA_ROOM_OCCUPIED | stayId=d748d7e7-50e1-4565-94d8-7a171ac95fb4 | roomId=689bbbde-5331-4873-b246-b1ba639864ee
    2026-08-20T18:12:38.935-06:00  INFO 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.s.impl.StayBillingCoordinator    : [STAY] INVOICE_CREATED | stayId=d748d7e7-50e1-4565-94d8-7a171ac95fb4 | invoiceId=d5c3bd06-d086-45df-8df7-f4cebc001dde | roomChargeId=2df445a9-6e1b-496b-b22b-760e528898dd

StayServiceImplTest > shouldMarkInvoiceCreationFailedWhenBillingServiceUnavailable() STANDARD_OUT
    2026-08-20T18:12:38.937-06:00  INFO 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.service.impl.StayServiceImpl     : Processing check-in | reservationId=5e7e424c-7432-4605-a13a-11ab3449cfa3 | walkIn=false
    2026-08-20T18:12:38.937-06:00  INFO 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.service.impl.StayServiceImpl     : [STAY] CHECK_IN_SUCCESS | stayId=4c6f701e-5d2e-4423-a5ec-fdb3081a9e59 | reservationId=5e7e424c-7432-4605-a13a-11ab3449cfa3 | guestId=99055e10-dcb0-48c3-ac22-5281f772d93f | roomId=66a633af-043c-4ab1-bcd9-77e5c24d5431
    2026-08-20T18:12:38.937-06:00  INFO 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.service.impl.StayServiceImpl     : [STAY] SAGA_ROOM_OCCUPIED | stayId=4c6f701e-5d2e-4423-a5ec-fdb3081a9e59 | roomId=66a633af-043c-4ab1-bcd9-77e5c24d5431
    2026-08-20T18:12:38.937-06:00 ERROR 29537 --- [frontdesk-service] [    Test worker] [                                                 ] c.h.f.s.s.impl.StayBillingCoordinator    : [STAY] INVOICE_CREATION_FAILED | stayId=4c6f701e-5d2e-4423-a5ec-fdb3081a9e59 | reason=BILLING_SERVICE_UNAVAILABLE

TenantIsolationArchTest > CUSTOM_QUERY_METHODS_ON_TENANT_ROOT_REPOSITORIES_MUST_SCOPE_BY_HOTEL_ID STANDARD_OUT
    2026-08-20T18:12:38.955-06:00  INFO 29537 --- [frontdesk-service] [    Test worker] [                                                 ] com.tngtech.archunit.core.PluginLoader   : Detected Java version 21.0.11

Gradle Test Executor 13 finished executing tests.
2026-08-20T18:12:40.755-06:00  INFO 29537 --- [frontdesk-service] [ionShutdownHook] [                                                 ] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-08-20T18:12:41.071-06:00  INFO 29537 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-08-20T18:12:41.073-06:00  INFO 29537 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
2026-08-20T18:12:41.089-06:00  INFO 29537 --- [frontdesk-service] [ionShutdownHook] [                                                 ] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-08-20T18:12:41.089-06:00  INFO 29537 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Shutdown initiated...
2026-08-20T18:12:41.090-06:00  INFO 29537 --- [frontdesk-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Shutdown completed.

> Task :frontdesk-service:test

408 tests completed, 1 failed
Generating HTML test report...
Finished generating test html results (0.063 secs) into: /Users/usuario/Desktop/HOTEL-PMS/frontdesk-service/build/reports/tests/test
Finished generating test XML results (0.036 secs) into: /Users/usuario/Desktop/HOTEL-PMS/frontdesk-service/build/test-results/test

> Task :frontdesk-service:test FAILED

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':frontdesk-service:test'.
> There were failing tests. See the report at: file:///Users/usuario/Desktop/HOTEL-PMS/frontdesk-service/build/reports/tests/test/index.html

* Try:
> Run with --scan to get full insights from a Build Scan (powered by Develocity).

BUILD FAILED in 38s
14 actionable tasks: 1 executed, 13 up-to-date
Watched directory hierarchies: [/Users/usuario/Desktop/HOTEL-PMS]


```
