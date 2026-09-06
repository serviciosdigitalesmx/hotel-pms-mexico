# Gateway Native O2 evidence and reusable images

## ADD scope and acceptance

Assigned branch: `codex/gateway-native`, PR #18. This increment starts from
`4c7cfc0bec82a11b87c76053758a9cb8dd1352db`, whose quick and final O2 runtime gates
passed. Prior O2 run: https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33802870641
(evidence artifact `9912147173`). Its evidence contains successful routing,
JWT/RBAC, Redis 429, and HMAC nonce 200/401 checks; it did not contain a reusable
image, loaded RAM, Prometheus verification, or sustained stability measurements.
Its old "idle" measurement followed functional traffic and is not comparable to
the new pre-traffic idle measurement.

This delta changes only Gateway workflow, gate instrumentation, and this report.
Gateway application sources, security, Lua RuntimeHints, Gradle configuration,
Native Dockerfile, schemas, contracts, and the JVM Dockerfile remain unchanged.
Billing and final integrated-stack work belong to the coordinating task.

Individual Gateway acceptance is PASS at runtime/image source commit
`065ea876667926a9ecaccbb3d4743d7e3ddab94f`: final O2 and JVM gates passed, all eight
startup/RAM/image-size fields are present, both sustained workloads finished with
zero failures and no restart/OOM, and both tested images were archived together.
The later documentation closure does not change or rebuild either binary.
PR application-source changes still run quick mode automatically; gate-only
changes use a manual final O2 dispatch without a duplicate quick run. Buildx GHA
and Gradle caches are retained, with at most one nativeCompile per job.

## Measurement and gate design

- Startup: wall time from `docker run` until `/actuator/health` becomes UP;
  health polling has approximately two-second resolution.
- Idle RAM: median of three cgroup-v2 working-set samples, after ten seconds with
  no application traffic. Native and JVM follow the same procedure.
- Loaded RAM: maximum cgroup working set (`memory.current - inactive_file`) from
  samples approximately every five seconds during 180 seconds of real traffic.
  Raw current/inactive-file values, first/last/mean/peak values are retained.
- Load: four clients, one request per client per second, alternating real Auth
  `/api/v1/auth/me` and Frontdesk `/api/v1/rooms`. Responses must be HTTP 200 with
  the expected identity/shape and propagated correlation IDs. Rooms uses its real
  paginated contract (`content`, `totalElements`, `number`); the seeded test tenant
  has no rooms, so this exercises a real authenticated empty-page database read.
- Sustained checks: health, liveness, readiness, unchanged running container,
  zero restarts/OOMs; Prometheus must expose actual HTTP request counters.
- Functional checks retain JWT authentication, real login, tenant-header
  replacement, receptionist read 200/write 403, Redis rate-limit 429, and
  downstream HMAC replay 200/401 plus persisted Redis nonce. Added checks cover
  invalid JWT with forged internal headers, CSRF denial, and refresh 200.
- JVM control also checks unauthenticated 401, login, refresh, RBAC read/write,
  then the same authenticated workload and observability/stability gates.

This is a bounded 180-second stability gate per runtime, not a long-duration leak
test or a maximum-throughput benchmark. JVM uses `-Xmx512m`; Native is not assigned
a heap limit by the JVM-only `JAVA_TOOL_OPTIONS`. Memory measurements cover each
gateway container, excluding shared dependencies. Image sizes are uncompressed
Docker image sizes, not archive download sizes.

## Artifact contract for the integrated stack

On successful manual final O2, `gateway-native-jvm-images-<full-source-sha>` includes:

- `api-gateway-o2-jvm-<full-source-sha>.tar.gz`: both validated Docker images;
- `SHA256SUMS`: checksum of that gzip archive;
- `native-image-inspect.json`, `jvm-image-inspect.json`, `provenance.json`, and
  `docker-save-manifest.json`: image IDs, OS/architecture, tags, sizes, revision,
  workflow URL, and archive mapping.

Tags preserved in the archive:

| Runtime | Reusable tag | Immutable revision tag |
| --- | --- | --- |
| Native O2 | `hotel-pms/api-gateway-native:validated` | `hotel-pms/api-gateway-native:<full-source-sha>` |
| JVM | `hotel-pms/api-gateway-jvm:validated` | `hotel-pms/api-gateway-jvm:<full-source-sha>` |

The existing `:ci` tags are used by the gate. Export uses `docker save | gzip -1`;
`actions/upload-artifact@v4` uses `compression-level: 0` to avoid recompressing the
gzip. Retention is 30 days. Artifact ID/URL/digest are recorded in runtime metrics
and the GitHub step summary. Verify `sha256sum --check SHA256SUMS`, then use
`docker load --input api-gateway-o2-jvm-<full-source-sha>.tar.gz` on Linux amd64.

Runtime configuration remains external. Set `CONFIG_SERVER_URL=http://config-server:8888`,
Config Server username/password, `GW_REDIS_HOST=redis`, Redis password, JWT/HMAC
secrets, and `SPRING_PROFILES_ACTIVE=api-gateway`. Default downstream names are
`auth-service:8087`, `frontdesk-service:8081`, `guest-service:8083`,
`billing-service:8085`, and `fb-service:8086`; `GW_*_SERVICE_URI` can explicitly
set them. The integration network supplies `config-server`, `redis`, `postgres`,
and service module aliases. Application/management ports are 8080/8090.

### Required integration profile

Set `SPRING_PROFILES_ACTIVE=api-gateway` explicitly for the Native integration and
the matching JVM comparison. This is the profile of the validated artifact, not
an image tag or the Config Server's own `native` repository-backend profile.

Evidence at `065ea876667926a9ecaccbb3d4743d7e3ddab94f`:

- `api-gateway/Dockerfile.native:50` and the workflow AOT preflight set that profile.
- `scripts/ci/verify-gateway-native-runtime.sh:83` sets it for both gateway runtimes.
- `api-gateway-native.log:33` and `api-gateway-jvm.log:13` report the active profile
  as `api-gateway`; both fetch Config Server environments for `default` and
  `api-gateway`.
- Both exported images' `Config.Env` omit `SPRING_PROFILES_ACTIVE`; the Native
  final stage contains no `ENV` that supplies it.
- The Gateway environment in the base `docker-compose.yml:328` omits it too.

The coordinating task must supply this environment entry in its integration
override. This documentation closure does not edit Compose. A run omitting the
profile, or activating `prod`/additional profiles, has not been validated here;
this report does not assert an observed failure for those untested variants.

## Final verified evidence (2026-09-04 UTC)

Final O2 run: [33830442810](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33830442810),
source SHA `065ea876667926a9ecaccbb3d4743d7e3ddab94f`. GitHub reports SUCCESS for
the cheap JVM tests/AOT preflight, image metadata, complete runtime gate, archive,
image upload, and evidence upload. The runtime gate ran from 02:43:32 to 02:51:10 UTC.

- Runtime evidence: [artifact 9921685813](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33830442810/artifacts/9921685813),
  `gateway-native-runtime-evidence`. The supplied local copy at
  `/tmp/pms-final-evidence.lbZiDy/gateway` was reviewed directly: `metrics.txt`,
  idle/load JSON samples, Prometheus output, logs, provenance, and archive manifest.
- Paired images: [artifact 9921685455](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33830442810/artifacts/9921685455),
  `gateway-native-jvm-images-065ea876667926a9ecaccbb3d4743d7e3ddab94f`.
  Published artifact size: 233,717,409 bytes. Current expiry: 2026-10-04 02:51:28 UTC.

### Exact metrics

RAM and image sizes below are bytes; RAM is the cgroup working set described above.

| Metric | Native O2 | JVM control |
| --- | ---: | ---: |
| Startup to health UP, ms | 2,258 | 8,820 |
| Idle RAM | 98,263,040 | 289,787,904 |
| Loaded RAM, sampled peak | 124,092,416 | 320,565,248 |
| Docker image size | 252,154,777 | 274,783,412 |
| Sustained duration, seconds | 180.001 | 180.001 |
| Authenticated requests | 720 | 720 |
| Request errors | 0 | 0 |
| Loaded memory samples | 36 | 36 |
| Request latency p95, ms | 23.769 | 29.405 |
| Restart count | 0 | 0 |
| OOM killed | false | false |

The sampled loaded mean was 123,353,429 bytes Native versus 314,237,838 JVM;
first/last samples were 121,454,592/123,580,416 Native and
306,192,384/317,710,336 JVM. Both final container states were running.

### ADD integration coverage

| Contract or integration | Final evidence/status |
| --- | --- |
| WebFlux routing to real Auth and Frontdesk | PASS in O2 and JVM; authenticated sustained requests |
| Health, liveness, readiness | UP in both modes, checked repeatedly during traffic |
| Login, refresh, `/auth/me` | 200 in both modes |
| JWT required | Unauthenticated 401 in both; malformed JWT plus forged internal headers 401 in Native |
| JWT RBAC | Receptionist rooms read 200 and room-type write 403 in both |
| Tenant/internal header replacement | Native spoofed-tenant request 200 with exactly the expected tenant's account; no other accounts returned |
| CSRF | Native mutation without CSRF header rejected 403 |
| Redis rate limiting/Lua resource | Native burst reaches 429 with existing limiter settings |
| Downstream HMAC and anti-replay | Native gate verifies first request 200, repeated nonce 401, nonce persisted in Redis |
| Config Server, Redis, PostgreSQL/Flyway dependencies | PASS using real services and the existing migrations |
| Prometheus and correlation IDs | PASS in both modes; exported HTTP request counters and response IDs inspected |
| Bounded sustained stability | PASS in both modes: 720 requests, zero errors/restarts/OOMs |
| Gateway JPA/Flyway | NOT APPLICABLE; Gateway itself is reactive and has no JPA/database schema |
| Integrated all-Native/frontend E2E | NOT VALIDATED BY THIS SERVICE GATE; coordinating task owns it |
| PDF routes and text/fonts | NOT VALIDATED; no PDF gate is claimed |

### Actual tags, image IDs, and checksums

Archive: `api-gateway-o2-jvm-065ea876667926a9ecaccbb3d4743d7e3ddab94f.tar.gz`.
`docker-save-manifest.json` contains these four tags:

```text
hotel-pms/api-gateway-native:validated
hotel-pms/api-gateway-native:065ea876667926a9ecaccbb3d4743d7e3ddab94f
hotel-pms/api-gateway-jvm:validated
hotel-pms/api-gateway-jvm:065ea876667926a9ecaccbb3d4743d7e3ddab94f
```

The two `:ci` tags exist in runner image metadata for gate execution, but are not
included in the archive. Use the SHA tags when pinning the integrated stack;
`:validated` is a convenient alias that a later Docker load may replace.

- Native image ID: `sha256:108acad0645e9fbc4c54cc7af5be04340bbf6b47541ebac4178a6278ec5e4512`.
- JVM image ID: `sha256:6af46eaac6a3f0fdd881ac37df7dab67827babc974ad6a7d5dd7876c47c82650`.
- Inner `.tar.gz` SHA256 from `SHA256SUMS`:
  `8317d4db6f661a18b9d204b5d53516dab2d36023ea757516b330d310463ef057`.
- GitHub artifact ZIP SHA256 (a different container/file):
  `0f7cf7ac8de8325c41de5bb3447f7ad8c55cf0a88fc2802ec807d0d169408ca1`.

Both images are Linux amd64 and run as `spring:spring`. Native entrypoint is
`/app/app`; JVM entrypoint is `java -jar app.jar`. Export uses gzip level 1 and
GitHub upload compression level 0; these two compression settings refer to
different layers of the download and must not be conflated.

The paired artifact was downloaded separately for this documentation closure to
`/tmp/hotel-pms-gateway.lCBz2E-images-33830442810` and verified without Docker or
recompilation. The inner archive is 233,699,604 bytes and its recomputed SHA256
matches `SHA256SUMS`. Its actual `manifest.json` matches the evidence manifest;
both image configuration blobs hash to the image IDs above and match provenance.
All referenced layers are present. The Native layer contains `app/app`, an
ELF64 x86-64 executable of 167,513,160 bytes; the JVM layer contains `app/app.jar`,
a JAR/ZIP of 56,384,680 bytes. Tags, Linux/amd64 architecture, entrypoints, and
non-root user all match the metadata. Local `docker load` was not run; the
coordinating task owns loading these images and executing the integrated stack.

### Change provenance

- `b1fa8735349eef139080c46fa83d61b55e9f819c`: Gateway gate metrics, sustained
  observation, and paired image export.
- `9022da8bec0629d60442924440605bf35d29505c`: skip duplicate quick builds for
  gate-only PR updates; use a protected mutation for the CSRF negative check.
- `065ea876667926a9ecaccbb3d4743d7e3ddab94f`: correct the new load assertion to
  validate the existing paginated rooms contract. Prior run 33829974899 correctly
  failed because the observer incorrectly expected a flat array; no service
  source change was needed. Its failed load result is superseded by the final run.

The Gateway application, Dockerfiles, Gradle configuration, and security code
have no delta from the proven `4c7cfc0` baseline. This closure changes only this
report; the validated image SHA remains `065ea876...` even after the docs commit.

## Explicit boundaries

The service gate runs Native Gateway against real JVM Config/Auth/Frontdesk plus
PostgreSQL and Redis. It does not certify the all-Native stack or frontend E2E.
Guest/Billing/F&B-specific routes, public booking, and PDF output are not exercised.
In particular, Frontdesk PDF text/fonts are unvalidated here; Billing PDF correction
is owned by the coordinating task. Gateway does not itself use JPA/Flyway; those
checks apply to the real Auth and Frontdesk dependencies. Existing unrelated global
quality failures do not count as this service's Native gate result. No merges.

Zipkin span delivery is not validated: no collector was started by this gate, and
both runtime logs contain dropped-span connection warnings. The observability
PASS above covers Actuator/Prometheus and correlation IDs only. The bounded load
and gateway-only RAM comparison do not replace full-stack RAM/E2E measurements.
