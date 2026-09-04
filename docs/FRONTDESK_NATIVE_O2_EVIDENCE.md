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

Only the Frontdesk workflow and runtime gate change. Application source,
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

## Final verification

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

Pending: final O2 run, artifact download/checksum/tag verification, final
metrics and test counts. No completion claim until these are recorded here.

## Actual limitations outside this gate

- `GET /api/v1/quotations/{id}/pdf` exists but PDF text/fonts are **unvalidated**.
  A successful HTTP response or PDF signature is not valid PDF content proof.
  Main owns the Billing Identity-H/text/font correction; this delta does not
  modify the shared PDF engine or certify Frontdesk quotations.
- Guest and Billing dependencies in this independent gate are real JVM
  controls. Main owns the all-Native/all-JVM integrated stack and frontend E2E.
- Notification delivery is not exercised by this independent gate; the
  complete notification flow belongs in main's integrated stack validation.
- Prometheus and structured logs are collected; Zipkin/Loki export and
  external Alloggiati submission are not tested.
- Global frontend/PMD failures are unrelated unless they directly block this
  service gate. Do not broaden this migration into global quality work.
