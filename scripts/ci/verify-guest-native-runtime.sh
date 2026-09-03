#!/usr/bin/env bash
set -Eeuo pipefail

RESULT_DIR="${GITHUB_WORKSPACE:-$(pwd)}/build/guest-native-runtime"
NETWORK_NAME="guest-native-ci"
POSTGRES_CONTAINER="guest-native-postgres"
REDIS_CONTAINER="guest-native-redis"
CONFIG_CONTAINER="guest-native-config"
FRONTDESK_CONTAINER="guest-native-frontdesk"
BILLING_CONTAINER="guest-native-billing"
GUEST_CONTAINER="guest-service-native"
JVM_GUEST_CONTAINER="guest-service-jvm"

: "${CI_POSTGRES_PASSWORD:?CI_POSTGRES_PASSWORD is required}"
: "${CI_REDIS_PASSWORD:?CI_REDIS_PASSWORD is required}"
: "${CI_CONFIG_PASSWORD:?CI_CONFIG_PASSWORD is required}"
: "${CI_HMAC_SECRET:?CI_HMAC_SECRET is required}"

mkdir -p "${RESULT_DIR}"

collect_evidence() {
  docker ps -a > "${RESULT_DIR}/docker-ps.txt" 2>&1 || true
  for container in "${CONFIG_CONTAINER}" "${POSTGRES_CONTAINER}" "${REDIS_CONTAINER}" \
      "${FRONTDESK_CONTAINER}" "${BILLING_CONTAINER}" "${GUEST_CONTAINER}" \
      "${JVM_GUEST_CONTAINER}"; do
    docker logs "${container}" > "${RESULT_DIR}/${container}.log" 2>&1 || true
  done
}
trap collect_evidence EXIT

wait_for_health() {
  local label="$1"
  local url="$2"
  local attempts="${3:-90}"
  local container="${4:-}"
  local response container_state
  for ((i = 1; i <= attempts; i++)); do
    response="$(curl --silent --show-error --max-time 3 "${url}" 2>/dev/null || true)"
    if jq -e '.status == "UP"' >/dev/null 2>&1 <<<"${response}"; then
      return 0
    fi
    if [[ -n "${container}" ]]; then
      container_state="$(docker inspect --format '{{.State.Status}}' "${container}" 2>/dev/null || true)"
      if [[ "${container_state}" == "exited" || "${container_state}" == "dead" ]]; then
        echo "${label} container stopped before becoming UP" >&2
        return 1
      fi
    fi
    sleep 2
  done
  echo "${label} did not become UP at ${url}" >&2
  return 1
}

signed_request() {
  local method="$1"
  local url="$2"
  local hotel_id="$3"
  local nonce="$4"
  local output_file="$5"
  local body="${6:-}"
  local timestamp signature
  timestamp="$(date +%s%3N)"
  signature="$(printf '%s' "ci-admin:ADMIN:${hotel_id}:${timestamp}:${nonce}" \
    | openssl dgst -sha256 -hmac "${CI_HMAC_SECRET}" -r | awk '{print $1}')"

  local curl_args=(
    --silent --show-error --output "${output_file}" --write-out '%{http_code}'
    --request "${method}"
    --header "X-Auth-User: ci-admin"
    --header "X-Auth-Role: ADMIN"
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

echo "Starting real runtime Config Server"
docker network create "${NETWORK_NAME}" >/dev/null
docker run --detach --name "${CONFIG_CONTAINER}" --network "${NETWORK_NAME}" \
  --publish 18888:8888 --publish 18091:8090 \
  --env "CONFIG_SERVER_PASSWORD=${CI_CONFIG_PASSWORD}" \
  --env 'JAVA_TOOL_OPTIONS=-Xmx256m -XX:MaxMetaspaceSize=128m -XX:+ExitOnOutOfMemoryError' \
  hotel-pms/config-service:ci >/dev/null
wait_for_health config-service http://127.0.0.1:18091/actuator/health 90 "${CONFIG_CONTAINER}"
curl --silent --show-error --fail --user "configuser:${CI_CONFIG_PASSWORD}" \
  http://127.0.0.1:18888/guest-service/default \
  | jq -e '.name == "guest-service"' >/dev/null

echo "Starting PostgreSQL and Redis"
docker run --detach --name "${POSTGRES_CONTAINER}" --network "${NETWORK_NAME}" \
  --env POSTGRES_USER=postgres \
  --env "POSTGRES_PASSWORD=${CI_POSTGRES_PASSWORD}" \
  postgres:15-alpine >/dev/null

docker run --detach --name "${REDIS_CONTAINER}" --network "${NETWORK_NAME}" \
  redis:8.8.1-alpine redis-server --requirepass "${CI_REDIS_PASSWORD}" >/dev/null

postgres_ready_streak=0
for _ in {1..60}; do
  if docker exec "${POSTGRES_CONTAINER}" pg_isready --username postgres >/dev/null 2>&1 \
      && docker exec "${POSTGRES_CONTAINER}" psql --username postgres --dbname postgres \
        --tuples-only --no-align --command 'select 1;' 2>/dev/null | grep -qx 1; then
    postgres_ready_streak="$((postgres_ready_streak + 1))"
    if [[ "${postgres_ready_streak}" -ge 2 ]]; then
      break
    fi
  else
    postgres_ready_streak=0
  fi
  sleep 2
done
if [[ "${postgres_ready_streak}" -lt 2 ]]; then
  echo "PostgreSQL did not remain ready for two consecutive checks" >&2
  exit 1
fi

for database in hotel_guest hotel_guest_jvm hotel_frontdesk hotel_billing; do
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

echo "Starting real JVM downstream services"
docker run --detach --name "${FRONTDESK_CONTAINER}" --network "${NETWORK_NAME}" \
  --network-alias frontdesk-service \
  --publish 18092:8090 \
  --env SPRING_PROFILES_ACTIVE=frontdesk-service \
  --env CONFIG_SERVER_URL=http://guest-native-config:8888 \
  --env CONFIG_SERVER_PASSWORD="${CI_CONFIG_PASSWORD}" \
  --env INTERNAL_REDIS_HOST=guest-native-redis \
  --env INTERNAL_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" \
  --env SPRING_DATA_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" \
  --env SPRING_DATASOURCE_URL=jdbc:postgresql://guest-native-postgres:5432/hotel_frontdesk \
  --env SPRING_DATASOURCE_USERNAME=postgres \
  --env "SPRING_DATASOURCE_PASSWORD=${CI_POSTGRES_PASSWORD}" \
  --env INTERNAL_HMAC_SECRET="${CI_HMAC_SECRET}" \
  --env ALLOGGIATI_USERNAME=ci_placeholder_user \
  --env ALLOGGIATI_PASSWORD=ci_placeholder_password \
  --env ALLOGGIATI_WS_KEY=ci_placeholder_wskey \
  --env ALLOGGIATI_DRY_RUN=true \
  --env ALLOGGIATI_CREDENTIALS_ENCRYPTION_KEY=ci_placeholder_encryption_key \
  --env ALLOGGIATI_CREDENTIALS_ENCRYPTION_SALT=deadbeefdeadbeefdeadbeefdeadbeef \
  --env 'JAVA_TOOL_OPTIONS=-Xmx512m -XX:MaxMetaspaceSize=256m -XX:+ExitOnOutOfMemoryError' \
  hotel-pms/frontdesk-service:ci >/dev/null

docker run --detach --name "${BILLING_CONTAINER}" --network "${NETWORK_NAME}" \
  --network-alias billing-service \
  --publish 18093:8090 \
  --env SPRING_PROFILES_ACTIVE=billing-service \
  --env CONFIG_SERVER_URL=http://guest-native-config:8888 \
  --env CONFIG_SERVER_PASSWORD="${CI_CONFIG_PASSWORD}" \
  --env INTERNAL_REDIS_HOST=guest-native-redis \
  --env INTERNAL_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" \
  --env SPRING_DATA_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" \
  --env SPRING_DATASOURCE_URL=jdbc:postgresql://guest-native-postgres:5432/hotel_billing \
  --env SPRING_DATASOURCE_USERNAME=postgres \
  --env "SPRING_DATASOURCE_PASSWORD=${CI_POSTGRES_PASSWORD}" \
  --env INTERNAL_HMAC_SECRET="${CI_HMAC_SECRET}" \
  --env 'JAVA_TOOL_OPTIONS=-Xmx512m -XX:MaxMetaspaceSize=256m -XX:+ExitOnOutOfMemoryError' \
  hotel-pms/billing-service:ci >/dev/null

wait_for_health frontdesk-service http://127.0.0.1:18092/actuator/health 120 "${FRONTDESK_CONTAINER}"
wait_for_health billing-service http://127.0.0.1:18093/actuator/health 120 "${BILLING_CONTAINER}"

echo "Starting guest-service Native Image"
startup_started_ms="$(date +%s%3N)"
docker run --detach --name "${GUEST_CONTAINER}" --network "${NETWORK_NAME}" \
  --publish 18083:8083 --publish 18090:8090 \
  --env SPRING_PROFILES_ACTIVE=guest-service \
  --env CONFIG_SERVER_URL=http://guest-native-config:8888 \
  --env CONFIG_SERVER_PASSWORD="${CI_CONFIG_PASSWORD}" \
  --env INTERNAL_REDIS_HOST=guest-native-redis \
  --env INTERNAL_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" \
  --env SPRING_DATA_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" \
  --env SPRING_DATASOURCE_URL=jdbc:postgresql://guest-native-postgres:5432/hotel_guest \
  --env SPRING_DATASOURCE_USERNAME=postgres \
  --env "SPRING_DATASOURCE_PASSWORD=${CI_POSTGRES_PASSWORD}" \
  --env APPLICATION_CONFIG_FRONTDESK_SERVICE_URL=http://frontdesk-service:8081 \
  --env APPLICATION_CONFIG_BILLING_SERVICE_URL=http://billing-service:8085 \
  --env INTERNAL_HMAC_SECRET="${CI_HMAC_SECRET}" \
  hotel-pms/guest-service-native:ci >/dev/null

wait_for_health guest-service-native http://127.0.0.1:18090/actuator/health 120 "${GUEST_CONTAINER}"
startup_ready_ms="$(date +%s%3N)"
startup_ms="$((startup_ready_ms - startup_started_ms))"

health_body="$(curl --silent --show-error --fail http://127.0.0.1:18090/actuator/health)"
jq -e '.status == "UP"' >/dev/null <<<"${health_body}"
printf '%s\n' "${health_body}" > "${RESULT_DIR}/health.json"

idle_memory="$(docker stats --no-stream --format '{{.MemUsage}}|{{.MemPerc}}' "${GUEST_CONTAINER}")"

missing_hmac_code="$(curl --silent --output "${RESULT_DIR}/missing-hmac.json" \
  --write-out '%{http_code}' http://127.0.0.1:18083/api/v1/guests)"
[[ "${missing_hmac_code}" == "401" ]]

hotel_a="00000000-0000-0000-0000-000000000101"
hotel_b="00000000-0000-0000-0000-000000000202"
guest_payload='{"firstName":"Native","lastName":"Validation","email":"native-validation@example.test"}'
create_code="$(signed_request POST http://127.0.0.1:18083/api/v1/guests \
  "${hotel_a}" "$(openssl rand -hex 16)" "${RESULT_DIR}/guest-create.json" "${guest_payload}")"
[[ "${create_code}" == "201" ]]
guest_id="$(jq -er '.id' "${RESULT_DIR}/guest-create.json")"

same_tenant_code="$(signed_request GET "http://127.0.0.1:18083/api/v1/guests/${guest_id}" \
  "${hotel_a}" "$(openssl rand -hex 16)" "${RESULT_DIR}/same-tenant.json")"
[[ "${same_tenant_code}" == "200" ]]

cross_tenant_code="$(signed_request GET "http://127.0.0.1:18083/api/v1/guests/${guest_id}" \
  "${hotel_b}" "$(openssl rand -hex 16)" "${RESULT_DIR}/cross-tenant.json")"
[[ "${cross_tenant_code}" == "404" ]]

list_code="$(signed_request GET http://127.0.0.1:18083/api/v1/guests \
  "${hotel_a}" "$(openssl rand -hex 16)" "${RESULT_DIR}/guest-list.json")"
[[ "${list_code}" == "200" ]]

replay_nonce="$(openssl rand -hex 16)"
replay_timestamp="$(date +%s%3N)"
replay_signature="$(printf '%s' "ci-admin:ADMIN:${hotel_a}:${replay_timestamp}:${replay_nonce}" \
  | openssl dgst -sha256 -hmac "${CI_HMAC_SECRET}" -r | awk '{print $1}')"
replay_headers=(
  --header 'X-Auth-User: ci-admin'
  --header 'X-Auth-Role: ADMIN'
  --header "X-Auth-Hotel: ${hotel_a}"
  --header "X-Auth-Timestamp: ${replay_timestamp}"
  --header "X-Auth-Nonce: ${replay_nonce}"
  --header "X-Internal-Signature: ${replay_signature}"
)
first_replay_code="$(curl --silent --output "${RESULT_DIR}/replay-first.json" --write-out '%{http_code}' \
  "${replay_headers[@]}" "http://127.0.0.1:18083/api/v1/guests/${guest_id}")"
second_replay_code="$(curl --silent --output "${RESULT_DIR}/replay-second.json" --write-out '%{http_code}' \
  "${replay_headers[@]}" "http://127.0.0.1:18083/api/v1/guests/${guest_id}")"
[[ "${first_replay_code}" == "200" && "${second_replay_code}" == "401" ]]
redis_nonce_exists="$(docker exec "${REDIS_CONTAINER}" redis-cli --pass "${CI_REDIS_PASSWORD}" \
  exists "internal-auth:nonce:${replay_nonce}" 2>/dev/null)"
[[ "${redis_nonce_exists}" == "1" ]]

nonce_count_before="$(docker exec "${REDIS_CONTAINER}" redis-cli --pass "${CI_REDIS_PASSWORD}" \
  --scan --pattern 'internal-auth:nonce:*' 2>/dev/null | wc -l | tr -d ' ')"
export_code="$(signed_request GET "http://127.0.0.1:18083/api/v1/guests/${guest_id}/export" \
  "${hotel_a}" "$(openssl rand -hex 16)" "${RESULT_DIR}/guest-export.json")"
[[ "${export_code}" == "200" ]]
nonce_count_after="$(docker exec "${REDIS_CONTAINER}" redis-cli --pass "${CI_REDIS_PASSWORD}" \
  --scan --pattern 'internal-auth:nonce:*' 2>/dev/null | wc -l | tr -d ' ')"
feign_nonce_delta="$((nonce_count_after - nonce_count_before))"
docker exec "${REDIS_CONTAINER}" redis-cli --pass "${CI_REDIS_PASSWORD}" \
  --scan --pattern 'internal-auth:nonce:*' 2>/dev/null \
  | sort > "${RESULT_DIR}/redis-nonces-after-export.txt"
printf 'Feign nonce evidence: before=%s after=%s delta=%s\n' \
  "${nonce_count_before}" "${nonce_count_after}" "${feign_nonce_delta}"
native_feign_status=PASS
if [[ "${feign_nonce_delta}" -lt 3 ]]; then
  native_feign_status=FAIL
  echo "Native export did not prove both signed Feign calls; continuing with the JVM control" >&2
fi

flyway_latest="$(docker exec "${POSTGRES_CONTAINER}" psql --username postgres --dbname hotel_guest \
  --tuples-only --no-align --command \
  'select max(version::integer) from flyway_schema_history where success = true;')"
[[ "${flyway_latest}" == "10" ]]

persisted_rows="$(docker exec "${POSTGRES_CONTAINER}" psql --username postgres --dbname hotel_guest \
  --tuples-only --no-align --command \
  "select count(*) from guests where id = '${guest_id}' and hotel_id = '${hotel_a}';")"
[[ "${persisted_rows}" == "1" ]]

for _ in {1..15}; do
  curl --silent --show-error --fail http://127.0.0.1:18090/actuator/health \
    | jq -e '.status == "UP"' >/dev/null
  sleep 1
done

loaded_memory="$(docker stats --no-stream --format '{{.MemUsage}}|{{.MemPerc}}' "${GUEST_CONTAINER}")"
image_size_bytes="$(docker image inspect hotel-pms/guest-service-native:ci --format '{{.Size}}')"

docker stop "${GUEST_CONTAINER}" >/dev/null

echo "Starting guest-service JVM control"
jvm_startup_started_ms="$(date +%s%3N)"
docker run --detach --name "${JVM_GUEST_CONTAINER}" --network "${NETWORK_NAME}" \
  --publish 28083:8083 --publish 28090:8090 \
  --env SPRING_PROFILES_ACTIVE=guest-service \
  --env CONFIG_SERVER_URL=http://guest-native-config:8888 \
  --env CONFIG_SERVER_PASSWORD="${CI_CONFIG_PASSWORD}" \
  --env INTERNAL_REDIS_HOST=guest-native-redis \
  --env INTERNAL_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" \
  --env SPRING_DATA_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" \
  --env SPRING_DATASOURCE_URL=jdbc:postgresql://guest-native-postgres:5432/hotel_guest_jvm \
  --env SPRING_DATASOURCE_USERNAME=postgres \
  --env "SPRING_DATASOURCE_PASSWORD=${CI_POSTGRES_PASSWORD}" \
  --env APPLICATION_CONFIG_FRONTDESK_SERVICE_URL=http://frontdesk-service:8081 \
  --env APPLICATION_CONFIG_BILLING_SERVICE_URL=http://billing-service:8085 \
  --env INTERNAL_HMAC_SECRET="${CI_HMAC_SECRET}" \
  --env 'JAVA_TOOL_OPTIONS=-Xmx512m -XX:MaxMetaspaceSize=256m -XX:+ExitOnOutOfMemoryError' \
  hotel-pms/guest-service-jvm:ci >/dev/null

wait_for_health guest-service-jvm http://127.0.0.1:28090/actuator/health 120 "${JVM_GUEST_CONTAINER}"
jvm_startup_ready_ms="$(date +%s%3N)"
jvm_startup_ms="$((jvm_startup_ready_ms - jvm_startup_started_ms))"
jvm_health_body="$(curl --silent --show-error --fail http://127.0.0.1:28090/actuator/health)"
jq -e '.status == "UP"' >/dev/null <<<"${jvm_health_body}"
printf '%s\n' "${jvm_health_body}" > "${RESULT_DIR}/jvm-health.json"
jvm_idle_memory="$(docker stats --no-stream --format '{{.MemUsage}}|{{.MemPerc}}' "${JVM_GUEST_CONTAINER}")"

jvm_missing_hmac_code="$(curl --silent --output "${RESULT_DIR}/jvm-missing-hmac.json" \
  --write-out '%{http_code}' http://127.0.0.1:28083/api/v1/guests)"
[[ "${jvm_missing_hmac_code}" == "401" ]]

jvm_guest_payload='{"firstName":"JVM","lastName":"Control","email":"jvm-control@example.test"}'
jvm_create_code="$(signed_request POST http://127.0.0.1:28083/api/v1/guests \
  "${hotel_a}" "$(openssl rand -hex 16)" "${RESULT_DIR}/jvm-guest-create.json" "${jvm_guest_payload}")"
[[ "${jvm_create_code}" == "201" ]]
jvm_guest_id="$(jq -er '.id' "${RESULT_DIR}/jvm-guest-create.json")"

jvm_same_tenant_code="$(signed_request GET "http://127.0.0.1:28083/api/v1/guests/${jvm_guest_id}" \
  "${hotel_a}" "$(openssl rand -hex 16)" "${RESULT_DIR}/jvm-same-tenant.json")"
[[ "${jvm_same_tenant_code}" == "200" ]]
jvm_cross_tenant_code="$(signed_request GET "http://127.0.0.1:28083/api/v1/guests/${jvm_guest_id}" \
  "${hotel_b}" "$(openssl rand -hex 16)" "${RESULT_DIR}/jvm-cross-tenant.json")"
[[ "${jvm_cross_tenant_code}" == "404" ]]
jvm_list_code="$(signed_request GET http://127.0.0.1:28083/api/v1/guests \
  "${hotel_a}" "$(openssl rand -hex 16)" "${RESULT_DIR}/jvm-guest-list.json")"
[[ "${jvm_list_code}" == "200" ]]

jvm_replay_nonce="$(openssl rand -hex 16)"
jvm_replay_timestamp="$(date +%s%3N)"
jvm_replay_signature="$(printf '%s' "ci-admin:ADMIN:${hotel_a}:${jvm_replay_timestamp}:${jvm_replay_nonce}" \
  | openssl dgst -sha256 -hmac "${CI_HMAC_SECRET}" -r | awk '{print $1}')"
jvm_replay_headers=(
  --header 'X-Auth-User: ci-admin'
  --header 'X-Auth-Role: ADMIN'
  --header "X-Auth-Hotel: ${hotel_a}"
  --header "X-Auth-Timestamp: ${jvm_replay_timestamp}"
  --header "X-Auth-Nonce: ${jvm_replay_nonce}"
  --header "X-Internal-Signature: ${jvm_replay_signature}"
)
jvm_first_replay_code="$(curl --silent --output "${RESULT_DIR}/jvm-replay-first.json" \
  --write-out '%{http_code}' "${jvm_replay_headers[@]}" \
  "http://127.0.0.1:28083/api/v1/guests/${jvm_guest_id}")"
jvm_second_replay_code="$(curl --silent --output "${RESULT_DIR}/jvm-replay-second.json" \
  --write-out '%{http_code}' "${jvm_replay_headers[@]}" \
  "http://127.0.0.1:28083/api/v1/guests/${jvm_guest_id}")"
[[ "${jvm_first_replay_code}" == "200" && "${jvm_second_replay_code}" == "401" ]]
jvm_redis_nonce_exists="$(docker exec "${REDIS_CONTAINER}" redis-cli --pass "${CI_REDIS_PASSWORD}" \
  exists "internal-auth:nonce:${jvm_replay_nonce}" 2>/dev/null)"
[[ "${jvm_redis_nonce_exists}" == "1" ]]

jvm_nonce_count_before="$(docker exec "${REDIS_CONTAINER}" redis-cli --pass "${CI_REDIS_PASSWORD}" \
  --scan --pattern 'internal-auth:nonce:*' 2>/dev/null | wc -l | tr -d ' ')"
jvm_export_code="$(signed_request GET "http://127.0.0.1:28083/api/v1/guests/${jvm_guest_id}/export" \
  "${hotel_a}" "$(openssl rand -hex 16)" "${RESULT_DIR}/jvm-guest-export.json")"
[[ "${jvm_export_code}" == "200" ]]
jvm_nonce_count_after="$(docker exec "${REDIS_CONTAINER}" redis-cli --pass "${CI_REDIS_PASSWORD}" \
  --scan --pattern 'internal-auth:nonce:*' 2>/dev/null | wc -l | tr -d ' ')"
jvm_feign_nonce_delta="$((jvm_nonce_count_after - jvm_nonce_count_before))"
printf 'JVM Feign nonce evidence: before=%s after=%s delta=%s\n' \
  "${jvm_nonce_count_before}" "${jvm_nonce_count_after}" "${jvm_feign_nonce_delta}"
jvm_feign_status=PASS
if [[ "${jvm_feign_nonce_delta}" -lt 3 ]]; then
  jvm_feign_status=FAIL
fi

jvm_flyway_latest="$(docker exec "${POSTGRES_CONTAINER}" psql --username postgres \
  --dbname hotel_guest_jvm --tuples-only --no-align --command \
  'select max(version::integer) from flyway_schema_history where success = true;')"
[[ "${jvm_flyway_latest}" == "10" ]]
jvm_persisted_rows="$(docker exec "${POSTGRES_CONTAINER}" psql --username postgres \
  --dbname hotel_guest_jvm --tuples-only --no-align --command \
  "select count(*) from guests where id = '${jvm_guest_id}' and hotel_id = '${hotel_a}';")"
[[ "${jvm_persisted_rows}" == "1" ]]

for _ in {1..15}; do
  curl --silent --show-error --fail http://127.0.0.1:28090/actuator/health \
    | jq -e '.status == "UP"' >/dev/null
  sleep 1
done
jvm_loaded_memory="$(docker stats --no-stream --format '{{.MemUsage}}|{{.MemPerc}}' "${JVM_GUEST_CONTAINER}")"
jvm_image_size_bytes="$(docker image inspect hotel-pms/guest-service-jvm:ci --format '{{.Size}}')"

cat > "${RESULT_DIR}/metrics.txt" <<METRICS
native_startup_ms=${startup_ms}
native_idle_memory=${idle_memory}
native_loaded_memory=${loaded_memory}
native_image_size_bytes=${image_size_bytes}
native_health_status=UP
native_postgresql=PASS
native_flyway_latest_version=${flyway_latest}
native_redis=PASS
native_hmac_missing_headers=401
native_hmac_replay_first=${first_replay_code}
native_hmac_replay_second=${second_replay_code}
native_feign_accepted_nonce_delta=${feign_nonce_delta}
native_feign_status=${native_feign_status}
native_tenant_same_hotel=${same_tenant_code}
native_tenant_cross_hotel=${cross_tenant_code}
native_paginated_list=${list_code}
native_stability_health_checks=15/15
jvm_startup_ms=${jvm_startup_ms}
jvm_idle_memory=${jvm_idle_memory}
jvm_loaded_memory=${jvm_loaded_memory}
jvm_image_size_bytes=${jvm_image_size_bytes}
jvm_health_status=UP
jvm_postgresql=PASS
jvm_flyway_latest_version=${jvm_flyway_latest}
jvm_redis=PASS
jvm_hmac_missing_headers=${jvm_missing_hmac_code}
jvm_hmac_replay_first=${jvm_first_replay_code}
jvm_hmac_replay_second=${jvm_second_replay_code}
jvm_feign_accepted_nonce_delta=${jvm_feign_nonce_delta}
jvm_feign_status=${jvm_feign_status}
jvm_tenant_same_hotel=${jvm_same_tenant_code}
jvm_tenant_cross_hotel=${jvm_cross_tenant_code}
jvm_paginated_list=${jvm_list_code}
jvm_stability_health_checks=15/15
METRICS

cat "${RESULT_DIR}/metrics.txt"

if [[ "${native_feign_status}" != PASS ]]; then
  echo "Native Feign integration did not claim both downstream HMAC nonces" >&2
  exit 1
fi
if [[ "${jvm_feign_status}" != PASS ]]; then
  echo "JVM control also failed the Feign HMAC integration check" >&2
  exit 1
fi
