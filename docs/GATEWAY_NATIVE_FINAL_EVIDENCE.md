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

Acceptance requires final O2 and JVM gates to pass, all eight startup/RAM/image-size
fields to be present, the sustained workload to finish with zero failures and no
restart/OOM, and a downloadable archive containing the exact two tested images.
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
  the expected identity/shape and propagated correlation IDs.
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

## Pending evidence

The revised final O2 run, downloadable artifact IDs, exact final metrics, and
download verification will be recorded here after execution. Until then this
increment is not validated.

## Explicit boundaries

The service gate runs Native Gateway against real JVM Config/Auth/Frontdesk plus
PostgreSQL and Redis. It does not certify the all-Native stack or frontend E2E.
Guest/Billing/F&B-specific routes, public booking, and PDF output are not exercised.
In particular, Frontdesk PDF text/fonts are unvalidated here; Billing PDF correction
is owned by the coordinating task. Gateway does not itself use JPA/Flyway; those
checks apply to the real Auth and Frontdesk dependencies. Existing unrelated global
quality failures do not count as this service's Native gate result. No merges.
