# Frontdesk Native O2 — ADD scope and evidence

## Reopened scope: quotation PDF content and fonts

The user reopened the individual gate on 2026-09-04 because the existing
quotation PDF route shares the Billing PDF engine while the then-current
Frontdesk Native configuration had no proven Identity-H/XMPBox/AWT support.
The earlier O2 artifact at source `738d010f` remains valid only for its
documented non-PDF coverage and is superseded for integrated handoff by the
artifact at source `38fd5a2b` below.

The strengthened acceptance creates and reloads a priced quotation through
the existing REST contract, verifies tenant/HMAC denial on its PDF endpoint,
downloads the PDF twice, and requires HTTP 200, PDF bytes, Spanish text plus
the actual guest/room/server-calculated total, embedded Noto Sans Regular and
Bold with Unicode mapping, valid pages and rendered PNGs. The final O2 run
passes all of those assertions in both Native and JVM modes. The shared PDF
engine, public contracts, schemas and security behavior were not changed.
Billing `80e20fef45235ee8f114d736f1ab571bb0642812` was used read-only as the
proven AWT/Identity-H reference. Status: **approved for this bounded
individual-service gate**; integrated-stack validation remains owned by main.

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

The reopened delta is restricted to the Frontdesk workflow/runtime PDF gate,
Native Docker/GraalVM AWT support, Frontdesk runtime hints and this report.
It does not refactor `pdf-template-engine` or change the JVM Dockerfile,
business implementation, API/schema or authorization rules. The Native
builder captures AWT reachability with the bundled Noto fonts; the runtime
includes the required AWT/font libraries and Graal-produced JNI libraries;
the hints retain PDF templates/fonts, `Identity-H` and XMPBox `TextType`.
No local Docker or nativeCompile was used. Each runner invokes at most one
nativeCompile; `processAot` is its prerequisite. Existing focused JVM tests
run before image building. The `[final-o2]` selector prevents a duplicate PR
quick build while the complete O2 is dispatched explicitly.

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

## Final verification — individual O2/PDF gate passed

Validated source/image commit:
`38fd5a2bad5ca33071b362d95fda48a20d5bf68f`.
[Final run 33855201348](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33855201348)
completed successfully on 2026-09-04 at 09:14:49 UTC. Logs show exactly one
`:frontdesk-service:nativeCompile`, compiler optimization level **2**, target
**x86-64-v3**, generated AWT JNI libraries and the captured AWT reachability
metadata. The complete runtime gate ended
`NATIVE_GATE_PASS phase=complete`; no baseline-image diagnostic mode was
used. The corresponding PR run `33855200677` only ran the selector, so no
duplicate quick Native compilation occurred.

Downloaded and inspected the actual focused-test XML, runtime evidence,
PDFs/previews and Docker save archive. Final focused tests: **116 passed,
0 failures, 0 errors, 0 skipped**. Both business flows link the created
room, reservation, stay and invoice IDs through check-in, payment and
check-out. SQL assertions confirm one persisted checked-out stay per
database and Flyway version 20.

| Measured field | Native O2 | JVM control |
| --- | ---: | ---: |
| Startup to health UP | 10,507 ms | 32,140 ms |
| Idle Docker memory snapshot | 147.6 MiB | 484.4 MiB |
| Peak sampled memory during load | 144.1 MiB | 562.5 MiB |
| Peak sampled memory, bytes | 151,099,802 | 589,824,000 |
| Docker image size, bytes | 371,799,666 | 320,946,355 |
| Sustained load duration | 301.35 s | 301.19 s |
| Successful signed reads | 11,302 | 11,464 |
| Concurrent workers | 4 | 4 |
| Memory samples | 27 | 27 |
| Health/probe checks | 87/87 | 87/87 |
| Load/probe errors; restarts; OOM | 0; 0; false | 0; 0; false |

Each mode completed 29 checks of each health, liveness and readiness
endpoint. Native read counts were rooms 2,827, reservations 2,824, stays
2,825 and room types 2,826; JVM completed 2,866 reads of each route.
Responses were checked for nonempty persisted data. Prometheus snapshots
before and after load contain HTTP request, Hikari connection and process
uptime metrics. The raw samples, totals, probes and container state agree
with the corresponding summary fields.

For both modes: authenticated Config Server retrieval,
PostgreSQL/JPA/Flyway, Redis, and real Guest/Billing Feign calls passed.
Missing and invalid HMAC returned 401; identical nonce replay returned 200
then 401. Cross-tenant quotation/PDF, reservation, room and stay access
returned 404; receptionist room-type writes returned 403; legacy Alloggiati
reports denied both ADMIN and RECEPTIONIST.

Native startup was 67.3% shorter, idle memory 69.5% lower and peak sampled
loaded memory 74.4% lower; its image was **15.8% larger** than the JVM
control. Idle is one snapshot immediately after startup; loaded RAM is the
maximum of 27 later Docker working-set samples. The modes ran sequentially
with separate fresh Frontdesk databases and the same JVM downstream
services. These are individual-service measurements, not total-backend RAM,
capacity or long-term leak certification.

### Quotation PDF contract evidence

Native and JVM each created then reloaded one real priced quotation. Four
downloads were checked independently (two per mode): HTTP 200,
`application/pdf`, attachment filename containing the quotation ID, `%PDF-`
bytes, one valid rendered page, and extracted text containing `COTIZACIÓN`,
`Habitación`, `Native Frontdesk`, the actual room, `Opción PDF Native` and
the server-calculated `200.00`. `pdffonts` reports both subsetted
`NotoSans-Bold` and `NotoSans-Regular` as embedded with `Identity-H` and
Unicode maps (`emb=yes`, `sub=yes`, `uni=yes`) in every download.

Native PDFs were 22,340 bytes; JVM PDFs were 22,089 bytes. The retained
first-page PNG from each mode was visually inspected and is legible with
the expected accents, guest, room and amount. The POST/GET persistence
contract compares all 14 stable response fields exactly; only the database
representation boundary for `createdAt`/`updatedAt` permits a maximum one
microsecond difference. Cross-tenant access returned 404 and missing HMAC
returned 401. Repeated PDFs are deliberately validated separately rather
than assumed byte-deterministic.

This closes the former empty/unverified-PDF risk for Frontdesk's quotation
route under the stated content/font contract. It does not certify PDF/UA:
OpenHTMLToPDF logs that no document description is present and ignores some
flexbox declarations. The inspected output remains visually correct for this
template; accessibility/remediation was not expanded into this Native fix.

### Downloadable images and evidence

| Artifact | ID | Purpose |
| --- | --- | --- |
| [Paired Native/JVM images](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33855201348/artifacts/9930690941) | `9930690941` | Docker save gzip, metadata, checksum, provenance and copied metrics |
| [Runtime/PDF evidence](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33855201348/artifacts/9930691624) | `9930691624` | Both functional flows, PDFs/text/fonts/previews, Prometheus, load samples and logs |
| [Focused test XML](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33855201348/artifacts/9930692152) | `9930692152` | 116 passing focused tests |

Image artifact name:
`frontdesk-service-validated-images-38fd5a2bad5ca33071b362d95fda48a20d5bf68f`.
It expires **2026-10-04 09:14:01 UTC**, unless retained elsewhere by main.
The successful upload log confirms `actions/upload-artifact@v4` with
`compression-level: 0`. The API-reported artifact ZIP digest is
`sha256:3cd92a044f4e8e233d4a729e2ebf48ae35513b019d19f06a70c04a8d56bd57aa`.

The inner `frontdesk-service-images.tar.gz` is 321,353,078 bytes and has
SHA-256:

```text
343d56694715f9f1bda71a9d7649175de0fa39fc6dacfada3893504307f30e99
```

Verified `SHA256SUMS`, gzip integrity, both manifest entries, all six tags,
image-config IDs, every referenced layer and all 32 SHA-named blob content
hashes. Native has five layers; JVM has nine. The image artifact metrics are
byte-identical to the runtime evidence. No local Docker load/run was
performed; main owns the integrated runtime.

Actual loadable tags:

```text
hotel-pms/frontdesk-service-native:validated
hotel-pms/frontdesk-service-native:38fd5a2bad5ca33071b362d95fda48a20d5bf68f
hotel-pms/frontdesk-service-native:ci
hotel-pms/frontdesk-service-jvm:validated
hotel-pms/frontdesk-service-jvm:38fd5a2bad5ca33071b362d95fda48a20d5bf68f
hotel-pms/frontdesk-service-jvm:ci
```

Docker image IDs:

```text
Native sha256:274c55da66b6a09a7b492b152c0339ef3e0aabbc495a052b28b4ce04f39ff2fd
JVM    sha256:cd72f016fd7fa56078f864ae52c50afb9d8228b5135f464e32e4a72fb1bcadd7
```

Verified local download paths (temporary, on this Mac):

```text
/tmp/frontdesk-final-verify.loE9YZ/paired/frontdesk-service-images.tar.gz
/tmp/frontdesk-final-verify.loE9YZ/paired/SHA256SUMS
/tmp/frontdesk-final-verify.loE9YZ/paired/image-metadata.json
/tmp/frontdesk-final-verify.loE9YZ/paired/provenance.json
/tmp/frontdesk-final-verify.loE9YZ/runtime/native/pdf-result.json
/tmp/frontdesk-final-verify.loE9YZ/runtime/jvm/pdf-result.json
/tmp/frontdesk-final-verify.loE9YZ/tests/
```

This closes Frontdesk's individual O2/PDF gate, evidence and paired-image
handoff. It does not close the overall PMS migration. This final
documentation-only closure does not replace the source/image SHA above and
requires no binary rebuild.

### Preflight history and unrelated failures

The PDF reopening separated harness defects from the application defect.
Initial harness commit `30ed027e` and
[diagnostic run 33846365517](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33846365517)
reused the prior paired images without nativeCompile and failed in both modes
because the first harness compared the full POST and persisted GET JSON;
PostgreSQL legitimately serializes stored timestamps at microsecond rather
than Java nanosecond precision. Commit `b02866ab` changed that check to the
stable contract fields, and [run 33854012734](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33854012734)
showed the remaining one-microsecond PostgreSQL rounding boundary. Commit
`fa07b3df` limits timestamp tolerance to that boundary; it does not relax any
PDF assertion.

[Diagnostic run 33854533076](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33854533076)
then reached the real Native PDF route and retained its HTTP 500 evidence in
artifact `9929730592`. The Native log showed
`UnsatisfiedLinkError: Can't load library: awt` through PDFBox and
OpenHTMLToPDF. Commit `38fd5a2b` applies the bounded proven correction:
builder/runtime AWT and font libraries, captured AWT reachability, emitted
Graal JNI libraries, headless dynamic-library arguments, XMPBox `TextType`
constructor reflection, and the bundled `Identity-H` CMap. The succeeding
O2 result above demonstrates that this was an application Native gap after
the harness was corrected, not a false green or a PDF-engine replacement.

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

- Frontdesk quotation PDF text and embedded fonts are now validated in both
  modes, but PDF/UA accessibility is not. OpenHTMLToPDF reports no document
  description and unsupported flexbox declarations; no PDF-template refactor
  was authorized or attempted.
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
- Native logs contain Lettuce, bounded-elastic and Zipkin reporter thread
  warnings during the intentional shutdown that precedes the JVM control.
  They occur after the passing load window; shutdown/thread cleanup and
  long-term leak behavior are not certified by this five-minute gate.
- External Alloggiati submission is not tested; the existing Mexico runtime
  correctly keeps those HTTP routes denied.
- Global frontend/PMD failures are unrelated unless they directly block this
  service gate. Do not broaden this migration into global quality work.
