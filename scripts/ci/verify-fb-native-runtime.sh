#!/usr/bin/env bash
set -Eeuo pipefail

RESULT_DIR="${GITHUB_WORKSPACE:-$(pwd)}/${NATIVE_METRICS_DIR:-build/fb-native-runtime}"
NETWORK_NAME="fb-native-ci"
POSTGRES_CONTAINER="fb-native-postgres"
REDIS_CONTAINER="fb-native-redis"
CONFIG_CONTAINER="fb-native-config"
FRONTDESK_CONTAINER="fb-native-frontdesk"
BILLING_CONTAINER="fb-native-billing"
FB_CONTAINER="fb-native-service"
JVM_FB_CONTAINER="fb-jvm-service"
FB_IMAGE="${FB_IMAGE:-hotel-pms/fb-service-native:ci}"
JVM_FB_IMAGE="hotel-pms/fb-service-jvm:ci"
LOAD_SECONDS=120
STABILITY_SECONDS=300

: "${CI_POSTGRES_PASSWORD:?CI_POSTGRES_PASSWORD is required}"
: "${CI_REDIS_PASSWORD:?CI_REDIS_PASSWORD is required}"
: "${CI_CONFIG_PASSWORD:?CI_CONFIG_PASSWORD is required}"
: "${CI_HMAC_SECRET:?CI_HMAC_SECRET is required}"

mkdir -p "${RESULT_DIR}"
: > "${RESULT_DIR}/metrics.txt"

collect_evidence() {
  local result=$?
  if [[ "$result" -ne 0 ]]; then
    printf 'native_runtime_gate=NATIVE_GATE_FAIL\nexit_code=%s\n' "$result" >> "${RESULT_DIR}/metrics.txt"
  fi
  docker ps -a > "${RESULT_DIR}/docker-ps.txt" 2>&1 || true
  for container in "${CONFIG_CONTAINER}" "${POSTGRES_CONTAINER}" "${REDIS_CONTAINER}" \
      "${FRONTDESK_CONTAINER}" "${BILLING_CONTAINER}" "${FB_CONTAINER}" "${JVM_FB_CONTAINER}"; do
    docker logs "${container}" > "${RESULT_DIR}/${container}.log" 2>&1 || true
    docker inspect --format '{{json .State}}' "${container}" > "${RESULT_DIR}/${container}-state.json" 2>/dev/null || true
  done
}
trap collect_evidence EXIT

metric() {
  printf '%s\n' "$1" >> "${RESULT_DIR}/metrics.txt"
}

wait_for_health() {
  local label="$1" url="$2" attempts="${3:-90}" container="${4:-}" interval="${5:-2}"
  local response container_state
  local i
  for ((i = 1; i <= attempts; i++)); do
    response="$(curl --silent --show-error --max-time 3 "${url}" 2>/dev/null || true)"
    if jq -e '.status == "UP"' >/dev/null 2>&1 <<<"${response}"; then
      metric "${label}_health=UP"
      return 0
    fi
    if [[ -n "${container}" ]]; then
      container_state="$(docker inspect --format '{{.State.Status}}' "${container}" 2>/dev/null || true)"
      if [[ "${container_state}" == "exited" || "${container_state}" == "dead" ]]; then
        echo "${label} stopped before becoming healthy" >&2
        return 1
      fi
    fi
    sleep "$interval"
  done
  echo "${label} did not become healthy at ${url}" >&2
  return 1
}

signed_request() {
  local method="$1" url="$2" hotel_id="$3" nonce="$4" output_file="$5" body="${6:-}"
  local timestamp signature role="${7:-ADMIN}"
  timestamp="$(date +%s%3N)"
  signature="$(printf '%s' "ci-admin:${role}:${hotel_id}:${timestamp}:${nonce}" \
    | openssl dgst -sha256 -hmac "${CI_HMAC_SECRET}" -r | awk '{print $1}')"
  local curl_args=(
    --silent --show-error --max-time 15 --output "${output_file}" --write-out '%{http_code}'
    --request "${method}"
    --header "X-Auth-User: ci-admin"
    --header "X-Auth-Role: ${role}"
    --header "X-Auth-Hotel: ${hotel_id}"
    --header "X-Auth-Timestamp: ${timestamp}"
    --header "X-Auth-Nonce: ${nonce}"
    --header "X-Internal-Signature: ${signature}"
  )
  if [[ -n "${body}" ]]; then
    curl_args+=(--header 'Content-Type: application/json' --data "${body}")
  fi
  curl "${curl_args[@]}" "${url}"
}

assert_code() {
  local expected="$1" actual="$2" description="$3"
  if [[ "${actual}" != "${expected}" ]]; then
    echo "${description}: expected HTTP ${expected}, got ${actual}" >&2
    return 1
  fi
  metric "${description// /_}=HTTP_${actual}"
}

new_id() {
  uuidgen | tr '[:upper:]' '[:lower:]'
}

# Exact bytes, same cgroup-v2 working-set convention as docker stats on Linux:
# memory.current minus inactive_file. Keep raw counters for independent review.
memory_bytes() {
  local label="$1" phase="$2" container="$3" current inactive working
  current="$(docker exec "$container" cat /sys/fs/cgroup/memory.current)"
  inactive="$(docker exec "$container" awk '$1 == "inactive_file" {print $2}' /sys/fs/cgroup/memory.stat)"
  [[ "$current" =~ ^[0-9]+$ && "$inactive" =~ ^[0-9]+$ ]]
  working=$((current - inactive))
  [[ "$working" -gt 0 ]]
  printf '%s,%s,%s,%s,%s,%s\n' "$(date +%s%3N)" "$label" "$phase" "$current" "$inactive" "$working" \
    >> "${RESULT_DIR}/memory-samples.csv"
  printf '%s\n' "$working"
}

check_probes() {
  local label="$1" port="$2" probe
  for probe in health health/liveness health/readiness; do
    curl --silent --show-error --fail --max-time 5 "http://127.0.0.1:${port}/actuator/${probe}" \
      > "${RESULT_DIR}/${label}-${probe//\//-}.json"
    jq -e '.status == "UP"' "${RESULT_DIR}/${label}-${probe//\//-}.json" >/dev/null
  done
}

measure_startup_idle() {
  local label="$1" container="$2" port="$3" started i idle=()
  docker stop "$container" >/dev/null
  started="$(date +%s%3N)"
  docker start "$container" >/dev/null
  wait_for_health "${label}_measured" "http://127.0.0.1:${port}/actuator/health" 1200 "$container" 0.2
  check_probes "$label" "$port"
  metric "${label}_startup_ms=$(( $(date +%s%3N) - started ))"
  sleep 15
  for i in {1..5}; do
    idle+=("$(memory_bytes "$label" idle "$container")")
    sleep 1
  done
  metric "${label}_idle_memory=$(printf '%s\n' "${idle[@]}" | sort -n | sed -n '3p')"
}

load_worker() {
  local label="$1" port="$2" worker="$3" deadline="$4" count=0 code
  local response="${RESULT_DIR}/load-${label}-${worker}.json"
  while [[ "$(date +%s)" -lt "$deadline" ]]; do
    code="$(signed_request GET "http://127.0.0.1:${port}/api/v1/fb/menu-items" "$HOTEL_A" "$(new_id)" "$response")"
    [[ "$code" == 200 ]]
    jq -e --arg id "$MENU_ID" 'any(.[]; .id == $id and .price == 12.5)' "$response" >/dev/null
    count=$((count + 1))
  done
  printf '%s\n' "$count" > "${RESULT_DIR}/load-${label}-${worker}-count.txt"
}

metric "source_commit=$(git rev-parse HEAD)"
metric 'memory_unit=bytes'
metric 'memory_method=cgroup_v2_memory.current_minus_inactive_file'
metric 'startup_method=container_start_through_health_liveness_readiness_after_schema_migration'
metric 'idle_method=median_5_samples_after_15_seconds_quiet'
metric 'loaded_method=peak_working_set_during_120_seconds_4_concurrent_clients_per_runtime'
printf 'timestamp_ms,runtime,phase,memory_current_bytes,inactive_file_bytes,working_set_bytes\n' > "${RESULT_DIR}/memory-samples.csv"
metric "native_image_size_bytes=$(docker image inspect "$FB_IMAGE" --format '{{.Size}}')"
metric "jvm_image_size_bytes=$(docker image inspect "$JVM_FB_IMAGE" --format '{{.Size}}')"
docker image inspect "$FB_IMAGE" "$JVM_FB_IMAGE" > "${RESULT_DIR}/image-metadata.json"

echo 'Starting Config Server, PostgreSQL and Redis'
docker network create "${NETWORK_NAME}" >/dev/null
docker run --detach --name "${CONFIG_CONTAINER}" --network "${NETWORK_NAME}" \
  --network-alias config-server --publish 18888:8888 --publish 18091:8090 \
  --env "CONFIG_SERVER_PASSWORD=${CI_CONFIG_PASSWORD}" \
  --env 'JAVA_TOOL_OPTIONS=-Xmx256m -XX:MaxMetaspaceSize=128m -XX:+ExitOnOutOfMemoryError' \
  hotel-pms/config-service:ci >/dev/null
wait_for_health config-server http://127.0.0.1:18091/actuator/health 90 "${CONFIG_CONTAINER}"
curl --silent --show-error --fail --user "configuser:${CI_CONFIG_PASSWORD}" \
  http://127.0.0.1:18888/fb-service/default | jq -e '.name == "fb-service"' >/dev/null
metric 'config_server_fb_profile=AVAILABLE'

docker run --detach --name "${POSTGRES_CONTAINER}" --network "${NETWORK_NAME}" \
  --network-alias postgres \
  --env POSTGRES_USER=postgres --env "POSTGRES_PASSWORD=${CI_POSTGRES_PASSWORD}" \
  postgres:15-alpine >/dev/null
docker run --detach --name "${REDIS_CONTAINER}" --network "${NETWORK_NAME}" \
  --network-alias redis redis:8.8.1-alpine redis-server --requirepass "${CI_REDIS_PASSWORD}" >/dev/null

postgres_ready_streak=0
for _ in {1..60}; do
  if docker exec "${POSTGRES_CONTAINER}" pg_isready --username postgres >/dev/null 2>&1 \
      && docker exec "${POSTGRES_CONTAINER}" psql --username postgres --dbname postgres \
        --tuples-only --no-align --command 'select 1;' 2>/dev/null | grep -qx 1; then
    postgres_ready_streak=$((postgres_ready_streak + 1))
    [[ "${postgres_ready_streak}" -ge 2 ]] && break
  else
    postgres_ready_streak=0
  fi
  sleep 2
done
if [[ "${postgres_ready_streak}" -lt 2 ]]; then
  echo 'PostgreSQL did not remain ready for two consecutive checks' >&2
  exit 1
fi
for database in hotel_fb hotel_frontdesk hotel_billing; do
  docker exec "${POSTGRES_CONTAINER}" createdb --username postgres "${database}"
done
if ! docker exec "${REDIS_CONTAINER}" redis-cli --pass "${CI_REDIS_PASSWORD}" ping 2>/dev/null | grep -qx PONG; then
  echo 'Redis did not accept the configured password' >&2
  exit 1
fi
metric 'postgres_redis=READY'

docker run --detach --name "${FRONTDESK_CONTAINER}" --network "${NETWORK_NAME}" \
  --network-alias frontdesk-service --publish 18081:8081 --publish 18082:8090 \
  --env 'SPRING_PROFILES_ACTIVE=frontdesk-service' \
  --env 'CONFIG_SERVER_URL=http://config-server:8888' \
  --env "CONFIG_SERVER_PASSWORD=${CI_CONFIG_PASSWORD}" \
  --env "INTERNAL_HMAC_SECRET=${CI_HMAC_SECRET}" \
  --env "INTERNAL_REDIS_PASSWORD=${CI_REDIS_PASSWORD}" \
  --env "SPRING_DATA_REDIS_PASSWORD=${CI_REDIS_PASSWORD}" \
  --env 'SPRING_DATASOURCE_URL=jdbc:postgresql://fb-native-postgres:5432/hotel_frontdesk' \
  --env 'SPRING_DATASOURCE_USERNAME=postgres' \
  --env "SPRING_DATASOURCE_PASSWORD=${CI_POSTGRES_PASSWORD}" \
  --env 'ALLOGGIATI_DRY_RUN=true' \
  --env 'ALLOGGIATI_USERNAME=ci-placeholder' --env 'ALLOGGIATI_PASSWORD=ci-placeholder' \
  --env 'ALLOGGIATI_WS_KEY=ci-placeholder' \
  --env 'ALLOGGIATI_CREDENTIALS_ENCRYPTION_KEY=ci-placeholder-encryption-key' \
  --env 'ALLOGGIATI_CREDENTIALS_ENCRYPTION_SALT=deadbeefdeadbeefdeadbeefdeadbeef' \
  hotel-pms/frontdesk-service:ci >/dev/null

docker run --detach --name "${BILLING_CONTAINER}" --network "${NETWORK_NAME}" \
  --network-alias billing-service --publish 18085:8085 --publish 18086:8090 \
  --env 'SPRING_PROFILES_ACTIVE=billing-service' \
  --env 'CONFIG_SERVER_URL=http://config-server:8888' \
  --env "CONFIG_SERVER_PASSWORD=${CI_CONFIG_PASSWORD}" \
  --env "INTERNAL_HMAC_SECRET=${CI_HMAC_SECRET}" \
  --env "INTERNAL_REDIS_PASSWORD=${CI_REDIS_PASSWORD}" \
  --env "SPRING_DATA_REDIS_PASSWORD=${CI_REDIS_PASSWORD}" \
  --env 'SPRING_DATASOURCE_URL=jdbc:postgresql://fb-native-postgres:5432/hotel_billing' \
  --env 'SPRING_DATASOURCE_USERNAME=postgres' \
  --env "SPRING_DATASOURCE_PASSWORD=${CI_POSTGRES_PASSWORD}" \
  hotel-pms/billing-service:ci >/dev/null

common_fb_env=(
  --env 'SPRING_PROFILES_ACTIVE=fb-service'
  --env 'CONFIG_SERVER_URL=http://config-server:8888'
  --env "CONFIG_SERVER_PASSWORD=${CI_CONFIG_PASSWORD}"
  --env "INTERNAL_HMAC_SECRET=${CI_HMAC_SECRET}"
  --env "INTERNAL_REDIS_PASSWORD=${CI_REDIS_PASSWORD}"
  --env "SPRING_DATA_REDIS_PASSWORD=${CI_REDIS_PASSWORD}"
  --env 'APPLICATION_CONFIG_FRONTDESK_SERVICE_URL=http://frontdesk-service:8081'
  --env 'APPLICATION_CONFIG_BILLING_SERVICE_URL=http://billing-service:8085'
  --env 'SPRING_DATASOURCE_URL=jdbc:postgresql://fb-native-postgres:5432/hotel_fb'
  --env 'SPRING_DATASOURCE_USERNAME=postgres'
  --env "SPRING_DATASOURCE_PASSWORD=${CI_POSTGRES_PASSWORD}"
)
docker run --detach --name "${FB_CONTAINER}" --network "${NETWORK_NAME}" \
  --network-alias fb-service \
  --publish 18087:8086 --publish 18088:8090 \
  "${common_fb_env[@]}" "${FB_IMAGE}" >/dev/null
docker run --detach --name "${JVM_FB_CONTAINER}" --network "${NETWORK_NAME}" \
  --publish 18089:8086 --publish 18090:8090 \
  "${common_fb_env[@]}" "$JVM_FB_IMAGE" >/dev/null

wait_for_health frontdesk-service http://127.0.0.1:18082/actuator/health 120 "${FRONTDESK_CONTAINER}"
wait_for_health billing-service http://127.0.0.1:18086/actuator/health 120 "${BILLING_CONTAINER}"
wait_for_health fb-service-native http://127.0.0.1:18088/actuator/health 120 "${FB_CONTAINER}"
wait_for_health fb-service-jvm http://127.0.0.1:18090/actuator/health 120 "${JVM_FB_CONTAINER}"
metric "native_build_mode=${CI_NATIVE_BUILD_MODE:-unspecified}"
metric "native_image=${FB_IMAGE}"
metric 'jvm_fallback_image=hotel-pms/fb-service-jvm:ci'

# Both measurements use the same already-migrated database, ready downstream
# services, runtime configuration, readiness criteria and quiet interval.
measure_startup_idle native "$FB_CONTAINER" 18088
measure_startup_idle jvm "$JVM_FB_CONTAINER" 18090
for runtime in native jvm; do
  port=18088
  [[ "$runtime" == jvm ]] && port=18090
  curl --silent --show-error --fail --max-time 5 "http://127.0.0.1:${port}/actuator/prometheus" \
    > "${RESULT_DIR}/${runtime}-prometheus.txt"
  grep -q '^process_uptime_seconds' "${RESULT_DIR}/${runtime}-prometheus.txt"
  metric "${runtime}_liveness_readiness_prometheus=VERIFIED"
done
docker exec "$POSTGRES_CONTAINER" psql --username postgres --dbname hotel_fb --tuples-only --no-align \
  --command 'SELECT count(*) > 0 AND bool_and(success) FROM flyway_schema_history;' \
  | grep -qx t
docker exec "$POSTGRES_CONTAINER" psql --username postgres --dbname hotel_fb --csv \
  --command 'SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;' \
  > "${RESULT_DIR}/flyway-history.csv"
metric 'postgres_flyway=VERIFIED'

HOTEL_A="$(new_id)"
HOTEL_B="$(new_id)"
GUEST_ID="$(new_id)"
ROOM_TYPE_ID="$(new_id)"
ROOM_ID="$(new_id)"
RESERVATION_ID="$(new_id)"
STAY_ID="$(new_id)"

echo 'Seeding a real CHECKED_IN stay in frontdesk PostgreSQL'
docker exec -i "${POSTGRES_CONTAINER}" psql --username postgres --dbname hotel_frontdesk \
  --set ON_ERROR_STOP=1 <<SQL
INSERT INTO room_types (id, hotel_id, name, description, max_occupancy, base_price, active, created_at, updated_at)
VALUES ('${ROOM_TYPE_ID}', '${HOTEL_A}', 'Native CI Room Type', 'Native integration fixture', 2, 100.00, true, now(), now());
INSERT INTO rooms (id, hotel_id, room_number, room_type_id, status, active, created_at, updated_at)
VALUES ('${ROOM_ID}', '${HOTEL_A}', 'NATIVE-${ROOM_ID:0:8}', '${ROOM_TYPE_ID}', 'OCCUPIED', true, now(), now());
INSERT INTO reservations (id, version, hotel_id, guest_id, expected_guests, actual_guests,
    check_in_date, check_out_date, status, active, created_at, updated_at)
VALUES ('${RESERVATION_ID}', 0, '${HOTEL_A}', '${GUEST_ID}', 1, 1,
    CURRENT_DATE, CURRENT_DATE + 1, 'CHECKED_IN', true, now(), now());
INSERT INTO reservation_line_items (id, reservation_id, room_id, price, active, created_at, updated_at)
VALUES (gen_random_uuid(), '${RESERVATION_ID}', '${ROOM_ID}', 100.00, true, now(), now());
INSERT INTO stays (id, hotel_id, reservation_id, guest_id, room_id, status, actual_check_in_time,
    expected_check_out_date, guest_display_name, room_number, active, created_at, updated_at, occupant_count)
VALUES ('${STAY_ID}', '${HOTEL_A}', '${RESERVATION_ID}', '${GUEST_ID}', '${ROOM_ID}', 'CHECKED_IN',
    now(), CURRENT_DATE + 1, 'Native CI Guest', 'NATIVE-${ROOM_ID:0:8}', true, now(), now(), 1);
SQL

echo 'Creating the real Billing invoice used by F&B confirmation'
invoice_file="${RESULT_DIR}/invoice-create.json"
invoice_status="$(signed_request POST http://127.0.0.1:18085/api/v1/invoices/stay "${HOTEL_A}" "$(new_id)" "${invoice_file}" \
  "{\"stayId\":\"${STAY_ID}\",\"guestId\":\"${GUEST_ID}\",\"reservationId\":\"${RESERVATION_ID}\"}")"
assert_code 201 "${invoice_status}" billing_invoice_create
INVOICE_ID="$(jq -er '.id' "${invoice_file}")"

native_menu_file="${RESULT_DIR}/native-menu-create.json"
menu_status="$(signed_request POST http://127.0.0.1:18087/api/v1/fb/menu-items "${HOTEL_A}" "$(new_id)" \
  "${native_menu_file}" '{"name":"Native Breakfast","price":12.50,"category":"Breakfast","description":"Native gate item","available":true}')"
assert_code 201 "${menu_status}" native_menu_create
MENU_ID="$(jq -er '.id' "${native_menu_file}")"
jq -e '.price == 12.5 or .price == 12.50 or .price == "12.50"' "${native_menu_file}" >/dev/null
metric 'native_menu_server_price=VERIFIED'

native_menu_list="${RESULT_DIR}/native-menu-list.json"
menu_list_status="$(signed_request GET http://127.0.0.1:18087/api/v1/fb/menu-items "${HOTEL_A}" "$(new_id)" "${native_menu_list}")"
assert_code 200 "${menu_list_status}" native_menu_list
jq -e --arg id "${MENU_ID}" 'map(select(.id == $id)) | length == 1' "${native_menu_list}" >/dev/null

other_menu_list="${RESULT_DIR}/other-tenant-menu-list.json"
other_menu_status="$(signed_request GET http://127.0.0.1:18087/api/v1/fb/menu-items "${HOTEL_B}" "$(new_id)" "${other_menu_list}")"
assert_code 200 "${other_menu_status}" other_tenant_menu_list
jq -e 'length == 0' "${other_menu_list}" >/dev/null
metric 'tenant_menu_isolation=VERIFIED'

jvm_menu_list="${RESULT_DIR}/jvm-menu-list.json"
jvm_menu_status="$(signed_request GET http://127.0.0.1:18089/api/v1/fb/menu-items "${HOTEL_A}" "$(new_id)" "${jvm_menu_list}")"
assert_code 200 "${jvm_menu_status}" jvm_fallback_menu_list
jq -e --arg id "${MENU_ID}" 'map(select(.id == $id)) | length == 1' "${jvm_menu_list}" >/dev/null
metric 'jvm_fallback_contract=VERIFIED'

order_file="${RESULT_DIR}/native-order-create.json"
order_status="$(signed_request POST http://127.0.0.1:18087/api/v1/fb/orders "${HOTEL_A}" "$(new_id)" \
  "${order_file}" "{\"stayId\":\"${STAY_ID}\",\"items\":[{\"menuItemId\":\"${MENU_ID}\",\"quantity\":2}]}")"
assert_code 201 "${order_status}" native_order_create
ORDER_ID="$(jq -er '.id' "${order_file}")"
jq -e '.totalAmount == 25 or .totalAmount == 25.0 or .totalAmount == "25.00"' "${order_file}" >/dev/null
metric 'restaurant_order_server_total=VERIFIED'

confirm_file="${RESULT_DIR}/native-order-confirm.json"
confirm_status="$(signed_request POST "http://127.0.0.1:18087/api/v1/fb/orders/${ORDER_ID}/confirm" "${HOTEL_A}" "$(new_id)" "${confirm_file}")"
assert_code 200 "${confirm_status}" native_order_confirm
jq -e '.status == "BILLED_TO_ROOM"' "${confirm_file}" >/dev/null

billing_invoice_file="${RESULT_DIR}/billing-invoice-after-fb.json"
billing_invoice_status="$(signed_request GET "http://127.0.0.1:18085/api/v1/invoices/${INVOICE_ID}" "${HOTEL_A}" "$(new_id)" "${billing_invoice_file}")"
assert_code 200 "${billing_invoice_status}" billing_invoice_read
jq -e --arg order "${ORDER_ID}" 'any(.charges[]; .type == "FB_ORDER" and .referenceId == $order and (.amount == 25 or .amount == 25.0 or .amount == "25.00"))' "${billing_invoice_file}" >/dev/null
metric 'feign_billing_real_charge=VERIFIED'
metric 'feign_frontdesk_real_checked_in=VERIFIED'

other_order_file="${RESULT_DIR}/other-tenant-order.json"
other_order_status="$(signed_request POST http://127.0.0.1:18087/api/v1/fb/orders "${HOTEL_B}" "$(new_id)" \
  "${other_order_file}" "{\"stayId\":\"${STAY_ID}\",\"items\":[{\"menuItemId\":\"${MENU_ID}\",\"quantity\":1}]}")"
assert_code 404 "${other_order_status}" other_tenant_order_rejected
metric 'tenant_order_isolation=VERIFIED'

jvm_order_status="$(signed_request POST http://127.0.0.1:18089/api/v1/fb/orders "$HOTEL_A" "$(new_id)" \
  "${RESULT_DIR}/jvm-order-create.json" "{\"stayId\":\"${STAY_ID}\",\"items\":[{\"menuItemId\":\"${MENU_ID}\",\"quantity\":1}]}")"
assert_code 201 "$jvm_order_status" jvm_order_create
JVM_ORDER_ID="$(jq -er '.id' "${RESULT_DIR}/jvm-order-create.json")"
jvm_confirm_status="$(signed_request POST "http://127.0.0.1:18089/api/v1/fb/orders/${JVM_ORDER_ID}/confirm" "$HOTEL_A" "$(new_id)" \
  "${RESULT_DIR}/jvm-order-confirm.json")"
assert_code 200 "$jvm_confirm_status" jvm_order_confirm
jq -e '.status == "BILLED_TO_ROOM"' "${RESULT_DIR}/jvm-order-confirm.json" >/dev/null
jvm_invoice_status="$(signed_request GET "http://127.0.0.1:18085/api/v1/invoices/${INVOICE_ID}" "$HOTEL_A" "$(new_id)" \
  "${RESULT_DIR}/billing-invoice-after-jvm.json")"
assert_code 200 "$jvm_invoice_status" jvm_billing_invoice_read
jq -e --arg order "$JVM_ORDER_ID" 'any(.charges[]; .type == "FB_ORDER" and .referenceId == $order and .amount == 12.5)' \
  "${RESULT_DIR}/billing-invoice-after-jvm.json" >/dev/null
metric 'jvm_feign_frontdesk_billing=VERIFIED'

# Apply equivalent sustained authenticated reads to both runtimes before
# deliberately stopping downstream services for the existing fallback checks.
echo 'Measuring Native/JVM loaded RAM with four clients per runtime for 120 seconds'
load_deadline=$(( $(date +%s) + LOAD_SECONDS ))
load_pids=()
for worker in {1..4}; do
  load_worker native 18087 "$worker" "$load_deadline" &
  load_pids+=("$!")
  load_worker jvm 18089 "$worker" "$load_deadline" &
  load_pids+=("$!")
done
native_peak=0
jvm_peak=0
while [[ "$(date +%s)" -lt "$load_deadline" ]]; do
  native_sample="$(memory_bytes native loaded "$FB_CONTAINER")"
  jvm_sample="$(memory_bytes jvm loaded "$JVM_FB_CONTAINER")"
  (( native_sample > native_peak )) && native_peak="$native_sample"
  (( jvm_sample > jvm_peak )) && jvm_peak="$jvm_sample"
  sleep 2
done
for pid in "${load_pids[@]}"; do wait "$pid"; done
metric "native_loaded_memory=$native_peak"
metric "jvm_loaded_memory=$jvm_peak"
metric "load_seconds=$LOAD_SECONDS"
metric 'load_concurrent_clients_per_runtime=4'
for runtime in native jvm; do
  requests="$(awk '{sum += $1} END {print sum}' "${RESULT_DIR}"/load-"${runtime}"-*-count.txt)"
  [[ "$requests" -gt 0 ]]
  metric "${runtime}_load_successful_requests=$requests"
  metric "${runtime}_load_failed_requests=0"
done

echo 'Checking health, authenticated contracts and tenant isolation continuously for five minutes'
stability_start="$(date +%s)"
stability_samples=0
while [[ "$(( $(date +%s) - stability_start ))" -lt "$STABILITY_SECONDS" ]]; do
  for runtime in native jvm; do
    port=18087
    management_port=18088
    [[ "$runtime" == jvm ]] && { port=18089; management_port=18090; }
    check_probes "$runtime" "$management_port"
    code="$(signed_request GET "http://127.0.0.1:${port}/api/v1/fb/menu-items" "$HOTEL_A" "$(new_id)" "${RESULT_DIR}/${runtime}-stability-menu.json")"
    [[ "$code" == 200 ]]
    jq -e --arg id "$MENU_ID" 'any(.[]; .id == $id and .price == 12.5)' "${RESULT_DIR}/${runtime}-stability-menu.json" >/dev/null
    code="$(signed_request GET "http://127.0.0.1:${port}/api/v1/fb/menu-items" "$HOTEL_B" "$(new_id)" "${RESULT_DIR}/${runtime}-stability-tenant-b.json")"
    [[ "$code" == 200 ]]
    jq -e 'length == 0' "${RESULT_DIR}/${runtime}-stability-tenant-b.json" >/dev/null
    memory_bytes "$runtime" stability "fb-${runtime}-service" >/dev/null
  done
  stability_samples=$((stability_samples + 1))
  printf '%s,%s,UP,UP\n' "$(date +%s%3N)" "$stability_samples" >> "${RESULT_DIR}/stability-samples.csv"
  sleep 5
done
metric "stability_seconds=$(( $(date +%s) - stability_start ))"
metric "stability_samples=$stability_samples"
metric 'native_stability=PASS'
metric 'jvm_stability=PASS'

docker exec "$POSTGRES_CONTAINER" psql --username postgres --dbname hotel_fb --tuples-only --no-align \
  --command "SELECT count(*) FROM restaurant_orders WHERE id = '${ORDER_ID}' AND hotel_id = '${HOTEL_A}' AND status = 'BILLED_TO_ROOM';" \
  | grep -qx 1
metric 'jpa_order_persistence=VERIFIED'
docker restart "$FB_CONTAINER" >/dev/null
wait_for_health native_after_persistence_restart http://127.0.0.1:18088/actuator/health 120 "$FB_CONTAINER"
persisted_status="$(signed_request GET "http://127.0.0.1:18087/api/v1/fb/orders/stay/${STAY_ID}" "$HOTEL_A" "$(new_id)" "${RESULT_DIR}/orders-after-restart.json")"
assert_code 200 "$persisted_status" native_order_persistence_after_restart
jq -e --arg id "$ORDER_ID" 'any(.[]; .id == $id and .status == "BILLED_TO_ROOM")' "${RESULT_DIR}/orders-after-restart.json" >/dev/null
metric 'persistence_after_restart=VERIFIED'

echo 'Exercising Billing Feign fallback after a real order is created'
fallback_order_file="${RESULT_DIR}/native-fallback-order.json"
fallback_order_status="$(signed_request POST http://127.0.0.1:18087/api/v1/fb/orders "${HOTEL_A}" "$(new_id)" \
  "${fallback_order_file}" "{\"stayId\":\"${STAY_ID}\",\"items\":[{\"menuItemId\":\"${MENU_ID}\",\"quantity\":1}]}")"
assert_code 201 "${fallback_order_status}" fallback_order_create
FALLBACK_ORDER_ID="$(jq -er '.id' "${fallback_order_file}")"
docker stop "${BILLING_CONTAINER}" >/dev/null
fallback_confirm_file="${RESULT_DIR}/native-billing-fallback-confirm.json"
fallback_confirm_status="$(signed_request POST "http://127.0.0.1:18087/api/v1/fb/orders/${FALLBACK_ORDER_ID}/confirm" "${HOTEL_A}" "$(new_id)" "${fallback_confirm_file}")"
assert_code 200 "${fallback_confirm_status}" billing_fallback_confirm
jq -e '.status == "BILLED_TO_ROOM"' "${fallback_confirm_file}" >/dev/null
metric 'resilience4j_billing_fallback=VERIFIED'

echo 'Exercising Stay Feign fallback with the real frontdesk container stopped'
docker stop "${FRONTDESK_CONTAINER}" >/dev/null
stay_fallback_file="${RESULT_DIR}/native-stay-fallback.json"
stay_fallback_status="$(signed_request POST http://127.0.0.1:18087/api/v1/fb/orders "${HOTEL_A}" "$(new_id)" \
  "${stay_fallback_file}" "{\"stayId\":\"$(new_id)\",\"items\":[{\"menuItemId\":\"${MENU_ID}\",\"quantity\":1}]}")"
assert_code 404 "${stay_fallback_status}" stay_fallback_rejection
metric 'resilience4j_stay_fallback=VERIFIED'

echo 'Exercising Redis-backed HMAC replay protection'
replay_nonce="$(new_id)"
replay_file="${RESULT_DIR}/replay-first.json"
replay_first_status="$(signed_request GET http://127.0.0.1:18087/api/v1/fb/menu-items "${HOTEL_A}" "${replay_nonce}" "${replay_file}")"
assert_code 200 "${replay_first_status}" hmac_first_request
replay_second_status="$(signed_request GET http://127.0.0.1:18087/api/v1/fb/menu-items "${HOTEL_A}" "${replay_nonce}" "${RESULT_DIR}/replay-second.json")"
assert_code 401 "${replay_second_status}" hmac_replay_rejected
metric 'hmac_redis_replay_protection=VERIFIED'

for runtime in native jvm; do
  port=18087
  [[ "$runtime" == jvm ]] && port=18089
  unsigned_status="$(curl --silent --show-error --max-time 5 --output "${RESULT_DIR}/${runtime}-unsigned.json" \
    --write-out '%{http_code}' "http://127.0.0.1:${port}/api/v1/fb/menu-items")"
  assert_code 401 "$unsigned_status" "${runtime}_unsigned_rejected"
  invalid_status="$(CI_HMAC_SECRET=deliberately-invalid-gate-signing-key signed_request GET \
    "http://127.0.0.1:${port}/api/v1/fb/menu-items" "$HOTEL_A" "$(new_id)" "${RESULT_DIR}/${runtime}-invalid-hmac.json")"
  assert_code 401 "$invalid_status" "${runtime}_invalid_hmac_rejected"
  forbidden_status="$(signed_request POST "http://127.0.0.1:${port}/api/v1/fb/menu-items" "$HOTEL_A" "$(new_id)" \
    "${RESULT_DIR}/${runtime}-rbac-forbidden.json" '{"name":"Forbidden","price":1,"category":"Test","available":true}' RECEPTIONIST)"
  assert_code 403 "$forbidden_status" "${runtime}_rbac_menu_write_rejected"
  check_probes "$runtime" "$((port + 1))"
done
for container in "$FB_CONTAINER" "$JVM_FB_CONTAINER"; do
  docker inspect --format '{{json .}}' "$container" \
    | jq -e '.State.Running == true and .State.OOMKilled == false and .RestartCount == 0' >/dev/null
done
metric 'native_jvm_no_oom_or_automatic_restarts=VERIFIED'

for key in native_startup_ms jvm_startup_ms native_idle_memory jvm_idle_memory \
    native_loaded_memory jvm_loaded_memory native_image_size_bytes jvm_image_size_bytes; do
  value="$(sed -n "s/^${key}=//p" "${RESULT_DIR}/metrics.txt")"
  [[ "$value" =~ ^[0-9]+$ && "$value" -gt 0 ]]
done

metric 'native_runtime_gate=PASS'
echo "Native F&B runtime gate passed; evidence in ${RESULT_DIR}"
