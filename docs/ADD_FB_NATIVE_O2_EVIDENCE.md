# ADD: F&B Native O2 individual evidence and image handoff

Evidence verified on 2026-09-04. Individual F&B O2 gate and paired image export: complete. Integrated all-Native/all-JVM PMS E2E: owned by Main and not certified by this report. This closure adds documentation only; it does not rebuild or merge anything.

## Scope and source

- Assigned branch: `codex/fb-native`; existing PR: [#21](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/pull/21).
- Isolated working checkout: `/tmp/hotel-pms-fb-native.5K0O9M`. The user's Desktop and development checkouts were not edited.
- Previous proven O2: `e8d6d95b728b2b2464b646dd33d093d36c9ec5d9`, [run 33801254805](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33801254805), evidence artifact `9912096315`.
- Validated gate/image source revision: `83d5584aabd4c5b49e8121e79389b185ca877645`, [run 33829870882](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33829870882), **SUCCESS**, completed 2026-09-04 02:57:25 UTC. This source SHA remains the immutable image tag even after the documentation commit.
- This revision changes only `.github/workflows/fb-native.yml`, `scripts/ci/verify-fb-native-runtime.sh`, and the root-build exclusion in `fb-service/Dockerfile.native.dockerignore`. F&B domain source, Native hints, build flags, schema, security and APIs are identical to the proven O2 source.
- The pre-existing PR also contains unrelated CI/frontend/auth/billing changes from earlier work. This revision neither modifies nor removes them. Review this bounded delta separately; no merge or history rewrite was performed.

## Build and gate

The workflow runs the F&B JVM tests and Spring AOT checks before a single Native build. Manual dispatch selects `optimized-O2` or `quick-Ob`; PR events use quick mode. This revision was dispatched once in O2 mode with automatic push/PR workflows skipped, avoiding a duplicate quick build for unchanged proven domain code. Maven/Gradle caching and Buildx GHA caching remain enabled. Buildx has a per-service/per-mode cache scope plus the previous cache as a read fallback. Root build logs are excluded from the Native context so runtime evidence does not invalidate the binary cache.

Native compilation remains in the GitHub Linux runner, using the existing GraalVM 24.0.2 image, `-O2`, `-J-Xmx12g`, and `--parallelism=2`. The completed log confirms GraalVM CE 24.0.2+11.1, **optimization level 2**, target **x86-64-v3**, and one executed `:fb-service:nativeCompile`. The JVM control uses the existing Java 21 Dockerfile. No local Docker or local native compilation was used. The runner executed the O2 build in this run; no cache-hit or skipped-binary-build claim is made.

The gate runs real Config Server, PostgreSQL 15, Redis, Frontdesk JVM and Billing JVM dependencies. It retains the existing Native MenuItem/RestaurantOrder create/confirm, server-calculated prices, tenant A/B isolation, real Feign calls and Billing charge verification, Redis nonce replay rejection, and both downstream fallback checks. Additional coverage includes:

- Health, liveness, readiness and a nonempty Prometheus process metric for both runtimes.
- Successful Flyway history, SQL persistence, restart and authenticated REST readback.
- JVM order creation and confirmation with a real verified Billing charge.
- Unsigned and incorrectly signed requests rejected with 401; authenticated RECEPTIONIST catalog writes rejected with 403 in both runtimes.
- Comparable startup, idle memory, loaded memory and image size.
- Sustained authenticated reads, body checks and negative tenant reads; no OOM or automatic restart.
- Required metric values must exist and be positive before the gate may write `native_runtime_gate=PASS`. Failures record `NATIVE_GATE_FAIL`.

## Measurement method

Startup measures elapsed milliseconds from `docker start` through health, liveness and readiness UP, with 200 ms health polling. Both services are first bootstrapped and then measured against the same already-migrated database and ready downstream services. These are service restart-to-readiness measurements, not fresh-database installation times. The timers include Docker command overhead and the three probe checks.

Memory values are exact bytes from cgroup v2 `memory.current - inactive_file`, the Linux Docker working-set convention. Raw current, inactive-file and working-set counters are retained in `memory-samples.csv`. Idle is the median of five one-second samples after 15 quiet seconds. Loaded memory is the sampled peak during 120 seconds of four concurrent authenticated menu-read clients per runtime. Native and JVM are exercised simultaneously against the same data; successful request counts are recorded independently, so this is equal concurrency and duration, not equal throughput. The values describe this workload on one shared runner, not production capacity or a long-term leak test.

After load, the gate runs at least 300 seconds of health/liveness/readiness, authenticated menu reads with actual item/price checks, and empty tenant-B results for both runtimes, with timestamped samples. Intentional restart/persistence and downstream-outage checks are separate from this stability window.

## Final results

The run's successful state was checked against the downloaded runtime evidence, raw samples, REST bodies, Flyway history, and image archive. Runtime evidence artifact: [9921802055](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33829870882/artifacts/9921802055), `fb-native-runtime-evidence-optimized-O2-83d5584aabd4c5b49e8121e79389b185ca877645`. Local evidence reviewed at `/tmp/pms-final-evidence.lbZiDy/fb`.

| Measurement | Native O2 | JVM control |
| --- | ---: | ---: |
| `*_startup_ms` | 2,303 | 14,041 |
| `*_idle_memory` (bytes) | 125,902,848 | 484,118,528 |
| Idle working set (MiB) | 120.07 | 461.69 |
| `*_loaded_memory` (bytes) | 123,092,992 | 535,814,144 |
| Loaded working set (MiB) | 117.39 | 510.99 |
| `*_image_size_bytes` | 324,834,201 | 312,200,953 |
| Image size (MiB) | 309.79 | 297.74 |
| Successful requests during 120-second load | 6,427 | 6,335 |
| Failed load requests | 0 | 0 |
| Stability | PASS | PASS |

Native startup was 83.60% shorter; idle and loaded working sets were 73.99% and 77.03% lower. The Native image was **4.05% larger** than the JVM image. Native loaded memory is lower than its earlier idle median in these observations: these are separate measurement windows, not a lifetime high-water mark. All 218 raw cgroup samples passed arithmetic checks; each runtime's median was recomputed from five idle samples and its loaded peak from 48 load samples.

| ADD validation block | Completed evidence | Status / boundary |
| --- | --- | --- |
| Cheap JVM tests and AOT | 47 tests across 7 suites; 0 failed, 0 errored, 0 skipped; `processAot` passed | PASS |
| Final Native build | Graal optimization level 2; one nativeCompile; image metadata verified | PASS |
| Config/PostgreSQL/Redis | Real F&B Config profile; PostgreSQL and password-protected Redis ready; Flyway V1–V7 successful | PASS |
| Probes and observability | Health, liveness, readiness UP and `process_uptime_seconds` exported for Native and JVM | PASS; no external Zipkin delivery claim |
| Domain and real downstream Feign | Native order create/confirm; invoice charge 25.00; JVM order create/confirm; invoice charge 12.50; matching order reference IDs in actual invoice JSON | PASS |
| Tenant isolation | Tenant-B catalog empty and cross-tenant order rejected with 404; repeated tenant-B reads in stability window | PASS |
| HMAC / replay / RBAC | Native nonce replay 401; both runtimes reject unsigned/invalid HMAC with 401 and RECEPTIONIST catalog writes with 403 | PASS |
| Persistence | Billed order verified in SQL and returned by authenticated REST after Native restart | PASS |
| Resilience4j fallbacks | Real Billing and Frontdesk containers stopped; existing fallback paths verified | PASS for existing fallback behavior; not delivery during outage |
| Sustained operation | 120-second load, followed by **301 seconds / 56 timestamped stability samples** for both runtimes; no OOM or automatic restart | PASS for this bounded window |
| Downloadable Native/JVM pair | Both image configs and all six archive tags verified; tar checksum and gzip integrity verified | PASS |
| All-Native / all-JVM integrated stack and frontend E2E | Not executed by this service gate | PENDING — Main owns integration |

`metrics.txt` records `native_build_mode=optimized-O2`, `native_runtime_gate=PASS`, and `image_artifact_id=9921801599`. Local Bash/workflow syntax checks and ShellCheck also passed on the gate revision. No unrelated global PMD/Vitest result is represented as a Native gate result.

## Image handoff contract

Paired image artifact: [9921801599](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33829870882/artifacts/9921801599), named `fb-service-optimized-O2-images-83d5584aabd4c5b49e8121e79389b185ca877645`, 295,330,668 bytes in GitHub. It expires **2026-10-04 02:57:07 UTC**. The downloaded pair was inspected at `/tmp/hotel-pms-fb-native.5K0O9M/build/final-o2-images`.

Archive: `fb-service-optimized-O2-83d5584aabd4c5b49e8121e79389b185ca877645.tar.gz`. Its actual Docker save manifest contains these six tags:

| Runtime | Gate tag | Integrated-stack tag | Immutable source tag |
| --- | --- | --- | --- |
| Native | `hotel-pms/fb-service-native:ci` | `hotel-pms/fb-service-native:validated` | `hotel-pms/fb-service-native:83d5584aabd4c5b49e8121e79389b185ca877645` |
| JVM | `hotel-pms/fb-service-jvm:ci` | `hotel-pms/fb-service-jvm:validated` | `hotel-pms/fb-service-jvm:83d5584aabd4c5b49e8121e79389b185ca877645` |

Checksums and identities have different meanings and must not be interchanged:

- **Tar.gz SHA-256**, locally recomputed and matched to `SHA256SUMS`: `8a854555682b1136e1a02c20e2343c5a47d7b9c5aa71eef01aab084d94256853`.
- **GitHub artifact ZIP digest**, recorded by GitHub's API: `sha256:af9460fb302d84ab095f8faec1817abb728fa5ab9a5773218ff0491754cb40ea`.
- **Native Docker image/config ID**: `sha256:ab3b953e085712be257608d0b7a8a630982967071e654fe180a82a32a72cf1f3`.
- **JVM Docker image/config ID**: `sha256:94cb0a50ddac948cf98e3db607f7ba2250f5c7de8938ee1edc3b30b7ea392c73`.

The tar's embedded `manifest.json` matches `docker-save-manifest.json`; each archived config's SHA-256 matches its Docker image ID, and those IDs match the images measured by the runtime gate. `gzip --test` and tar checksum verification passed. No local Docker load was performed. Both image metadata entries report `linux/amd64`; the Native compiler additionally targets x86-64-v3, so ARM64 and older x86 CPUs are not validated targets.

The artifact also contains `SHA256SUMS`, full image inspection metadata, the actual Docker save manifest, source/run provenance, and gate metrics. The tar is gzip-compressed once; [upload-artifact v4](https://github.com/actions/upload-artifact) uses `compression-level: 0` to avoid compressing it again. Retention is 30 days; copy the artifact to durable storage before expiry if longer retention is required. No registry push is performed.

Main's loader can download the exact artifact, then load both tested binaries without rebuilding. Run on a compatible Linux amd64 runner (from an empty destination directory):

```sh
gh run download 33829870882 --repo serviciosdigitalesmx/hotel-pms-mexico \
  --name fb-service-optimized-O2-images-83d5584aabd4c5b49e8121e79389b185ca877645
sha256sum --check SHA256SUMS
gzip --test fb-service-optimized-O2-83d5584aabd4c5b49e8121e79389b185ca877645.tar.gz
docker load --input fb-service-optimized-O2-83d5584aabd4c5b49e8121e79389b185ca877645.tar.gz
docker image inspect hotel-pms/fb-service-native:validated hotel-pms/fb-service-jvm:validated
```

For a loader using the REST artifact API, the artifact ID is `9921801599`; the archive contains both the Native and JVM modules, not one archive per runtime. Match the inspected IDs above after loading. Retagging/rebuilding is unnecessary; existing `:ci` tags remain included for gates.

Use network aliases `config-server`, `redis`, `postgres`, `frontdesk-service`, `billing-service`, and `fb-service` for the integrated stack. Application port is 8086; management is 8090. Supply runtime Config Server, HMAC, Redis and PostgreSQL credentials through the existing environment contract. Set `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/hotel_fb`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_PROFILES_ACTIVE=fb-service`, `CONFIG_SERVER_URL=http://config-server:8888`, `CONFIG_SERVER_PASSWORD`, `INTERNAL_HMAC_SECRET`, `INTERNAL_REDIS_PASSWORD`, and `SPRING_DATA_REDIS_PASSWORD`. Native Feign hosts baked by the proven AOT build are `frontdesk-service:8081` and `billing-service:8085`.

## Actual boundaries and remaining integration work

- This individual gate exercises F&B with real **JVM** downstream services. Main owns the final simultaneous all-Native and all-JVM stacks, frontend/gateway/auth E2E, and total stack RAM/startup measurements; those are not certified here.
- F&B authenticates internal HMAC headers and applies its existing RBAC. End-user JWT login/refresh belongs to Auth/Gateway and is outside this service gate.
- The existing JVM Dockerfile healthcheck targets application port 8086 while the configured management port is 8090. The actual final Docker state is **Native healthy / JVM unhealthy**, although both services' direct management probes and authenticated contracts passed. Main should override the JVM Compose healthcheck to `wget -q --spider http://localhost:8090/actuator/health || exit 1` when loading the existing JVM image. This docs-only closure does not change or rebuild that image; the old Docker healthcheck is a remaining integration limitation.
- Billing's current fallback may return null while the existing F&B contract transitions the order to `BILLED_TO_ROOM` during a downstream outage. The retained fallback test verifies that existing behavior, not successful charge delivery during an outage. The healthy-path test independently verifies the real persisted charge.
- F&B has no PDF route in this gate. Neither Billing PDF text/fonts nor Frontdesk PDF routes are validated here; Billing's PDF correction belongs to Main.
- Config Server profile and Prometheus endpoint are checked. This gate does not run an external Zipkin collector or assert span delivery.
- No global PMD/Vitest repair, merge, deployment, API/schema modification or security relaxation was performed in this revision.
