# Frontdesk Native O2 — ADD scope and evidence

## A.SPEC: complete the proven service gate and preserve loadable images

Owner: `codex/frontdesk-native`, PR #22. Isolated checkout:
`/tmp/hotel-pms-frontdesk.LhlPgp`. Do not modify the dirty Desktop/dev
checkouts, other service migrations, the shared billing PDF implementation,
schemas, API contracts, or security configuration. No merge is authorized.

Baseline: commit `e8222c2c4b0981e6d7cb959bab84c6ea067f9e14`,
[O2 run 33802818935](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33802818935),
runtime evidence artifact `9912278128`. Inspected the downloaded evidence and
compiler log: optimization level 2; startup 10,360 ms Native / 25,345 ms JVM;
idle memory 138.7 MiB / 467.5 MiB. This baseline lacks loaded memory,
probe/Prometheus assertions, sustained load, and downloadable images.

The implementation delta changes only the Frontdesk workflow, runtime gate
and this report. The final closure changes documentation only. Application source,
GraalVM arguments, Native Dockerfile and JVM Dockerfile stay unchanged.
The existing Native Docker ignore excludes workflow, gate and documentation
changes, allowing the previous O2 cache to be reused when available. No
local Docker or nativeCompile is used. Each runner invokes at most one
nativeCompile; processAot is its prerequisite. Existing focused JVM tests run
before image building. PR synchronization skips compilation when only gate
or documentation files changed; final O2 is dispatched explicitly.

Acceptance: both modes execute real Config Server/PostgreSQL/Redis and
Guest/Billing JVM integrations; rooms → reservation → check-in → invoice →
payment → check-out; SQL persistence and Flyway 20; missing/invalid HMAC,
nonce replay, RBAC rejection, cross-tenant reservation/room/stay denial;
health/liveness/readiness and Prometheus HTTP/database/process metrics.
Both modes then run 300 seconds of identical concurrent signed reads, four
workers with 100 ms pauses, nonempty persisted result checks, >=100 reads
per route, >=20 memory samples, no request/probe errors, OOM or restart.
Loaded RAM is the peak Docker working-set sample during that load; raw
samples and duration are retained. This bounded test is not a long-term
memory-leak or production-capacity certification.

## Image handoff contract

A successful final O2 gate saves Native **and** JVM images in one gzip tar,
including these tags for each mode:

- `hotel-pms/frontdesk-service-native:ci`, `:validated`, `:<full-commit-sha>`
- `hotel-pms/frontdesk-service-jvm:ci`, `:validated`, `:<full-commit-sha>`

Artifact name: `frontdesk-service-validated-images-<full-commit-sha>`.
Contains `frontdesk-service-images.tar.gz`, `SHA256SUMS`, full Docker image
metadata, provenance, gate status and all final metrics. The upload uses
`actions/upload-artifact@v4`, compression-level 0, retention 30 days.

After download: `sha256sum -c SHA256SUMS`, then
`gzip -dc frontdesk-service-images.tar.gz | docker load`.
Runtime architecture: linux/amd64; Native CPU target: x86-64-v3, as in the
proven O2. Configure Config Server URL, PostgreSQL URLs/credentials, Redis
and HMAC through runtime environment variables. Shared network aliases:
`config-server`, `postgres`, `redis`, `frontdesk-service`, `guest-service`,
`billing-service`. No CI dependency names or placeholder credentials are
required in the integrated stack: main supplies its runtime configuration.

## Final verification — individual O2 gate passed

Validated source/image commit:
`738d010f0e80812be96d11070147ec41804b04a6`.
[Final run 33830356702](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33830356702)
completed successfully on 2026-09-04 at 03:05:21 UTC. Logs show
`processAot`, exactly one `:frontdesk-service:nativeCompile`, compiler
optimization level **2**, and target **x86-64-v3**. The binary was rebuilt in
this run (11m25s image-build step); this was not a complete cache hit. The
runtime gate took 11m39s and ended `NATIVE_GATE_PASS phase=complete`.

Downloaded and inspected the actual test XML, runtime evidence and Docker
save archive. Final focused tests: **116 passed, 0 failures, 0 errors,
0 skipped**. Both business flows link the created room, reservation, stay
and invoice IDs through check-in, payment and check-out. SQL assertions
confirm one persisted checked-out stay per database and Flyway version 20.

| Measured field | Native O2 | JVM control |
| --- | ---: | ---: |
| Startup to health UP | 10,373 ms | 25,349 ms |
| Idle Docker memory snapshot | 137.8 MiB | 512.9 MiB |
| Peak sampled memory during load | 123.5 MiB | 567.1 MiB |
| Peak sampled memory, bytes | 129,499,136 | 594,647,450 |
| Docker image size, bytes | 358,126,489 | 320,946,301 |
| Sustained load duration | 301.20 s | 301.27 s |
| Successful signed reads | 11,310 | 11,448 |
| Concurrent workers | 4 | 4 |
| Memory samples | 27 | 27 |
| Health/probe checks | 87/87 | 87/87 |
| Load/probe errors; restarts; OOM | 0; 0; false | 0; 0; false |

Each mode completed 29 checks of each health, liveness and readiness
endpoint. Native read counts were rooms 2,829, reservations 2,826, stays
2,827, room types 2,828; JVM completed 2,862 reads of each route. Responses
were checked for nonempty persisted data. Prometheus snapshots before and
after load contain HTTP request, Hikari connection and process uptime
metrics. The raw `load-stability.json` samples, totals, probes and container
state were cross-checked against every corresponding summary field.

For both modes: Config Server authenticated configuration retrieval,
PostgreSQL/JPA/Flyway, Redis, and real Guest/Billing Feign calls passed.
Missing and invalid HMAC returned 401; identical nonce replay returned
200 then 401. Cross-tenant reservation, room and stay reads returned 404;
receptionist room-type writes returned 403; legacy Alloggiati reports denied
both ADMIN and RECEPTIONIST. Security and API/schema code did not change.

Native startup was 59.1% shorter, idle memory 73.1% lower, and peak sampled
loaded memory 78.2% lower in this run; its image was **11.6% larger**. Idle
is a single snapshot immediately after startup; loaded RAM is the maximum
of 27 later Docker working-set samples during authenticated reads after the
business flow. Thus Native loaded memory below its earlier idle snapshot is
not contradictory and is not a process-lifetime peak measurement. The two
modes ran sequentially with separate fresh Frontdesk databases and the same
JVM downstream services; these are individual-service results, not total
backend RAM or production throughput measurements.

### Downloadable images and evidence

| Artifact | ID | Purpose |
| --- | --- | --- |
| [Paired Native/JVM images](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33830356702/artifacts/9921963152) | `9921963152` | Docker save gzip, metadata, checksum, provenance and copied metrics |
| [Runtime evidence](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33830356702/artifacts/9921963416) | `9921963416` | Both functional flows, Prometheus, load samples and container logs |
| [Focused test XML](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33830356702/artifacts/9921963627) | `9921963627` | 116 passing focused tests |

Image artifact name:
`frontdesk-service-validated-images-738d010f0e80812be96d11070147ec41804b04a6`.
The image artifact expires **2026-10-04 03:05:12 UTC**, unless retained
elsewhere by the integrated-stack owner. `compression-level: 0` was verified
in the successful upload step. Its GitHub artifact ZIP digest is
`sha256:8be5dd577eecdeb7029d42fbacc310a83cbcd42f50246c5dec948c42c7b7ff09`.

The inner Docker archive `frontdesk-service-images.tar.gz` has SHA-256:

```text
56d36b7422ce047ab83e92e88bc3f66eefe04b072964ae4f2cf9ed384c5fa7a8
```

Verified `SHA256SUMS`, gzip integrity, both manifest entries, all six tags,
image-config digests against Docker metadata, all referenced layers, and
every SHA-named blob's content hash. Native has four layers; JVM has nine.
The image artifact's metrics are byte-identical to the runtime evidence.
No local Docker load/run was performed; main owns the integrated runtime.

Actual loadable tags:

```text
hotel-pms/frontdesk-service-native:validated
hotel-pms/frontdesk-service-native:738d010f0e80812be96d11070147ec41804b04a6
hotel-pms/frontdesk-service-native:ci
hotel-pms/frontdesk-service-jvm:validated
hotel-pms/frontdesk-service-jvm:738d010f0e80812be96d11070147ec41804b04a6
hotel-pms/frontdesk-service-jvm:ci
```

Docker image IDs:

```text
Native sha256:70c2245eb72c764d1056e6d64b421f6059a06ab5706e3a5eb72befa1a9337f32
JVM    sha256:d9e0715a91f00928dcacdc40f629d2fad591c8168b1e59a1a2755ba6d7ed879e
```

Verified local handoff paths (temporary, on this Mac):

```text
/tmp/hotel-pms-frontdesk.LhlPgp/build/final-images/frontdesk-service-images.tar.gz
/tmp/hotel-pms-frontdesk.LhlPgp/build/final-images/SHA256SUMS
/tmp/hotel-pms-frontdesk.LhlPgp/build/final-images/image-metadata.json
/tmp/hotel-pms-frontdesk.LhlPgp/build/final-images/provenance.json
/tmp/hotel-pms-frontdesk.LhlPgp/build/final-tests/
/tmp/pms-final-evidence.lbZiDy/frontdesk/metrics.txt
/tmp/pms-final-evidence.lbZiDy/frontdesk/native/load-stability.json
/tmp/pms-final-evidence.lbZiDy/frontdesk/jvm/load-stability.json
```

This closes Frontdesk's individual O2 gate/evidence/image handoff. It does
not close the overall PMS migration. The final documentation-only commit
does not replace the source/image SHA above and requires no binary rebuild.

### Preflight history and unrelated failures

Initial expanded run [33829932473](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33829932473)
stopped before Native compilation: 119 existing tests ran, 116 passed and
three legacy Alloggiati positive tests failed (expected 200, actual 403).
The baseline controller already uses `@PreAuthorize("denyAll()")`, introduced
by `738215e` (Mexico runtime safeguards). These stale positive expectations
are `UNRELATED_GLOBAL_CI_FAIL`; no application/security/test sources changed.
The focused suite retains all three receptionist-denied checks and the
remaining 113 service/tenant tests. Real runtime RBAC additionally verifies
receptionist room-type writes return 403 and legacy reports deny both ADMIN
and RECEPTIONIST. Original failure XML artifact: `9921319062`.

Run [33830144074](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33830144074)
passed the 116 focused tests but the automatically finalized whole-module
JaCoCo verification demanded 40% coverage from this partial selection (20%).
Only this focused command excludes `jacocoTestCoverageVerification`; the
repository coverage rule and full-suite commands remain unchanged. No
Native compilation ran in either failed preflight. Buildx now writes a
Frontdesk/mode-specific GHA scope to prevent parallel service cache eviction,
while also reading the prior default scope for O2 reuse.

## Actual limitations outside this gate

- `GET /api/v1/quotations/{id}/pdf` exists but PDF text/fonts are **unvalidated**.
  A successful HTTP response or PDF signature is not valid PDF content proof.
  Main owns the Billing Identity-H/text/font correction; this delta does not
  modify the shared PDF engine or certify Frontdesk quotations.
- Guest and Billing dependencies in this independent gate are real JVM
  controls. Main owns the all-Native/all-JVM integrated stack and frontend E2E.
- Notification delivery is not exercised by this independent gate; the
  Native and JVM logs explicitly show reservation-confirmed and checkout
  emails suppressed by the notification fallback. Main must validate real
  delivery in the integrated stack.
- Prometheus and application logs are collected. Both modes dropped spans
  because Zipkin was absent; Zipkin/Loki export remains unvalidated here
  and belongs to main's stack. Native logs also warn that GC notification
  metrics are unavailable; the three Alloggiati caches do not record full
  cache statistics. Do not claim complete observability parity.
- Native logs contain a Lettuce event-loop thread warning after the
  intentional shutdown that precedes the JVM control. It is outside the
  passing load window; shutdown/thread cleanup and long-term leak behavior
  are not certified by this five-minute gate.
- External Alloggiati submission is not tested; the existing Mexico runtime
  correctly keeps those HTTP routes denied.
- Global frontend/PMD failures are unrelated unless they directly block this
  service gate. Do not broaden this migration into global quality work.
