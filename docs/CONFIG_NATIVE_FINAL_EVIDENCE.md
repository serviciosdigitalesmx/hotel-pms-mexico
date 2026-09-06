# Config Native: resource coverage, runtime gate and reusable images

## Scope and acceptance

Service: `config-service`; branch: `codex/config-native-direct`; PR: #20.
Evidence verified: 2026-09-04. Individual Config O2/JVM gate: PASS. Integrated
PMS stack: outside this report and still requires the coordinating task's gate.
This report covers Config only. Billing and the integrated stack are owned by
the coordinating task. PR #19 and the user's Desktop/dev checkouts are untouched.

Required evidence: every shipped service configuration and profile is served
with HTTP Basic authentication; invalid/absent/build credentials receive 401;
health, liveness and readiness remain UP; Prometheus serves real process metrics;
Native and JVM configuration bodies match; startup, idle and loaded memory and
image sizes are measured; sustained load passes; both images can be downloaded
and loaded without rebuilding. No database, API or security-policy changes.

## Why the old green run was insufficient

Downloaded artifact `9893015897` (`config-native-optimized-evidence`) from
[run 33753485524](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33753485524),
commit `f67ed4cbfe06583228f0d6ad3c0d5495dc02e83d`, shows:

- `native-authenticated.json` for `/auth-service/default` contains only
  `classpath:/config/application.yml`.
- `jvm-authenticated.json` also contains `classpath:/config/auth-service.yml`.
- The old gate required a nonempty `propertySources` array, so shared settings
  hid the absent service configuration. It did not assert content parity.
- There was no sustained load, loaded memory or downloadable image archive.

Consequently that run cannot establish complete Config Server functionality.
Its old startup values also timed the wait after both containers had launched;
they are not directly comparable to the new measurements.

## Narrow implementation

Commit `3be845ef43513e613507a13766b128a5c72cdf6f` registers the existing
`config/.*\.(yml|yaml)` resources in GraalVM metadata. No Java, YAML configuration,
Dockerfile, Gradle, security-filter or API behavior was changed.
Commit `a44f8fee350231be67e3218c53dd5bb00995a939` places that metadata at
`config-service/src/main/resources/META-INF/native-image/com.hotelpms/config-service-resources/resource-config.json`,
separate from Spring's generated AOT metadata. It also includes `bootJar` in the
cheap preflight and requires client secrets to remain unresolved placeholders.
The preceding attempt, run `33830013231`, failed during JVM packaging because
the initial metadata path collided with generated AOT output; no Native
compilation ran in that failed attempt.
Resource registration follows the
[GraalVM metadata contract](https://www.graalvm.org/jdk24/reference-manual/native-image/metadata/).

The runtime gate now derives its 39 cases from the eight packaged configuration
names (seven clients plus shared application configuration). It checks default,
dev, test and prod, plus each client's named deployment profile. The shipped
`api-gateway-prod.yml` must be present and its Swagger overrides must take effect.
Dev/test and service-named profiles inherit shared/base files where no override
file exists; this is checked inheritance, not invented configuration.

Every response must include its service-specific source and match the JVM
response, including source precedence and all property values. Both runtimes
receive a newly generated username/password after compilation; the build-only
credential is rejected. No runtime credential is stored in the evidence.

CI retains Gradle caches and Buildx `type=gha` caches. Each runner executes at
most one `nativeCompile`. A binary change runs tests/AOT, then the -Ob runtime
gate, then a separate -O2 build and complete runtime gate. The manual
`final_only=true` option is reserved for gate/workflow edits when the identical
binary has already passed O2. PR-triggered runs do not need a duplicate dispatch.

## Measurement and stability method

- Startup: monotonic wall time from issuing `docker run` to first management
  health UP, polling every 100 ms, with a 180-second startup deadline.
- Idle: Docker memory usage sampled after health UP and five seconds of settling,
  before the configuration matrix is exercised. Numeric bytes are included.
- Loaded: peak of approximately ten-second Docker memory samples while four
  concurrent clients continuously cycle through all 39 authenticated responses.
  Each response is compared with the initial full body, not just its HTTP status.
- Stability: at least 180 seconds per runtime, with repeated health/liveness/
  readiness/Prometheus checks, no failed requests, no container restart or OOM.
  Native and JVM load phases run sequentially on the same runner.
- Image size: local uncompressed Docker `.Size` for each tested image.
- Raw memory samples, full config responses, profile gates, stability records,
  process logs, final container states and metrics are retained as evidence.

## Image handoff contract

The successful O2 job saves BOTH tested images in `config-service-images.tar.gz`,
using fast gzip and `actions/upload-artifact@v4` with `compression-level: 0` and
30-day retention. It verifies gzip integrity, two manifest entries/six tags,
SHA-256 checksums and `docker load` before publishing.

The exact six tags included in this artifact are:

```text
hotel-pms/config-service-native:ci
hotel-pms/config-service-native:validated
hotel-pms/config-service-native:a44f8fee350231be67e3218c53dd5bb00995a939
hotel-pms/config-service-jvm:ci
hotel-pms/config-service-jvm:validated
hotel-pms/config-service-jvm:a44f8fee350231be67e3218c53dd5bb00995a939
```

Use the full source-SHA tag for the integrated manifest; `:validated` is the
convenience alias and `:ci` remains available for gates. A documentation-only
commit after this run does not change the tested binary SHA or image identity.

The archive also has image IDs/config metadata, manifest, provenance and metrics.
The evidence artifact records the downloadable image artifact's ID, URL and
GitHub artifact digest separately from the tar.gz checksum.

For the integrated stack, load the archive then run the chosen image with
network alias `config-server`, application port 8888, management port 8090,
and runtime `CONFIG_SERVER_USERNAME`/`CONFIG_SERVER_PASSWORD`. The classpath
configuration backend remains active. Client configuration keeps the existing
`redis`, service-name and other integration addresses/placeholders. Postgres,
Redis and client-service end-to-end behavior are outside this Config-only gate.

## Final execution and artifact verification

[Run 33830247446](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33830247446)
tested commit `a44f8fee350231be67e3218c53dd5bb00995a939`. All three jobs passed:
JVM tests/AOT/bootJar, Native -Ob, and Native -O2. The final job is
[`100894063502`](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33830247446/job/100894063502).
Its compiler log records `nativeQuickBuild=false` and one `nativeCompile`;
the unchanged Gradle build selects `-O2` in that mode. This run rebuilt the
corrected resource-bearing binary, rather than relying on the older incomplete
configuration artifact.

| Final O2 comparison | Native | JVM control |
| --- | ---: | ---: |
| Startup, ms | 616 | 3765 |
| Idle RAM, MiB | 60.27 | 217.3 |
| Idle RAM, bytes | 63197676 | 227855565 |
| Peak sampled loaded RAM, MiB | 98.85 | 283 |
| Peak sampled loaded RAM, bytes | 103651738 | 296747008 |
| Docker image size, bytes | 180867639 | 262590263 |
| Authenticated application/profile cases | 39 | 39 |
| Sustained load, seconds | 180.050 | 180.061 |
| Validated requests, four clients | 5299 | 5561 |
| Failed requests / restarts / OOM | 0 / 0 / false | 0 / 0 / false |
| Loaded memory samples | 19 | 19 |

Both runtimes returned 401 for absent, incorrect and build-only credentials
across all 39 cases. Health, liveness and readiness remained UP; Prometheus
returned real process metrics; `/actuator/info` required authentication. The
full Native/JVM configuration responses are byte-identical in the downloaded
JSON evidence. In particular, Native `/auth-service/default` now includes
`classpath:/config/auth-service.yml`; the production Gateway override and
unresolved client-secret placeholders passed their assertions.

| Artifact | ID | Contents |
| --- | --- | --- |
| [Native O2 + JVM images](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33830247446/artifacts/9921959128) | `9921959128` | Paired image tar.gz, checksums, manifest, metadata, provenance, metrics |
| [Final O2 evidence](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33830247446/artifacts/9921959629) | `9921959629` | Profile/management gates, full response parity, stability, memory samples, logs and image artifact identity |
| [Quick -Ob evidence](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33830247446/artifacts/9921691699) | `9921691699` | Preceding successful quick runtime gate |

The paired artifact name is
`config-native-optimized-images-a44f8fee350231be67e3218c53dd5bb00995a939`.
Its GitHub ZIP size is 199579188 bytes; the API reports it unexpired, with expiry
2026-10-04 03:04:58 UTC. Both images are `linux/amd64`.

Actual Docker image IDs (not archive checksums):

```text
Native sha256:62b6d0969ead2482927cef034923899f6b92eea5f21ce2df06788750f617d170
JVM    sha256:91ff195f15de70de35bfc9cee2e943be44b09468dd611b32e6e332e40f382f6c
```

The GitHub artifact ZIP digest is
`sha256:8755f09837a332883e78dccd71ba4cef0819f9df23a85287d646586f3183f4e1`.
It is distinct from the checksum of the enclosed Docker archive. The artifact's
`SHA256SUMS` contains:

```text
31a3b4993c1ad3d28de0149ead9fc1e838da51d41102e070efc77ad5ecfc941b  config-service-images.tar.gz
d55d4237b9535df00346822b09024c8fb4577ab4dac1d081ee2b15542069e580  image-metadata.json
09248a1a86fa5a59b651bada4df328aa2d9e9c2b978008ced613a9ef31059a2a  manifest.json
f2cf57ebd4514c0e18f0cda2e85b4840268cc727307669ffd79d4ae05e08d18a  provenance.json
b81615dc87bff51d32a5cd19ee11f094b750e48f08083915d680b8dd6ead4f31  metrics.json
```

The O2 job log explicitly confirms all six `Loaded image:` tags and all five
checksum checks as `OK`, followed by artifact ID `9921959128`. For the closing
review, the downloaded evidence at `/tmp/pms-final-evidence.lbZiDy/config` was
checked against its recorded metadata/provenance/metrics hashes; all 39 security
records per runtime, full response parity and final running/non-OOM states were
also rechecked. No local Docker runtime or Native rebuild was used for closure.

Download and load on the integrated Linux Docker runner, without rebuilding:

```bash
gh run download 33830247446 -R serviciosdigitalesmx/hotel-pms-mexico \
  -n config-native-optimized-images-a44f8fee350231be67e3218c53dd5bb00995a939 \
  -D config-validated
cd config-validated
sha256sum --check SHA256SUMS
gzip -dc config-service-images.tar.gz | docker load
```

The parent task owns the integrated manifest and stack gate. No merge was made.

## Actual limits

This service gate does not establish integrated all-Native or all-JVM PMS E2E,
total backend memory, or Native client boot compatibility in the final stack.
The coordinating task must use the exported pair for those tests. Three minutes
of sustained load is a bounded CI check, not a long-duration production soak.
Config has no PDF route, database persistence or tenant-aware business API.
