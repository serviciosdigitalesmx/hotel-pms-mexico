#!/usr/bin/env bash
set -Eeuo pipefail

RESULT_DIR="${GITHUB_WORKSPACE:-$(pwd)}/build/gateway-native-runtime"
NETWORK_NAME="gateway-native-ci"
POSTGRES_CONTAINER="gateway-native-postgres"
REDIS_CONTAINER="gateway-native-redis"
CONFIG_CONTAINER="gateway-native-config"
AUTH_CONTAINER="gateway-native-auth"
FRONTDESK_CONTAINER="gateway-native-frontdesk"
NATIVE_CONTAINER="api-gateway-native"
JVM_CONTAINER="api-gateway-jvm"

: "${CI_POSTGRES_PASSWORD:?CI_POSTGRES_PASSWORD is required}"
: "${CI_REDIS_PASSWORD:?CI_REDIS_PASSWORD is required}"
: "${CI_CONFIG_PASSWORD:?CI_CONFIG_PASSWORD is required}"
: "${CI_HMAC_SECRET:?CI_HMAC_SECRET is required}"
: "${CI_JWT_SECRET:?CI_JWT_SECRET is required}"

mkdir -p "${RESULT_DIR}"

collect_evidence() {
    docker ps -a > "${RESULT_DIR}/docker-ps.txt" 2>&1 || true
    for container in "${CONFIG_CONTAINER}" "${POSTGRES_CONTAINER}" "${REDIS_CONTAINER}" \
        "${AUTH_CONTAINER}" "${FRONTDESK_CONTAINER}" "${NATIVE_CONTAINER}" "${JVM_CONTAINER}"; do
        docker logs "${container}" > "${RESULT_DIR}/${container}.log" 2>&1 || true
    done
}
trap collect_evidence EXIT

wait_for_health() {
    local label="$1" url="$2" attempts="${3:-120}" container="${4:-}"
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

cookie_value() {
    awk -v name="$1" '$6 == name {print $7}' "$2" | tail -n 1
}

http_code() {
    local output_file="$1"
    shift
    curl --silent --show-error --output "${output_file}" --write-out '%{http_code}' "$@"
}

signed_internal_request() {
    local output_file="$1" port="$2" hotel_id="$3" nonce="$4"
    local timestamp signature
    timestamp="$(date +%s%3N)"
    signature="$(printf '%s' "ci-gateway:ADMIN:${hotel_id}:${timestamp}:${nonce}" \
        | openssl dgst -sha256 -hmac "${CI_HMAC_SECRET}" -r | awk '{print $1}')"
    curl --silent --show-error --output "${output_file}" --write-out '%{http_code}' \
        --header 'X-Auth-User: ci-gateway' \
        --header 'X-Auth-Role: ADMIN' \
        --header "X-Auth-Hotel: ${hotel_id}" \
        --header "X-Auth-Timestamp: ${timestamp}" \
        --header "X-Auth-Nonce: ${nonce}" \
        --header "X-Internal-Signature: ${signature}" \
        "http://127.0.0.1:${port}/api/v1/rooms"
}

start_gateway() {
    local container="$1" image="$2" app_port="$3" management_port="$4" java_options="$5"
    docker run --detach --name "${container}" --network "${NETWORK_NAME}" \
        --publish "${app_port}:8080" --publish "${management_port}:8090" \
        --env SPRING_PROFILES_ACTIVE=api-gateway \
        --env CONFIG_SERVER_URL=http://gateway-native-config:8888 \
        --env CONFIG_SERVER_USERNAME=configuser \
        --env CONFIG_SERVER_PASSWORD="${CI_CONFIG_PASSWORD}" \
        --env GW_REDIS_HOST=gateway-native-redis \
        --env INTERNAL_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" \
        --env SPRING_DATA_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" \
        --env JWT_SECRET="${CI_JWT_SECRET}" \
        --env INTERNAL_HMAC_SECRET="${CI_HMAC_SECRET}" \
        --env GW_AUTH_SERVICE_URI=http://auth-service:8087 \
        --env GW_FRONTDESK_SERVICE_URI=http://frontdesk-service:8081 \
        --env GW_GUEST_SERVICE_URI=http://frontdesk-service:8081 \
        --env GW_BILLING_SERVICE_URI=http://frontdesk-service:8081 \
        --env GW_FB_SERVICE_URI=http://frontdesk-service:8081 \
        --env "JAVA_TOOL_OPTIONS=${java_options}" \
        "${image}" >/dev/null
}

docker network create "${NETWORK_NAME}" >/dev/null

echo "Starting real Config Server, PostgreSQL and Redis"
docker run --detach --name "${CONFIG_CONTAINER}" --network "${NETWORK_NAME}" \
    --publish 18888:8888 --publish 18091:8090 \
    --env "CONFIG_SERVER_PASSWORD=${CI_CONFIG_PASSWORD}" \
    --env "JWT_SECRET=${CI_JWT_SECRET}" \
    --env "INTERNAL_HMAC_SECRET=${CI_HMAC_SECRET}" \
    --env "INTERNAL_REDIS_PASSWORD=${CI_REDIS_PASSWORD}" \
    --env 'JAVA_TOOL_OPTIONS=-Xmx256m -XX:MaxMetaspaceSize=128m -XX:+ExitOnOutOfMemoryError' \
    hotel-pms/config-service:ci >/dev/null
wait_for_health config-service http://127.0.0.1:18091/actuator/health 90 "${CONFIG_CONTAINER}"

docker run --detach --name "${POSTGRES_CONTAINER}" --network "${NETWORK_NAME}" \
    --env POSTGRES_USER=postgres --env "POSTGRES_PASSWORD=${CI_POSTGRES_PASSWORD}" \
    postgres:15-alpine >/dev/null
docker run --detach --name "${REDIS_CONTAINER}" --network "${NETWORK_NAME}" \
    redis:8.8.1-alpine redis-server --requirepass "${CI_REDIS_PASSWORD}" >/dev/null

for _ in {1..60}; do
    if docker exec "${POSTGRES_CONTAINER}" pg_isready --username postgres >/dev/null 2>&1; then
        break
    fi
    sleep 2
done
docker exec "${POSTGRES_CONTAINER}" pg_isready --username postgres >/dev/null
docker exec "${POSTGRES_CONTAINER}" createdb --username postgres hotel_auth
docker exec "${POSTGRES_CONTAINER}" createdb --username postgres hotel_frontdesk
for _ in {1..30}; do
    if docker exec "${REDIS_CONTAINER}" redis-cli --pass "${CI_REDIS_PASSWORD}" ping 2>/dev/null | grep -qx PONG; then
        break
    fi
    sleep 1
done
docker exec "${REDIS_CONTAINER}" redis-cli --pass "${CI_REDIS_PASSWORD}" ping | grep -qx PONG

echo "Starting real JVM Auth and Frontdesk services"
docker run --detach --name "${AUTH_CONTAINER}" --network "${NETWORK_NAME}" \
    --network-alias auth-service --env SPRING_PROFILES_ACTIVE=auth-service \
    --env CONFIG_SERVER_URL=http://gateway-native-config:8888 \
    --env CONFIG_SERVER_PASSWORD="${CI_CONFIG_PASSWORD}" \
    --env AUTH_REDIS_HOST=gateway-native-redis \
    --env INTERNAL_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" \
    --env SPRING_DATA_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" \
    --env SPRING_DATASOURCE_URL=jdbc:postgresql://gateway-native-postgres:5432/hotel_auth \
    --env SPRING_DATASOURCE_USERNAME=postgres \
    --env SPRING_DATASOURCE_PASSWORD="${CI_POSTGRES_PASSWORD}" \
    --env JWT_SECRET="${CI_JWT_SECRET}" --env INTERNAL_HMAC_SECRET="${CI_HMAC_SECRET}" \
    --publish 18087:8087 --publish 18092:8090 \
    --env 'JAVA_TOOL_OPTIONS=-Xmx512m -XX:MaxMetaspaceSize=256m -XX:+ExitOnOutOfMemoryError' \
    hotel-pms/auth-service:ci >/dev/null

docker run --detach --name "${FRONTDESK_CONTAINER}" --network "${NETWORK_NAME}" \
    --network-alias frontdesk-service --env SPRING_PROFILES_ACTIVE=frontdesk-service \
    --env CONFIG_SERVER_URL=http://gateway-native-config:8888 \
    --env CONFIG_SERVER_PASSWORD="${CI_CONFIG_PASSWORD}" \
    --env INTERNAL_REDIS_HOST=gateway-native-redis \
    --env INTERNAL_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" \
    --env SPRING_DATA_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" \
    --env SPRING_DATASOURCE_URL=jdbc:postgresql://gateway-native-postgres:5432/hotel_frontdesk \
    --env SPRING_DATASOURCE_USERNAME=postgres \
    --env SPRING_DATASOURCE_PASSWORD="${CI_POSTGRES_PASSWORD}" \
    --env INTERNAL_HMAC_SECRET="${CI_HMAC_SECRET}" \
    --env ALLOGGIATI_USERNAME=ci_placeholder_user --env ALLOGGIATI_PASSWORD=ci_placeholder_password \
    --env ALLOGGIATI_WS_KEY=ci_placeholder_wskey --env ALLOGGIATI_DRY_RUN=true \
    --env ALLOGGIATI_CREDENTIALS_ENCRYPTION_KEY=ci_placeholder_encryption_key \
    --env ALLOGGIATI_CREDENTIALS_ENCRYPTION_SALT=deadbeefdeadbeefdeadbeefdeadbeef \
    --publish 18081:8081 --publish 18093:8090 \
    --env 'JAVA_TOOL_OPTIONS=-Xmx512m -XX:MaxMetaspaceSize=256m -XX:+ExitOnOutOfMemoryError' \
    hotel-pms/frontdesk-service:ci >/dev/null

wait_for_health auth-service http://127.0.0.1:18092/actuator/health 150 "${AUTH_CONTAINER}"
wait_for_health frontdesk-service http://127.0.0.1:18093/actuator/health 150 "${FRONTDESK_CONTAINER}"

echo "Starting Native gateway"
native_started_ms="$(date +%s%3N)"
start_gateway "${NATIVE_CONTAINER}" hotel-pms/api-gateway-native:ci 18080 18090 \
    '-Xmx512m -XX:MaxMetaspaceSize=256m -XX:+ExitOnOutOfMemoryError'
wait_for_health api-gateway-native http://127.0.0.1:18090/actuator/health 150 "${NATIVE_CONTAINER}"
native_ready_ms="$(date +%s%3N)"
native_startup_ms="$((native_ready_ms - native_started_ms))"
curl --silent --show-error --fail http://127.0.0.1:18090/actuator/health/liveness \
    > "${RESULT_DIR}/native-liveness.json"
curl --silent --show-error --fail http://127.0.0.1:18090/actuator/health/readiness \
    > "${RESULT_DIR}/native-readiness.json"
jq -e '.status == "UP"' "${RESULT_DIR}/native-liveness.json" >/dev/null
jq -e '.status == "UP"' "${RESULT_DIR}/native-readiness.json" >/dev/null

unauth_code="$(http_code "${RESULT_DIR}/native-unauthenticated.json" \
    --cookie-jar /dev/null http://127.0.0.1:18080/api/v1/rooms)"
[[ "${unauth_code}" == 401 ]]

echo "Validating real login, JWT, tenant header stripping and route forwarding"
admin_cookie="${RESULT_DIR}/admin.cookies"
: > "${admin_cookie}"
login_code="$(http_code "${RESULT_DIR}/native-login.json" \
    --cookie-jar "${admin_cookie}" --cookie "${admin_cookie}" \
    --header 'Content-Type: application/json' --request POST \
    --data '{"username":"e2e-live-other-hotel-admin","password":"password"}' \
    http://127.0.0.1:18080/api/v1/auth/login)"
[[ "${login_code}" == 200 ]]
jq -e '.mustChangePassword == false' "${RESULT_DIR}/native-login.json" >/dev/null
admin_csrf="$(cookie_value csrf_token "${admin_cookie}")"
[[ -n "${admin_csrf}" ]]

me_code="$(http_code "${RESULT_DIR}/native-me.json" --cookie "${admin_cookie}" \
    http://127.0.0.1:18080/api/v1/auth/me)"
[[ "${me_code}" == 200 ]]
jq -e '.username == "e2e-live-other-hotel-admin"' "${RESULT_DIR}/native-me.json" >/dev/null

spoofed_tenant_code="$(http_code "${RESULT_DIR}/native-tenant-spoof.json" \
    --cookie "${admin_cookie}" --header 'X-Auth-Hotel: 00000000-0000-0000-0000-000000000001' \
    http://127.0.0.1:18080/api/v1/auth/users)"
[[ "${spoofed_tenant_code}" == 200 ]]
jq -e 'any(.[]; .username == "e2e-live-other-hotel-admin")' \
    "${RESULT_DIR}/native-tenant-spoof.json" >/dev/null

echo "Validating RBAC with a real receptionist account"
create_user_code="$(http_code "${RESULT_DIR}/native-create-receptionist.json" \
    --cookie "${admin_cookie}" --header "X-CSRF-Token: ${admin_csrf}" \
    --header 'Content-Type: application/json' --request POST \
    --data '{"username":"native-receptionist","password":"Reception1A","email":"native-receptionist@example.test","role":"RECEPTIONIST"}' \
    http://127.0.0.1:18080/api/v1/auth/users)"
[[ "${create_user_code}" == 201 ]]

receptionist_cookie="${RESULT_DIR}/receptionist.cookies"
: > "${receptionist_cookie}"
receptionist_login_code="$(http_code "${RESULT_DIR}/native-receptionist-login.json" \
    --cookie-jar "${receptionist_cookie}" --cookie "${receptionist_cookie}" \
    --header 'Content-Type: application/json' --request POST \
    --data '{"username":"native-receptionist","password":"Reception1A"}' \
    http://127.0.0.1:18080/api/v1/auth/login)"
[[ "${receptionist_login_code}" == 200 ]]
receptionist_csrf="$(cookie_value csrf_token "${receptionist_cookie}")"
change_code="$(http_code "${RESULT_DIR}/native-receptionist-change-password.json" \
    --cookie "${receptionist_cookie}" --cookie-jar "${receptionist_cookie}" \
    --header "X-CSRF-Token: ${receptionist_csrf}" --header 'Content-Type: application/json' \
    --request POST --data '{"currentPassword":"Reception1A","newPassword":"Reception2B"}' \
    http://127.0.0.1:18080/api/v1/auth/change-password)"
[[ "${change_code}" == 200 ]]
receptionist_csrf="$(cookie_value csrf_token "${receptionist_cookie}")"
receptionist_read_code="$(http_code "${RESULT_DIR}/native-receptionist-rooms.json" \
    --cookie "${receptionist_cookie}" http://127.0.0.1:18080/api/v1/rooms)"
[[ "${receptionist_read_code}" == 200 ]]
receptionist_write_code="$(http_code "${RESULT_DIR}/native-receptionist-write-denied.json" \
    --cookie "${receptionist_cookie}" --header "X-CSRF-Token: ${receptionist_csrf}" \
    --header 'Content-Type: application/json' --request POST --data '{}' \
    http://127.0.0.1:18080/api/v1/room-types)"
[[ "${receptionist_write_code}" == 403 ]]

echo "Validating Redis rate limiting and downstream HMAC replay protection"
rate_limit_max=0
for _ in {1..12}; do
    code="$(http_code "${RESULT_DIR}/native-rate-limit-${RANDOM}.json" \
        --header 'Content-Type: application/json' --request POST --data '{}' \
        http://127.0.0.1:18080/api/v1/auth/login)"
    if [[ "${code}" -gt "${rate_limit_max}" ]]; then rate_limit_max="${code}"; fi
done
[[ "${rate_limit_max}" == 429 ]]

replay_nonce="$(openssl rand -hex 16)"
first_replay_code="$(signed_internal_request "${RESULT_DIR}/native-replay-first.json" 18081 \
    99999999-9999-9999-9999-999999999999 "${replay_nonce}")"
second_replay_code="$(signed_internal_request "${RESULT_DIR}/native-replay-second.json" 18081 \
    99999999-9999-9999-9999-999999999999 "${replay_nonce}")"
[[ "${first_replay_code}" == 200 && "${second_replay_code}" == 401 ]]
redis_replay_key="$(docker exec "${REDIS_CONTAINER}" redis-cli --pass "${CI_REDIS_PASSWORD}" \
    --scan --pattern 'internal-auth:nonce:*' | grep -F "${replay_nonce}" || true)"
[[ -n "${redis_replay_key}" ]]

native_idle_memory="$(docker stats --no-stream --format '{{.MemUsage}}|{{.MemPerc}}' "${NATIVE_CONTAINER}")"
native_image_size_bytes="$(docker image inspect hotel-pms/api-gateway-native:ci --format '{{.Size}}')"
docker stop "${NATIVE_CONTAINER}" >/dev/null

echo "Starting JVM gateway control"
jvm_started_ms="$(date +%s%3N)"
start_gateway "${JVM_CONTAINER}" hotel-pms/api-gateway-jvm:ci 28080 28090 \
    '-Xmx512m -XX:MaxMetaspaceSize=256m -XX:+ExitOnOutOfMemoryError'
wait_for_health api-gateway-jvm http://127.0.0.1:28090/actuator/health 150 "${JVM_CONTAINER}"
jvm_ready_ms="$(date +%s%3N)"
jvm_startup_ms="$((jvm_ready_ms - jvm_started_ms))"
jvm_unauth_code="$(http_code "${RESULT_DIR}/jvm-unauthenticated.json" \
    --cookie-jar /dev/null http://127.0.0.1:28080/api/v1/rooms)"
[[ "${jvm_unauth_code}" == 401 ]]
jvm_idle_memory="$(docker stats --no-stream --format '{{.MemUsage}}|{{.MemPerc}}' "${JVM_CONTAINER}")"
jvm_image_size_bytes="$(docker image inspect hotel-pms/api-gateway-jvm:ci --format '{{.Size}}')"

cat > "${RESULT_DIR}/metrics.txt" <<METRICS
native_build_mode=${CI_NATIVE_BUILD_MODE:-unknown}
native_startup_ms=${native_startup_ms}
native_idle_memory=${native_idle_memory}
native_image_size_bytes=${native_image_size_bytes}
native_health_status=UP
native_liveness=UP
native_readiness=UP
native_unauthenticated=${unauth_code}
native_login=${login_code}
native_auth_me=${me_code}
native_tenant_spoof_header=${spoofed_tenant_code}
native_receptionist_read=${receptionist_read_code}
native_receptionist_write_denied=${receptionist_write_code}
native_redis_rate_limit_max=${rate_limit_max}
native_hmac_replay_first=${first_replay_code}
native_hmac_replay_second=${second_replay_code}
native_hmac_replay_nonce_persisted=PASS
jvm_startup_ms=${jvm_startup_ms}
jvm_idle_memory=${jvm_idle_memory}
jvm_image_size_bytes=${jvm_image_size_bytes}
jvm_health_status=UP
jvm_unauthenticated=${jvm_unauth_code}
METRICS

cat "${RESULT_DIR}/metrics.txt"
