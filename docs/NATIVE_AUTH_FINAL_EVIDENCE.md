# Auth-service Native O2 evidence

## Scope and provenance

Auth-only follow-up on `codex/auth-native`, PR [23](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/pull/23).
The isolated checkout is `/tmp/hotel-pms-auth.tekU99`; neither dirty user checkout was edited.

- Proven baseline: `52b591af369e3b615e18d284812689f0fec2f1fb`, [run 33746998802](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33746998802). Its compiler log explicitly reports optimization level **2**, and its evidence says `optimized-O2`; this was not inferred from green status.
- Follow-up gate/image source: `3c02003386700897fe272ddfbd140dcbf63ddb96`, [run 33844799765](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33844799765).
- Gate expansion commit: `3842bf44f2834683cdbe9fab522047d9ded36d4e`. Its [run 33829833736](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33829833736) compiled O2 successfully but failed in the Python harness (`Request.method` absent for inferred methods), so it is **not** final runtime evidence and exported no validated image bundle. The follow-up changes that counter to `Request.get_method()`; GET, implicit POST and explicit POST were verified locally with the actual standard-library Request implementation.
- Delta from the proven baseline: `.github/workflows/auth-native.yml`, `scripts/ci/verify-auth-native-runtime.sh`, `scripts/ci/measure-auth-native-runtime.py`, and this report. No application Java, schema, API, security configuration, JVM Dockerfile, Native Dockerfile, or Gradle changes in this follow-up.
- The branch already contained its original Native hints/configuration and Flyway V7-to-V8 migration filename repair. Those inherited changes were preserved, not reimplemented here.
- Dispatch uses `native_quick_build=false`. Gate-only commits use `[skip ci]` to avoid a duplicate PR quick build; the explicitly dispatched O2 gate still runs. Gradle caches, Buildx GHA caches, and the single `nativeCompile` invocation per job are preserved.

## Final result

**Individual Auth O2 gate: PASS**, verified from the downloaded evidence for [run 33844799765](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33844799765) on 2026-09-04. JVM tests and `processAot` passed. The Native build step restored the cached compiler layer (`RUN ... nativeCompile` is `CACHED`) in nine seconds, without another native compilation. Its image ID is exactly the same as the image compiled with optimization level 2 in run 33829833736. The corrected harness then validated that O2 image and the JVM control; `failure-class.txt` is `PASS`.

| Metric | Native O2 | JVM |
| --- | ---: | ---: |
| Startup to health UP, ms | 4,277 | 15,026 |
| Idle working set, bytes | 133,976,064 | 454,713,344 |
| Loaded median working set, bytes | 294,555,648 | 594,792,448 |
| Loaded sampled peak, bytes | 467,496,960 | 686,559,232 |
| Image size, bytes | 324,244,377 | 312,365,701 |
| Sustained load, seconds | 300.357 | 300.156 |
| Completed login -> refresh -> me cycles during load | 2,271 | 2,924 |
| Unexpected HTTP/transport failures; HTTP 5xx | 0; 0 | 0; 0 |
| Restarts/OOM during the measured window | 0 / false | 0 / false |

Native used 127.8 MiB idle and 280.9 MiB under load, versus 433.6 / 567.2 MiB JVM (70.5% / 50.5% lower working set). The Native image is **3.8% larger**, and Native completed fewer cycles in this bounded, paced workload. These results do not claim universally better throughput or a smaller image.

Each measurement JSON contains 62 memory samples (3 idle, 59 loaded). Independently checked HTTP counts contain 2,272 Native and 2,925 JVM successful requests **per** login/refresh/me endpoint, including the initial non-load cycle. Each mode records five expected refresh 401s, four me 401s, one invalid-login 401, and 61 successful requests per management endpoint. Raw counts contain no 5xx. Downloaded tenant-B and post-restart user responses were also checked directly: no tenant-A user leaked, and the created RECEPTIONIST survived restart.

All required Auth gates passed: Config Server credentials, PostgreSQL/JPA/Flyway (eight successful migrations), Redis token version/blacklist/nonce, JWT signatures/claims/expiry/rejections, login/refresh/me, Secure cookie flags, RBAC (RECEPTIONIST 403, ADMIN 200), missing HMAC 401, exact nonce replay 200/401, tenant isolation, restart persistence, health/liveness/readiness, and Prometheus request metrics. This is the individual service result; the complete stack is still a separate main-owned gate.

## Downloaded and verified artifacts

- **Native + JVM paired images:** [artifact 9926467624](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33844799765/artifacts/9926467624), `auth-native-images-3c02003386700897fe272ddfbd140dcbf63ddb96`, 294,967,632 bytes as the outer artifact ZIP; expires **2026-10-04 06:47:23 UTC**.
- **Runtime evidence:** [artifact 9926468065](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33844799765/artifacts/9926468065), `auth-native-runtime-evidence`, 230,777 bytes. Includes full metrics, sampled JSON, HTTP responses, claims, Prometheus scrapes, container state and logs.
- **Build record:** [artifact 9926468877](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33844799765/artifacts/9926468877), including the O2 cache-hit evidence.

The paired artifact was actually downloaded to `/tmp/hotel-pms-auth.tekU99-images-33844799765`, and the runtime evidence to `/tmp/hotel-pms-auth.tekU99-evidence-33844799765`. `shasum -a 256 -c SHA256SUMS`, `gzip -t`, and comparison of the archive's own `manifest.json` against the packaged manifest all passed. All four archive tags below were inspected, along with both image IDs, platform, non-root user and entrypoints. The bundled metrics match the downloaded runtime artifact. No local Docker load/run was needed for these archive checks.

```text
auth-service-images.tar.gz SHA256:
9a654d17681519a49682a1b958a068d4c2012b63878f896ba1f2e9aa8cb92b5f

Native image ID:
sha256:306e82a4af44ec25b4eaa2a6ce01f0c824671ef669a6740159f76dd1afa4d1a5
JVM image ID:
sha256:3c642706780760d297b0c01b21c3f0866f7ac4668aa22e99927a007b69b29c83
```

## Gate coverage

- Real authenticated Config Server configuration plus unauthenticated 401.
- Real PostgreSQL 15, all eight Flyway migrations, Hibernate/JPA user creation, tenant A/B separation, and a persisted user read back after deliberate Native restart.
- Login, refresh, and `/api/v1/auth/me` must return 200 and retain the existing response contract; the same flow must work after restart.
- Access/refresh JWT signatures are independently checked as HS256 against the CI key. Subject, ADMIN role, tenant, expiry, refresh type, and JTI rotation are asserted. Absent, malformed, altered-signature, and expired JWTs must return 401 for me/refresh. Invalid login and replay of an already consumed refresh token must return 401.
- Existing HMAC gates require missing HMAC 401, RECEPTIONIST 403, ADMIN 200, and the exact repeated nonce 200 then 401. Redis must contain the consumed HMAC nonce, token version, and refresh blacklist entry.
- Auth cookies must retain Secure/SameSite=Strict, with HttpOnly for access/refresh. The runner sends cookies explicitly over loopback HTTP; server cookie flags are unchanged.
- Health, liveness, readiness, and real Prometheus HTTP request metrics are sampled before, throughout, and after load for both runtimes.
- Three concurrent sessions each repeatedly perform login -> refresh -> me for at least 300 seconds per runtime. Every response is checked. Transport failures, unexpected status/body, any 5xx, changed container start time, restart, or OOM fail the gate. The deliberate persistence restart happens **after** the Native stability window.

## Measurement method

Both modes run sequentially on the same Ubuntu runner with separate fresh PostgreSQL databases and the same authenticated Config Server and Redis dependencies. Each mode uses the same three-worker loop and duration. These are bounded regression/stability measurements, not a controlled maximum-throughput benchmark.

Startup measures container launch until `/actuator/health` reports UP, polling every two seconds. Idle memory is the median of three pre-business-load samples, at five-second intervals after startup. Loaded memory is the median of samples every five seconds during sustained load, with the sampled peak also retained. All memory values are exact bytes from cgroup v2 `memory.current - inactive_file`, matching Docker's working-set convention. Raw samples, HTTP status counts, worker cycle counts, and container states remain in the evidence artifact.

Image sizes are `docker image inspect .Size` for both Native and JVM, not archive download sizes. Native uses its existing default heap policy; the existing JVM control keeps `-Xmx512m -XX:MaxMetaspaceSize=256m`. Config Server has a separate 256 MiB heap. Thus these measurements compare the existing runnable configurations and do not claim equal heap limits or total stack RAM.

## Integrated-stack handoff

The successful O2 workflow exported Native **and** JVM into one `auth-service-images.tar.gz`, using `docker save | gzip -1`. `actions/upload-artifact@v4` used `compression-level: 0` to avoid recompressing the gzip stream. The image artifact expires after 30 days.

The downloaded archive contains these exact tags (the `:ci` tags were retained on the CI runner for gates and appear in image-inspect metadata, but are not saved in the archive):

```text
hotel-pms/auth-service-native:validated
hotel-pms/auth-service-native:3c02003386700897fe272ddfbd140dcbf63ddb96
hotel-pms/auth-service-jvm:validated
hotel-pms/auth-service-jvm:3c02003386700897fe272ddfbd140dcbf63ddb96
```

The bundle also contains `SHA256SUMS`, `manifest.json`, both `*-image-inspect.json` metadata files, `provenance.json`, and the runtime `metrics.txt`. Immutable source tags and image IDs are the identity anchors; `:validated` is a convenient moving alias. Verify the checksum before `docker load`.

```bash
gh run download 33844799765 --repo serviciosdigitalesmx/hotel-pms-mexico \
  --name auth-native-images-3c02003386700897fe272ddfbd140dcbf63ddb96 \
  --dir auth-images
cd auth-images
sha256sum -c SHA256SUMS
docker load --input auth-service-images.tar.gz
```

Network aliases for integration: `config-server`, `redis`, `postgres`, and `auth-service`. Auth listens on application port 8087 and management port 8090. Use `SPRING_PROFILES_ACTIVE=auth-service`, authenticated `CONFIG_SERVER_URL=http://config-server:8888`, `AUTH_REDIS_HOST=redis`, and `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/hotel_auth`; supply the Config Server, PostgreSQL, Redis, JWT and HMAC credentials at runtime. No CI token or password is a deployment credential. Both images use the unchanged application configuration contract.

## Actual limits

- This service gate uses the real **JVM** Config Server. An all-Native Config Server dependency, browser/gateway routing, CSRF enforcement at the gateway, and the combined Native/JVM stacks belong to the main integration run.
- Actuator/Prometheus export is checked. This job does not run Zipkin or Loki collectors: the downloaded logs contain connection failures to those absent collectors. Trace/log delivery is therefore unvalidated; no Auth HTTP 5xx or Native reflection errors were found in the checked evidence.
- Both images are `linux/amd64`; the Native compiler targeted `x86-64-v3`. These are Linux integration artifacts, not macOS/arm64 binaries.
- Auth has no PDF route in this scope. No billing or frontdesk PDF validation is claimed here.
- Five minutes per mode proves the bounded interval recorded in the artifact, not hours/days of production uptime or absence of slow memory growth.
- No merge, local Docker run, or local `nativeCompile` was performed. Unrelated global quality failures are outside this work, and skipped global PR checks are not represented as passing. The final evidence update changes only this document with `[skip ci]`; image source remains `3c02003386700897fe272ddfbd140dcbf63ddb96`, and no additional build is required.
