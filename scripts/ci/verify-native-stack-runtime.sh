#!/usr/bin/env bash
set -Eeuo pipefail
[[ "${GITHUB_ACTIONS:-}" == true ]] || { echo 'This integration gate only runs against isolated GitHub CI data.' >&2; exit 1; }
mode="${1:?native or jvm}"
[[ "$mode" == native || "$mode" == jvm ]]
: "${POSTGRES_PASSWORD:?}" "${REDIS_PASSWORD:?}" "${CONFIG_SERVER_PASSWORD:?}" "${INTERNAL_HMAC_SECRET:?}" "${JWT_SECRET:?}"
export NATIVE_STACK_MODE="$mode"
project="pms-native-ci-${GITHUB_RUN_ID:?}-$mode"
compose=(docker compose --project-name "$project" -f docker-compose.yml -f docker-compose.native-stack-ci.yml --profile observability)
result_dir="${GITHUB_WORKSPACE:?}/build/native-stack-runtime/$mode"
mkdir -p "$result_dir"
metrics="$result_dir/metrics.txt"
progress="$result_dir/progress.log"
services=(config-server api-gateway auth-service guest-service frontdesk-service billing-service fb-service notification-service)
management_ports=(18100 18101 18102 18103 18104 18105 18106 18107)
load_sampler_pid=''
current_phase='initialization'
phase() {
  current_phase="$*"
  printf '[%s] [%s] %s\n' "$(date -u +%Y-%m-%dT%H:%M:%SZ)" "${mode^^}" "$current_phase" | tee -a "$progress"
}
collect() {
  gate_exit=$?
  trap - EXIT
  failed_phase="$current_phase"
  phase "collect evidence after exit=$gate_exit phase=$failed_phase"
  if [[ -n "$load_sampler_pid" ]]; then
    kill "$load_sampler_pid" 2>/dev/null || true
    wait "$load_sampler_pid" 2>/dev/null || true
  fi
  timeout --signal=TERM --kill-after=5s 20s "${compose[@]}" ps --all > "$result_dir/containers.txt" 2>&1 || true
  for service in "${services[@]}" postgres redis frontend zipkin loki mailpit; do
    timeout --signal=TERM --kill-after=5s 20s "${compose[@]}" logs --no-color "$service" > "$result_dir/$service.log" 2>&1 || true
  done
  if [[ "$gate_exit" != 0 ]]; then
    printf '%s_STACK_GATE_FAIL phase=%s exit=%s\n' "${mode^^}" "$failed_phase" "$gate_exit" \
      > "$result_dir/failure-classification.txt"
  fi
  # These containers/volumes were created by this run. Volumes remain until the
  # disposable hosted runner is released; no workstation data is touched.
  timeout --signal=TERM --kill-after=10s 60s "${compose[@]}" down --timeout 20 >/dev/null 2>&1 || true
  exit "$gate_exit"
}
trap collect EXIT
sample_memory() {
  local stage="$1"
  mapfile -t backend_ids < <("${compose[@]}" ps -q "${services[@]}")
  [[ "${#backend_ids[@]}" == 8 ]]
  timeout --signal=TERM --kill-after=5s 30s docker stats --no-stream --format '{{json .}}' \
    "${backend_ids[@]}" > "$result_dir/backend-$stage-stats.jsonl"
  bytes="$(jq -s '
    map(.MemUsage | split(" / ")[0] | capture("(?<amount>[0-9.]+)(?<unit>[A-Za-z]+)") |
        (.amount | tonumber) * ({B:1,kB:1000,kiB:1024,KiB:1024,MB:1000000,MiB:1048576,GB:1000000000,GiB:1073741824}[.unit])) | add | round
  ' "$result_dir/backend-$stage-stats.jsonl")"
  echo "${mode}_backend_${stage}_memory_bytes=$bytes" >> "$metrics"
}
verify_hmac() {
  local service="$1" port="$2" method="$3" route="$4" accepted="$5"
  local nonce timestamp signature unsigned first replay
  nonce="$(openssl rand -hex 16)"
  timestamp="$(date +%s%3N)"
  signature="$(printf '%s' "ci-admin:ADMIN:99999999-9999-9999-9999-999999999999:$timestamp:$nonce" | openssl dgst -sha256 -hmac "$INTERNAL_HMAC_SECRET" -r | awk '{print $1}')"
  args=(-X "$method" -H 'Content-Type: application/json')
  if [[ "$method" == POST ]]; then args+=(-d '{}'); fi
  url="http://127.0.0.1:$port$route"
  unsigned="$(curl -sS --connect-timeout 3 --max-time 10 -o "$result_dir/$service-unsigned.json" \
    -w '%{http_code}' "${args[@]}" "$url")"
  [[ "$unsigned" == 401 ]]
  headers=(-H 'X-Auth-User: ci-admin' -H 'X-Auth-Role: ADMIN' -H 'X-Auth-Hotel: 99999999-9999-9999-9999-999999999999' -H "X-Auth-Timestamp: $timestamp" -H "X-Auth-Nonce: $nonce" -H "X-Internal-Signature: $signature")
  first="$(curl -sS --connect-timeout 3 --max-time 10 -o "$result_dir/$service-signed.json" \
    -w '%{http_code}' "${args[@]}" "${headers[@]}" "$url")"
  replay="$(curl -sS --connect-timeout 3 --max-time 10 -o "$result_dir/$service-replay.json" \
    -w '%{http_code}' "${args[@]}" "${headers[@]}" "$url")"
  [[ "$first" == "$accepted" && "$replay" == 401 ]]
  [[ "$(timeout --signal=TERM --kill-after=5s 20s "${compose[@]}" exec -T redis redis-cli \
    --no-auth-warning -a "$REDIS_PASSWORD" exists "internal-auth:nonce:$nonce")" == 1 ]]
  echo "${mode}_${service}_hmac_and_redis_replay=PASS" >> "$metrics"
}
# Exclude registry download time from both cold-start measurements. Each mode
# still initializes its own empty database and starts every container from zero.
phase 'pull shared infrastructure images'
timeout --signal=TERM --kill-after=30s 10m "${compose[@]}" pull postgres redis zipkin loki mailpit
phase 'start fresh integrated stack and wait for health'
started_ms="$(date +%s%3N)"
timeout --signal=TERM --kill-after=30s 12m "${compose[@]}" up -d --no-build --wait --wait-timeout 600 \
  postgres redis "${services[@]}" frontend zipkin loki mailpit
curl -fsS --connect-timeout 3 --max-time 10 http://127.0.0.1:18080/ >/dev/null
echo "${mode}_stack_available_ms=$(( $(date +%s%3N) - started_ms ))" > "$metrics"
phase 'verify Native/JVM PID1 identity, health and Prometheus'
for index in "${!services[@]}"; do
  service="${services[$index]}"
  container_id="$("${compose[@]}" ps -q "$service")"
  executable="$(docker exec "$container_id" readlink /proc/1/exe)"
  if [[ "$mode" == native ]]; then
    [[ "$executable" == /app/app ]]
  else
    [[ "$executable" == */java ]]
  fi
  docker inspect --format '{"image":{{json .Image}},"path":{{json .Path}},"state":{{json .State}},"restartCount":{{.RestartCount}}}' \
    "$container_id" > "$result_dir/$service-runtime-identity.json"
  echo "${mode}_${service}_pid1_executable=$executable" >> "$metrics"
  for suffix in '' /liveness /readiness; do
    curl -fsS --connect-timeout 3 --max-time 10 "http://127.0.0.1:${management_ports[$index]}/actuator/health$suffix" \
      | jq -e '.status == "UP"' >/dev/null
  done
  curl -fsS --connect-timeout 3 --max-time 10 \
    "http://127.0.0.1:${management_ports[$index]}/actuator/prometheus" > "$result_dir/$service-prometheus.txt"
  grep -Eq '^# (HELP|TYPE)' "$result_dir/$service-prometheus.txt"
  echo "${mode}_${service}_health_liveness_readiness_prometheus=PASS" >> "$metrics"
done
phase 'verify authenticated Config Server profiles'
for profile in auth-service api-gateway guest-service frontdesk-service billing-service fb-service notification-service; do
  curl -fsS --connect-timeout 3 --max-time 10 -u "configuser:$CONFIG_SERVER_PASSWORD" \
    "http://127.0.0.1:18888/$profile/default" \
    | jq -e --arg name "$profile" '.name == $name and (.propertySources | length) > 0' >/dev/null
done
config_unauthorized="$(curl -sS --connect-timeout 3 --max-time 10 -o "$result_dir/config-unauthorized.json" \
  -w '%{http_code}' http://127.0.0.1:18888/guest-service/default)"
[[ "$config_unauthorized" == 401 ]]
echo "${mode}_config_authenticated_profiles_and_denied_anonymous=PASS" >> "$metrics"
phase 'verify PostgreSQL and Flyway histories'
for database in hotel_auth hotel_guest hotel_frontdesk hotel_billing hotel_fb; do
  timeout --signal=TERM --kill-after=5s 30s "${compose[@]}" exec -T postgres \
    psql -U postgres -d "$database" -At -v ON_ERROR_STOP=1 \
    -c 'SELECT coalesce(json_agg(row_to_json(m) ORDER BY m.installed_rank), '\''[]'\''::json) FROM (SELECT installed_rank, version, description, type, script, checksum, success FROM flyway_schema_history) m;' \
    > "$result_dir/$database-flyway.json"
  jq -e 'length > 0 and all(.[]; .success == true)' "$result_dir/$database-flyway.json" >/dev/null
  echo "${mode}_${database}_postgresql_and_flyway=PASS" >> "$metrics"
done
phase 'measure idle memory and verify HMAC/Redis replay protection'
sample_memory idle
verify_hmac guest-service 18083 GET /api/v1/guests 200
verify_hmac frontdesk-service 18081 GET /api/v1/rooms 200
verify_hmac billing-service 18085 GET /api/v1/invoices 200
verify_hmac fb-service 18086 GET /api/v1/fb/menu-items 200
verify_hmac notification-service 18088 POST /internal/notifications/checkin 400
export PLAYWRIGHT_NATIVE_BASE_URL=http://127.0.0.1:18080
export NATIVE_E2E_OUTPUT_DIR="$result_dir/playwright"
export MAILPIT_BASE_URL=http://127.0.0.1:18025
# Stream all eight containers concurrently during real browser/API activity.
mapfile -t backend_ids < <("${compose[@]}" ps -q "${services[@]}")
docker stats --format '{{json .}}' "${backend_ids[@]}" > "$result_dir/backend-during-e2e-stats.jsonl" &
load_sampler_pid=$!
phase 'run three real Playwright frontend/API journeys'
(cd frontend && timeout --signal=TERM --kill-after=30s 15m npx playwright test --config playwright-native.config.ts)
kill "$load_sampler_pid" 2>/dev/null || true
wait "$load_sampler_pid" 2>/dev/null || true
load_sampler_pid=''
echo "${mode}_real_frontend_api_e2e=PASS" >> "$metrics"
sample_memory after_basic_use
peak_bytes="$(jq -s '
  def bytes: .MemUsage | split(" / ")[0] | capture("(?<amount>[0-9.]+)(?<unit>[A-Za-z]+)") |
    (.amount | tonumber) * ({B:1,kB:1000,kiB:1024,KiB:1024,MB:1000000,MiB:1048576,GB:1000000000,GiB:1073741824}[.unit]);
  group_by(.ID) | map(map(bytes) | max) | add | round
' "$result_dir/backend-during-e2e-stats.jsonl")"
echo "${mode}_backend_loaded_sum_per_container_peak_memory_bytes=$peak_bytes" >> "$metrics"
# Real PDFs must contain readable invoice text and embedded fonts, not just a
# successful HTTP status or %PDF header (a missing Native CMap can yield blanks).
phase 'verify browser and SMTP PDF text plus embedded fonts'
mapfile -d '' -t pdfs < <(find "$result_dir/playwright" -type f -name '*.pdf' -print0)
[[ "${#pdfs[@]}" -ge 2 ]] || { echo 'Missing invoice and SMTP attachment PDFs' >&2; exit 1; }
fixture_file="$(find "$result_dir/playwright/results" -type f -name fixture-identifiers.json -print -quit)"
[[ -n "$fixture_file" ]]
jq -e '.pdfExpectedText | length >= 5' "$fixture_file" >/dev/null
for pdf in "${pdfs[@]}"; do
  pdftotext -layout "$pdf" "$pdf.txt"
  while IFS= read -r expected_text; do
    grep -Fq "$expected_text" "$pdf.txt" || { echo "Missing PDF content: $expected_text" >&2; exit 1; }
  done < <(jq -r '.pdfExpectedText[]' "$fixture_file")
  pdffonts "$pdf" > "$pdf.fonts.txt"
  awk '/NotoSans/ && $(NF-4) == "yes" {embedded++} END {exit embedded < 1}' "$pdf.fonts.txt"
done
echo "${mode}_invoice_and_email_pdf_text_fonts=PASS" >> "$metrics"
# A five-minute post-flow soak checks all eight services without restarts/OOM.
phase 'start five-minute stability soak'
for round in {1..30}; do
  for index in "${!services[@]}"; do
    curl -fsS --max-time 3 "http://127.0.0.1:${management_ports[$index]}/actuator/health" | jq -e '.status == "UP"' >/dev/null
  done
  if (( round % 5 == 0 )); then phase "stability soak check $round/30 complete"; fi
  if (( round < 30 )); then sleep 10; fi
done
for service in "${services[@]}"; do
  container_id="$("${compose[@]}" ps -q "$service")"
  docker inspect "$container_id" | jq -e '.[0].RestartCount == 0 and .[0].State.OOMKilled == false and .[0].State.Running == true' >/dev/null
done
echo "${mode}_stability=8_services_30_checks_each_over_300s_zero_restarts_zero_oom" >> "$metrics"
phase 'verify Zipkin traces and Loki logs'
curl -fsS --connect-timeout 3 --max-time 15 http://127.0.0.1:19411/api/v2/services \
  > "$result_dir/zipkin-services.json"
for service in api-gateway auth-service guest-service frontdesk-service billing-service fb-service notification-service; do
  jq -e --arg service "$service" 'index($service) != null' "$result_dir/zipkin-services.json" >/dev/null
done
curl -fsS --connect-timeout 3 --max-time 15 -G http://127.0.0.1:13100/loki/api/v1/query_range \
  --data-urlencode 'query={service="notification-service"}' \
  --data-urlencode "start=${started_ms}000000" --data-urlencode "end=$(date +%s%N)" \
  --data-urlencode 'limit=1000' > "$result_dir/loki-notification.json"
jq -e '.status == "success" and (.data.result | length) > 0' "$result_dir/loki-notification.json" >/dev/null
echo "${mode}_zipkin_service_traces_and_loki_logs=PASS" >> "$metrics"
echo "${mode}_integrated_stack_gate=PASS" >> "$metrics"
phase 'integrated stack gate PASS'
cat "$metrics"
