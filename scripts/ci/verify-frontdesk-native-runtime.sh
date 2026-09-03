#!/usr/bin/env bash
set -Eeuo pipefail

RESULT_DIR="${GITHUB_WORKSPACE:-$(pwd)}/build/frontdesk-native-runtime"
NETWORK_NAME="frontdesk-native-ci"
POSTGRES_CONTAINER="frontdesk-native-postgres"
REDIS_CONTAINER="frontdesk-native-redis"
CONFIG_CONTAINER="frontdesk-native-config"
GUEST_CONTAINER="frontdesk-native-guest"
BILLING_CONTAINER="frontdesk-native-billing"
NATIVE_CONTAINER="frontdesk-service-native"
JVM_CONTAINER="frontdesk-service-jvm"

: "${CI_POSTGRES_PASSWORD:?CI_POSTGRES_PASSWORD is required}"
: "${CI_REDIS_PASSWORD:?CI_REDIS_PASSWORD is required}"
: "${CI_CONFIG_PASSWORD:?CI_CONFIG_PASSWORD is required}"
: "${CI_HMAC_SECRET:?CI_HMAC_SECRET is required}"

mkdir -p "${RESULT_DIR}"
GATE_PHASE=bootstrap

collect_evidence() {
  docker ps -a > "${RESULT_DIR}/docker-ps.txt" 2>&1 || true
  for container in "${CONFIG_CONTAINER}" "${POSTGRES_CONTAINER}" "${REDIS_CONTAINER}" \
      "${GUEST_CONTAINER}" "${BILLING_CONTAINER}" "${NATIVE_CONTAINER}" "${JVM_CONTAINER}"; do
    docker logs "${container}" > "${RESULT_DIR}/${container}.log" 2>&1 || true
  done
}
finalize_gate() {
  local status=$?
  collect_evidence
  if [[ "${status}" -eq 0 ]]; then
    printf 'NATIVE_GATE_PASS phase=%s\n' "${GATE_PHASE}" > "${RESULT_DIR}/gate-status.txt"
  else
    printf 'NATIVE_GATE_FAIL phase=%s exit=%s\n' "${GATE_PHASE}" "${status}" | tee "${RESULT_DIR}/gate-status.txt" >&2
  fi
  exit "${status}"
}
trap finalize_gate EXIT

wait_for_health() {
  local label="$1" url="$2" attempts="${3:-90}" container="${4:-}"
  local response state
  for ((i = 1; i <= attempts; i++)); do
    response="$(curl --silent --show-error --max-time 3 "${url}" 2>/dev/null || true)"
    if jq -e '.status == "UP"' >/dev/null 2>&1 <<<"${response}"; then
      return 0
    fi
    if [[ -n "${container}" ]]; then
      state="$(docker inspect --format '{{.State.Status}}' "${container}" 2>/dev/null || true)"
      if [[ "${state}" == exited || "${state}" == dead ]]; then
        echo "${label} stopped before becoming UP" >&2
        return 1
      fi
    fi
    sleep 2
  done
  echo "${label} did not become UP at ${url}" >&2
  return 1
}

signed_request() {
  local method="$1" url="$2" hotel_id="$3" nonce="$4" output_file="$5" body="${6:-}"
  local timestamp signature
  timestamp="$(date +%s%3N)"
  signature="$(printf '%s' "ci-admin:ADMIN:${hotel_id}:${timestamp}:${nonce}" \
    | openssl dgst -sha256 -hmac "${CI_HMAC_SECRET}" -r | awk '{print $1}')"
  local curl_args=(
    --silent --show-error --output "${output_file}" --write-out '%{http_code}'
    --request "${method}"
    --header 'X-Auth-User: ci-admin'
    --header 'X-Auth-Role: ADMIN'
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

count_nonce_keys() {
  docker exec "${REDIS_CONTAINER}" redis-cli --pass "${CI_REDIS_PASSWORD}" \
    --scan --pattern 'internal-auth:nonce:*' 2>/dev/null \
    | awk 'NF { count++ } END { print count + 0 }'
}

echo "Starting real Config Server"
docker network create "${NETWORK_NAME}" >/dev/null
docker run --detach --name "${CONFIG_CONTAINER}" --network "${NETWORK_NAME}" \
  --publish 18888:8888 --publish 18090:8090 \
  --env "CONFIG_SERVER_PASSWORD=${CI_CONFIG_PASSWORD}" \
  --env 'JAVA_TOOL_OPTIONS=-Xmx256m -XX:MaxMetaspaceSize=128m -XX:+ExitOnOutOfMemoryError' \
  hotel-pms/config-service:ci >/dev/null
wait_for_health config-service http://127.0.0.1:18090/actuator/health 90 "${CONFIG_CONTAINER}"
curl --silent --show-error --fail --user "configuser:${CI_CONFIG_PASSWORD}" \
  http://127.0.0.1:18888/frontdesk-service/default \
  | jq -e '.name == "frontdesk-service"' >/dev/null

echo "Starting PostgreSQL and Redis"
docker run --detach --name "${POSTGRES_CONTAINER}" --network "${NETWORK_NAME}" \
  --env POSTGRES_USER=postgres --env "POSTGRES_PASSWORD=${CI_POSTGRES_PASSWORD}" \
  postgres:15-alpine >/dev/null
docker run --detach --name "${REDIS_CONTAINER}" --network "${NETWORK_NAME}" \
  redis:8.8.1-alpine redis-server --requirepass "${CI_REDIS_PASSWORD}" >/dev/null

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
[[ "${postgres_ready_streak}" -ge 2 ]]
for database in hotel_frontdesk hotel_frontdesk_jvm hotel_guest hotel_billing; do
  docker exec "${POSTGRES_CONTAINER}" createdb --username postgres "${database}"
done
for _ in {1..30}; do
  if docker exec "${REDIS_CONTAINER}" redis-cli --pass "${CI_REDIS_PASSWORD}" ping 2>/dev/null \
      | grep -qx PONG; then
    break
  fi
  sleep 1
done
docker exec "${REDIS_CONTAINER}" redis-cli --pass "${CI_REDIS_PASSWORD}" ping 2>/dev/null \
  | grep -qx PONG

common_env=(
  --env CONFIG_SERVER_URL=http://frontdesk-native-config:8888
  --env "CONFIG_SERVER_PASSWORD=${CI_CONFIG_PASSWORD}"
  --env INTERNAL_REDIS_HOST=frontdesk-native-redis
  --env "INTERNAL_REDIS_PASSWORD=${CI_REDIS_PASSWORD}"
  --env "SPRING_DATA_REDIS_PASSWORD=${CI_REDIS_PASSWORD}"
  --env SPRING_DATASOURCE_USERNAME=postgres
  --env "SPRING_DATASOURCE_PASSWORD=${CI_POSTGRES_PASSWORD}"
  --env "INTERNAL_HMAC_SECRET=${CI_HMAC_SECRET}"
)

echo "Starting JVM downstream services"
docker run --detach --name "${GUEST_CONTAINER}" --network "${NETWORK_NAME}" \
  --network-alias guest-service --publish 18083:8083 --publish 18093:8090 \
  --env SPRING_PROFILES_ACTIVE=guest-service "${common_env[@]}" \
  --env SPRING_DATASOURCE_URL=jdbc:postgresql://frontdesk-native-postgres:5432/hotel_guest \
  --env APPLICATION_CONFIG_FRONTDESK_SERVICE_URL=http://frontdesk-service:8081 \
  hotel-pms/guest-service:ci >/dev/null
docker run --detach --name "${BILLING_CONTAINER}" --network "${NETWORK_NAME}" \
  --network-alias billing-service --publish 18085:8085 --publish 18095:8090 \
  --env SPRING_PROFILES_ACTIVE=billing-service "${common_env[@]}" \
  --env SPRING_DATASOURCE_URL=jdbc:postgresql://frontdesk-native-postgres:5432/hotel_billing \
  hotel-pms/billing-service:ci >/dev/null
wait_for_health guest-service http://127.0.0.1:18093/actuator/health 120 "${GUEST_CONTAINER}"
wait_for_health billing-service http://127.0.0.1:18095/actuator/health 120 "${BILLING_CONTAINER}"

GATE_PHASE=native-runtime
native_started_ms="$(date +%s%3N)"
docker run --detach --name "${NATIVE_CONTAINER}" --network "${NETWORK_NAME}" \
  --network-alias frontdesk-service --publish 18081:8081 --publish 18091:8090 \
  --env SPRING_PROFILES_ACTIVE=frontdesk-service "${common_env[@]}" \
  --env SPRING_DATASOURCE_URL=jdbc:postgresql://frontdesk-native-postgres:5432/hotel_frontdesk \
  --env ALLOGGIATI_USERNAME=ci-placeholder-user \
  --env ALLOGGIATI_PASSWORD=ci-placeholder-password \
  --env ALLOGGIATI_WS_KEY=ci-placeholder-ws-key \
  --env ALLOGGIATI_DRY_RUN=true \
  --env ALLOGGIATI_CREDENTIALS_ENCRYPTION_KEY=ci-placeholder-encryption-key \
  --env ALLOGGIATI_CREDENTIALS_ENCRYPTION_SALT=deadbeefdeadbeefdeadbeefdeadbeef \
  hotel-pms/frontdesk-service-native:ci >/dev/null
wait_for_health frontdesk-service-native http://127.0.0.1:18091/actuator/health 150 "${NATIVE_CONTAINER}"
native_ready_ms="$(date +%s%3N)"
native_startup_ms=$((native_ready_ms - native_started_ms))
native_health="$(curl --silent --show-error --fail http://127.0.0.1:18091/actuator/health)"
printf '%s\n' "${native_health}" > "${RESULT_DIR}/native-health.json"
jq -e '.status == "UP"' >/dev/null <<<"${native_health}"
native_memory="$(docker stats --no-stream --format '{{.MemUsage}}|{{.MemPerc}}' "${NATIVE_CONTAINER}")"
native_image_size="$(docker image inspect hotel-pms/frontdesk-service-native:ci --format '{{.Size}}')"

hotel_a=00000000-0000-0000-0000-000000000101
hotel_b=00000000-0000-0000-0000-000000000202
tomorrow="$(date -u -d '+1 day' +%F)"
checkout="$(date -u -d '+3 day' +%F)"

missing_hmac_code="$(curl --silent --output "${RESULT_DIR}/native-missing-hmac.json" \
  --write-out '%{http_code}' http://127.0.0.1:18081/api/v1/rooms)"
[[ "${missing_hmac_code}" == 401 ]]

guest_payload='{"firstName":"Native","lastName":"Frontdesk","email":"native-frontdesk@example.test"}'
guest_code="$(signed_request POST http://127.0.0.1:18083/api/v1/guests "${hotel_a}" \
  "$(openssl rand -hex 16)" "${RESULT_DIR}/guest-create.json" "${guest_payload}")"
[[ "${guest_code}" == 201 ]]
guest_id="$(jq -er '.id' "${RESULT_DIR}/guest-create.json")"

room_type_payload="{\"name\":\"Native Deluxe ${guest_id:0:8}\",\"description\":\"CI room type\",\"maxOccupancy\":2,\"basePrice\":100.00}"
room_type_code="$(signed_request POST http://127.0.0.1:18081/api/v1/room-types "${hotel_a}" \
  "$(openssl rand -hex 16)" "${RESULT_DIR}/room-type.json" "${room_type_payload}")"
[[ "${room_type_code}" == 201 ]]
room_type_id="$(jq -er '.id' "${RESULT_DIR}/room-type.json")"
room_payload="{\"hotelId\":\"${hotel_a}\",\"roomNumber\":\"N-${guest_id:0:8}\",\"roomTypeId\":\"${room_type_id}\",\"status\":\"CLEAN\"}"
room_code="$(signed_request POST http://127.0.0.1:18081/api/v1/rooms "${hotel_a}" \
  "$(openssl rand -hex 16)" "${RESULT_DIR}/room.json" "${room_payload}")"
[[ "${room_code}" == 201 ]]
room_id="$(jq -er '.id' "${RESULT_DIR}/room.json")"

reservation_payload="{\"guestId\":\"${guest_id}\",\"expectedGuests\":1,\"checkInDate\":\"${tomorrow}\",\"checkOutDate\":\"${checkout}\",\"status\":\"CONFIRMED\",\"lineItems\":[{\"roomId\":\"${room_id}\"}]}"
reservation_code="$(signed_request POST http://127.0.0.1:18081/api/v1/reservations "${hotel_a}" \
  "$(openssl rand -hex 16)" "${RESULT_DIR}/reservation.json" "${reservation_payload}")"
[[ "${reservation_code}" == 201 ]]
reservation_id="$(jq -er '.id' "${RESULT_DIR}/reservation.json")"

nonce_before="$(count_nonce_keys)"
stay_payload="{\"hotelId\":\"${hotel_a}\",\"reservationId\":\"${reservation_id}\",\"guestId\":\"${guest_id}\",\"roomId\":\"${room_id}\",\"status\":\"EXPECTED\",\"occupantCount\":1,\"guests\":[{\"firstName\":\"Native\",\"lastName\":\"Frontdesk\",\"gender\":\"M\",\"dateOfBirth\":\"1990-01-01\",\"placeOfBirth\":\"Monterrey\",\"citizenship\":\"MX\",\"documentType\":\"PASSPORT\",\"documentNumber\":\"NATIVE123\",\"documentPlaceOfIssue\":\"MX\",\"isPrimaryGuest\":true,\"travellerType\":\"OSPITE_SINGOLO\",\"travelPurpose\":\"BUSINESS\"}]}"
checkin_code="$(signed_request POST http://127.0.0.1:18081/api/v1/stays "${hotel_a}" \
  "$(openssl rand -hex 16)" "${RESULT_DIR}/checkin.json" "${stay_payload}")"
[[ "${checkin_code}" == 200 ]]
stay_id="$(jq -er '.id' "${RESULT_DIR}/checkin.json")"
invoice_id="$(jq -er '.invoiceId' "${RESULT_DIR}/checkin.json")"
jq -e '.status == "CHECKED_IN" and .roomNumber != null' "${RESULT_DIR}/checkin.json" >/dev/null

nonce_after="$(count_nonce_keys)"
feign_nonce_delta=$((nonce_after - nonce_before))
printf 'before=%s\nafter=%s\ndelta=%s\n' "${nonce_before}" "${nonce_after}" "${feign_nonce_delta}" \
  | tee "${RESULT_DIR}/feign-nonce-count.txt"
[[ "${feign_nonce_delta}" -ge 2 ]]

cross_tenant_code="$(signed_request GET "http://127.0.0.1:18081/api/v1/reservations/${reservation_id}" \
  "${hotel_b}" "$(openssl rand -hex 16)" "${RESULT_DIR}/cross-tenant.json")"
[[ "${cross_tenant_code}" == 404 ]]

replay_nonce="$(openssl rand -hex 16)"
replay_timestamp="$(date +%s%3N)"
replay_signature="$(printf '%s' "ci-admin:ADMIN:${hotel_a}:${replay_timestamp}:${replay_nonce}" \
  | openssl dgst -sha256 -hmac "${CI_HMAC_SECRET}" -r | awk '{print $1}')"
replay_headers=(
  --header 'X-Auth-User: ci-admin' --header 'X-Auth-Role: ADMIN'
  --header "X-Auth-Hotel: ${hotel_a}" --header "X-Auth-Timestamp: ${replay_timestamp}"
  --header "X-Auth-Nonce: ${replay_nonce}" --header "X-Internal-Signature: ${replay_signature}"
)
replay_first="$(curl --silent --output "${RESULT_DIR}/replay-first.json" --write-out '%{http_code}' \
  "${replay_headers[@]}" http://127.0.0.1:18081/api/v1/rooms)"
replay_second="$(curl --silent --output "${RESULT_DIR}/replay-second.json" --write-out '%{http_code}' \
  "${replay_headers[@]}" http://127.0.0.1:18081/api/v1/rooms)"
[[ "${replay_first}" == 200 && "${replay_second}" == 401 ]]

invoice_code="$(signed_request GET "http://127.0.0.1:18085/api/v1/invoices/${invoice_id}" "${hotel_a}" \
  "$(openssl rand -hex 16)" "${RESULT_DIR}/invoice.json")"
[[ "${invoice_code}" == 200 ]]
invoice_total="$(jq -er '.totalAmount' "${RESULT_DIR}/invoice.json")"
payment_payload="{\"amount\":${invoice_total},\"paymentMethod\":\"CASH\",\"transactionReference\":\"native-ci\"}"
payment_code="$(signed_request POST "http://127.0.0.1:18085/api/v1/invoices/${invoice_id}/payments" "${hotel_a}" \
  "$(openssl rand -hex 16)" "${RESULT_DIR}/payment.json" "${payment_payload}")"
[[ "${payment_code}" == 201 ]]

checkout_code="$(signed_request PUT "http://127.0.0.1:18081/api/v1/stays/${stay_id}/check-out" "${hotel_a}" \
  "$(openssl rand -hex 16)" "${RESULT_DIR}/checkout.json")"
[[ "${checkout_code}" == 200 ]]
jq -e '.status == "CHECKED_OUT"' "${RESULT_DIR}/checkout.json" >/dev/null

flyway_latest="$(docker exec "${POSTGRES_CONTAINER}" psql --username postgres --dbname hotel_frontdesk \
  --tuples-only --no-align --command 'select max(version::integer) from flyway_schema_history where success = true;')"
[[ "${flyway_latest}" == 20 ]]
persisted_rows="$(docker exec "${POSTGRES_CONTAINER}" psql --username postgres --dbname hotel_frontdesk \
  --tuples-only --no-align --command "select count(*) from stays where id = '${stay_id}' and hotel_id = '${hotel_a}' and status = 'CHECKED_OUT';")"
[[ "${persisted_rows}" == 1 ]]

native_stability=0
for _ in {1..15}; do
  curl --silent --show-error --fail http://127.0.0.1:18091/actuator/health \
    | jq -e '.status == "UP"' >/dev/null
  native_stability=$((native_stability + 1))
  sleep 1
done

docker stop "${NATIVE_CONTAINER}" >/dev/null
GATE_PHASE=jvm-control
jvm_started_ms="$(date +%s%3N)"
docker run --detach --name "${JVM_CONTAINER}" --network "${NETWORK_NAME}" \
  --network-alias frontdesk-service-jvm --publish 28081:8081 --publish 28091:8090 \
  --env SPRING_PROFILES_ACTIVE=frontdesk-service "${common_env[@]}" \
  --env SPRING_DATASOURCE_URL=jdbc:postgresql://frontdesk-native-postgres:5432/hotel_frontdesk_jvm \
  --env ALLOGGIATI_USERNAME=ci-placeholder-user --env ALLOGGIATI_PASSWORD=ci-placeholder-password \
  --env ALLOGGIATI_WS_KEY=ci-placeholder-ws-key --env ALLOGGIATI_DRY_RUN=true \
  --env ALLOGGIATI_CREDENTIALS_ENCRYPTION_KEY=ci-placeholder-encryption-key \
  --env ALLOGGIATI_CREDENTIALS_ENCRYPTION_SALT=deadbeefdeadbeefdeadbeefdeadbeef \
  --env 'JAVA_TOOL_OPTIONS=-Xmx512m -XX:MaxMetaspaceSize=256m -XX:+ExitOnOutOfMemoryError' \
  hotel-pms/frontdesk-service-jvm:ci >/dev/null
wait_for_health frontdesk-service-jvm http://127.0.0.1:28091/actuator/health 150 "${JVM_CONTAINER}"
jvm_ready_ms="$(date +%s%3N)"
jvm_startup_ms=$((jvm_ready_ms - jvm_started_ms))
jvm_health="$(curl --silent --show-error --fail http://127.0.0.1:28091/actuator/health)"
printf '%s\n' "${jvm_health}" > "${RESULT_DIR}/jvm-health.json"
jq -e '.status == "UP"' >/dev/null <<<"${jvm_health}"
jvm_missing_hmac_code="$(curl --silent --output "${RESULT_DIR}/jvm-missing-hmac.json" \
  --write-out '%{http_code}' http://127.0.0.1:28081/api/v1/rooms)"
[[ "${jvm_missing_hmac_code}" == 401 ]]
jvm_memory="$(docker stats --no-stream --format '{{.MemUsage}}|{{.MemPerc}}' "${JVM_CONTAINER}")"
jvm_image_size="$(docker image inspect hotel-pms/frontdesk-service-jvm:ci --format '{{.Size}}')"

cat > "${RESULT_DIR}/metrics.txt" <<METRICS
native_build_mode=${CI_NATIVE_BUILD_MODE:-unknown}
native_startup_ms=${native_startup_ms}
native_idle_memory=${native_memory}
native_image_size_bytes=${native_image_size}
native_health_status=UP
native_rooms_reservations_stays=PASS
native_checkin_status=CHECKED_IN
native_checkout_status=CHECKED_OUT
native_postgresql=PASS
native_flyway_latest_version=${flyway_latest}
native_redis=PASS
native_hmac_missing_headers=${missing_hmac_code}
native_hmac_replay_first=${replay_first}
native_hmac_replay_second=${replay_second}
native_feign_accepted_nonce_delta=${feign_nonce_delta}
native_tenant_cross_hotel=${cross_tenant_code}
native_stability_health_checks=${native_stability}/15
jvm_startup_ms=${jvm_startup_ms}
jvm_idle_memory=${jvm_memory}
jvm_image_size_bytes=${jvm_image_size}
jvm_health_status=UP
jvm_hmac_missing_headers=${jvm_missing_hmac_code}
METRICS
cat "${RESULT_DIR}/metrics.txt"
