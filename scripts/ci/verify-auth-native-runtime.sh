#!/usr/bin/env bash
set -Eeuo pipefail

RESULT_DIR="${GITHUB_WORKSPACE:-$(pwd)}/build/auth-native-runtime"
NETWORK_NAME="auth-native-ci"
POSTGRES_CONTAINER="auth-native-postgres"
REDIS_CONTAINER="auth-native-redis"
CONFIG_CONTAINER="auth-native-config"
NATIVE_CONTAINER="auth-service-native"
JVM_CONTAINER="auth-service-jvm"

: "${CI_POSTGRES_PASSWORD:?CI_POSTGRES_PASSWORD is required}"
: "${CI_REDIS_PASSWORD:?CI_REDIS_PASSWORD is required}"
: "${CI_CONFIG_PASSWORD:?CI_CONFIG_PASSWORD is required}"
: "${CI_HMAC_SECRET:?CI_HMAC_SECRET is required}"
: "${CI_JWT_SECRET:?CI_JWT_SECRET is required}"

mkdir -p "${RESULT_DIR}"
failure_class="PASS"

collect_evidence() {
    docker ps -a > "${RESULT_DIR}/docker-ps.txt" 2>&1 || true
    for container in "${CONFIG_CONTAINER}" "${POSTGRES_CONTAINER}" "${REDIS_CONTAINER}" \
        "${NATIVE_CONTAINER}" "${JVM_CONTAINER}"; do
        docker logs "${container}" > "${RESULT_DIR}/${container}.log" 2>&1 || true
    done
}

on_exit() {
    local status=$?
    collect_evidence
    printf '%s\n' "${failure_class}" > "${RESULT_DIR}/failure-class.txt"
    if [[ "${status}" -ne 0 ]]; then
        echo "${failure_class}: auth-service Native runtime validation did not complete" >&2
    fi
    return "${status}"
}
trap on_exit EXIT

global_fail() {
    failure_class="CI_GLOBAL_FAIL"
    echo "CI_GLOBAL_FAIL: $*" >&2
    exit 1
}

native_fail() {
    failure_class="NATIVE_GATE_FAIL"
    echo "NATIVE_GATE_FAIL: $*" >&2
    exit 1
}

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

cookie_value() {
    local jar="$1"
    local name="$2"
    awk -v wanted="${name}" '($0 !~ /^#/ || $0 ~ /^#HttpOnly_/) && $6 == wanted { value = $7 } END { print value }' "${jar}"
}

jwt_payload() {
    local token="$1"
    local encoded
    encoded="$(cut -d. -f2 <<<"${token}")"
    case $(( ${#encoded} % 4 )) in
        2) encoded+="==" ;;
        3) encoded+="=" ;;
        1) return 1 ;;
    esac
    printf '%s' "${encoded}" | tr '_-' '/+' | base64 --decode
}

signed_request() {
    local method="$1"
    local url="$2"
    local username="$3"
    local role="$4"
    local hotel_id="$5"
    local nonce="$6"
    local output_file="$7"
    local body="${8:-}"
    local timestamp signature
    timestamp="$(date +%s%3N)"
    signature="$(printf '%s' "${username}:${role}:${hotel_id}:${timestamp}:${nonce}" \
        | openssl dgst -sha256 -hmac "${CI_HMAC_SECRET}" -r | awk '{print $1}')"

    local curl_args=(
        --silent --show-error --output "${output_file}" --write-out '%{http_code}'
        --request "${method}"
        --header "X-Auth-User: ${username}"
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

run_basic_flow() {
    local scope="$1"
    local port="$2"
    local prefix="$3"
    local jar="${RESULT_DIR}/${prefix}-cookies.txt"
    local access_token old_refresh old_jti new_refresh new_jti claims me_body
    local login_code refresh_code me_code redis_tv redis_blacklist

    login_code="$(curl --silent --show-error --output "${RESULT_DIR}/${prefix}-login.json" \
        --write-out '%{http_code}' --cookie-jar "${jar}" \
        --header 'Content-Type: application/json' --data '{"username":"admin","password":"password"}' \
        "http://127.0.0.1:${port}/api/v1/auth/login" || true)"
    if [[ "${login_code}" != "200" ]]; then
        [[ "${scope}" == native ]] && native_fail "login returned HTTP ${login_code}"
        global_fail "JVM control login returned HTTP ${login_code}"
    fi
    jq -e '.mustChangePassword == true' "${RESULT_DIR}/${prefix}-login.json" >/dev/null \
        || { [[ "${scope}" == native ]] && native_fail "login body lost mustChangePassword"; global_fail "JVM login body lost mustChangePassword"; }

    access_token="$(cookie_value "${jar}" jwt)"
    [[ -n "${access_token}" ]] \
        || { [[ "${scope}" == native ]] && native_fail "login did not set the access JWT cookie"; global_fail "JVM login did not set the access JWT cookie"; }
    jwt_payload "${access_token}" > "${RESULT_DIR}/${prefix}-jwt-claims.json" \
        || { [[ "${scope}" == native ]] && native_fail "access JWT payload could not be decoded"; global_fail "JVM access JWT payload could not be decoded"; }
    claims="$(<"${RESULT_DIR}/${prefix}-jwt-claims.json")"
    jq -e '.sub == "admin" and .role == "ADMIN" and .hotelId == "00000000-0000-0000-0000-000000000001" and .mustChangePassword == true' \
        <<<"${claims}" >/dev/null \
        || { [[ "${scope}" == native ]] && native_fail "JWT claims do not prove subject, role, tenant, and password-change state"; global_fail "JVM JWT claims are incomplete"; }

    me_code="$(curl --silent --show-error --output "${RESULT_DIR}/${prefix}-me.json" \
        --write-out '%{http_code}' --cookie "${jar}" \
        "http://127.0.0.1:${port}/api/v1/auth/me" || true)"
    if [[ "${me_code}" != "200" ]]; then
        [[ "${scope}" == native ]] && native_fail "/auth/me returned HTTP ${me_code}"
        global_fail "JVM /auth/me returned HTTP ${me_code}"
    fi
    me_body="$(<"${RESULT_DIR}/${prefix}-me.json")"
    jq -e '.username == "admin" and .role == "ADMIN" and .mustChangePassword == true' <<<"${me_body}" >/dev/null \
        || { [[ "${scope}" == native ]] && native_fail "/auth/me body does not match JWT/user state"; global_fail "JVM /auth/me body is incomplete"; }

    old_refresh="$(cookie_value "${jar}" refresh_token)"
    [[ -n "${old_refresh}" ]] \
        || { [[ "${scope}" == native ]] && native_fail "login did not set the refresh cookie"; global_fail "JVM login did not set the refresh cookie"; }
    jwt_payload "${old_refresh}" > "${RESULT_DIR}/${prefix}-refresh-before-claims.json"
    old_jti="$(jq -er '.jti' "${RESULT_DIR}/${prefix}-refresh-before-claims.json")"
    refresh_code="$(curl --silent --show-error --output "${RESULT_DIR}/${prefix}-refresh.json" \
        --write-out '%{http_code}' --cookie "${jar}" --cookie-jar "${jar}" \
        --request POST "http://127.0.0.1:${port}/api/v1/auth/refresh" || true)"
    if [[ "${refresh_code}" != "200" ]]; then
        [[ "${scope}" == native ]] && native_fail "/auth/refresh returned HTTP ${refresh_code}"
        global_fail "JVM /auth/refresh returned HTTP ${refresh_code}"
    fi
    new_refresh="$(cookie_value "${jar}" refresh_token)"
    jwt_payload "${new_refresh}" > "${RESULT_DIR}/${prefix}-refresh-after-claims.json"
    new_jti="$(jq -er '.jti' "${RESULT_DIR}/${prefix}-refresh-after-claims.json")"
    [[ "${new_jti}" != "${old_jti}" ]] \
        || { [[ "${scope}" == native ]] && native_fail "refresh did not rotate the refresh JTI"; global_fail "JVM refresh did not rotate the refresh JTI"; }

    redis_tv="$(docker exec "${REDIS_CONTAINER}" redis-cli --pass "${CI_REDIS_PASSWORD}" exists 'user:tv:admin' 2>/dev/null || true)"
    redis_blacklist="$(docker exec "${REDIS_CONTAINER}" redis-cli --pass "${CI_REDIS_PASSWORD}" exists "rt:blacklist:${old_jti}" 2>/dev/null || true)"
    if [[ "${scope}" == native ]]; then
        [[ "${redis_tv}" == "1" ]] || native_fail "Redis token-version state was not written by login"
        [[ "${redis_blacklist}" == "1" ]] || native_fail "Redis refresh blacklist state was not written"
    else
        [[ "${redis_tv}" == "1" ]] || global_fail "JVM Redis token-version state was not written"
        [[ "${redis_blacklist}" == "1" ]] || global_fail "JVM Redis refresh blacklist state was not written"
    fi

    printf '%s\n' "${login_code}" > "${RESULT_DIR}/${prefix}-login-code.txt"
    printf '%s\n' "${refresh_code}" > "${RESULT_DIR}/${prefix}-refresh-code.txt"
    printf '%s\n' "${me_code}" > "${RESULT_DIR}/${prefix}-me-code.txt"
    if [[ "${scope}" == native ]]; then
        native_login_code="${login_code}"
        native_refresh_code="${refresh_code}"
        native_me_code="${me_code}"
    else
        jvm_login_code="${login_code}"
        jvm_refresh_code="${refresh_code}"
        jvm_me_code="${me_code}"
    fi
}

echo "Starting Config Server"
for container in "${CONFIG_CONTAINER}" "${POSTGRES_CONTAINER}" "${REDIS_CONTAINER}" \
    "${NATIVE_CONTAINER}" "${JVM_CONTAINER}"; do
    docker rm -f "${container}" >/dev/null 2>&1 || true
done
docker network create "${NETWORK_NAME}" >/dev/null || global_fail "could not create Docker network"
docker run --detach --name "${CONFIG_CONTAINER}" --network "${NETWORK_NAME}" \
    --publish 18888:8888 --publish 18091:8090 \
    --env "CONFIG_SERVER_PASSWORD=${CI_CONFIG_PASSWORD}" \
    --env 'JAVA_TOOL_OPTIONS=-Xmx256m -XX:MaxMetaspaceSize=128m -XX:+ExitOnOutOfMemoryError' \
    hotel-pms/config-service:ci >/dev/null
wait_for_health config-service http://127.0.0.1:18091/actuator/health 90 "${CONFIG_CONTAINER}" \
    || global_fail "Config Server health gate failed"
curl --silent --show-error --fail --user "configuser:${CI_CONFIG_PASSWORD}" \
    http://127.0.0.1:18888/auth-service/default \
    | jq -e '.name == "auth-service"' >/dev/null \
    || global_fail "Config Server did not serve auth-service configuration"

echo "Starting PostgreSQL and Redis"
docker run --detach --name "${POSTGRES_CONTAINER}" --network "${NETWORK_NAME}" \
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
[[ "${postgres_ready_streak}" -ge 2 ]] || global_fail "PostgreSQL did not remain ready"
for database in hotel_auth hotel_auth_jvm; do
    docker exec "${POSTGRES_CONTAINER}" createdb --username postgres "${database}" \
        || global_fail "could not create ${database}"
done
for _ in {1..30}; do
    if docker exec "${REDIS_CONTAINER}" redis-cli --pass "${CI_REDIS_PASSWORD}" ping 2>/dev/null | grep -qx PONG; then
        break
    fi
    sleep 1
done
docker exec "${REDIS_CONTAINER}" redis-cli --pass "${CI_REDIS_PASSWORD}" ping 2>/dev/null \
    | grep -qx PONG || global_fail "Redis authentication/readiness gate failed"

hotel_a="00000000-0000-0000-0000-000000000001"
hotel_b="99999999-9999-9999-9999-999999999999"

echo "Starting auth-service Native Image"
native_start_ms="$(date +%s%3N)"
docker run --detach --name "${NATIVE_CONTAINER}" --network "${NETWORK_NAME}" \
    --publish 18087:8087 --publish 18090:8090 \
    --env SPRING_PROFILES_ACTIVE=auth-service \
    --env CONFIG_SERVER_URL=http://${CONFIG_CONTAINER}:8888 \
    --env CONFIG_SERVER_USERNAME=configuser --env "CONFIG_SERVER_PASSWORD=${CI_CONFIG_PASSWORD}" \
    --env AUTH_REDIS_HOST="${REDIS_CONTAINER}" \
    --env INTERNAL_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" \
    --env SPRING_DATA_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" \
    --env SPRING_DATASOURCE_URL=jdbc:postgresql://${POSTGRES_CONTAINER}:5432/hotel_auth \
    --env SPRING_DATASOURCE_USERNAME=postgres --env "SPRING_DATASOURCE_PASSWORD=${CI_POSTGRES_PASSWORD}" \
    --env "INTERNAL_HMAC_SECRET=${CI_HMAC_SECRET}" --env "JWT_SECRET=${CI_JWT_SECRET}" \
    hotel-pms/auth-service-native:ci >/dev/null
wait_for_health auth-service-native http://127.0.0.1:18090/actuator/health 150 "${NATIVE_CONTAINER}" \
    || native_fail "Native service health/liveness gate failed"
native_ready_ms="$(date +%s%3N)"
native_startup_ms="$((native_ready_ms - native_start_ms))"
native_health="$(curl --silent --show-error --fail http://127.0.0.1:18090/actuator/health)" \
    || native_fail "Native health endpoint was not readable"
jq -e '.status == "UP"' <<<"${native_health}" >/dev/null || native_fail "Native health was not UP"
printf '%s\n' "${native_health}" > "${RESULT_DIR}/native-health.json"
native_memory="$(docker stats --no-stream --format '{{.MemUsage}}|{{.MemPerc}}' "${NATIVE_CONTAINER}")"

run_basic_flow native 18087 native

missing_hmac_code="$(curl --silent --output "${RESULT_DIR}/native-missing-hmac.json" \
    --write-out '%{http_code}' http://127.0.0.1:18087/api/v1/auth/users || true)"
[[ "${missing_hmac_code}" == "401" ]] || native_fail "missing internal HMAC returned HTTP ${missing_hmac_code}"

reception_code="$(signed_request GET http://127.0.0.1:18087/api/v1/auth/users \
    ci-receptionist RECEPTIONIST "${hotel_a}" "$(openssl rand -hex 16)" \
    "${RESULT_DIR}/native-rbac-receptionist.json")"
[[ "${reception_code}" == "403" ]] || native_fail "RECEPTIONIST user-management access returned HTTP ${reception_code}"

admin_code="$(signed_request GET http://127.0.0.1:18087/api/v1/auth/users \
    ci-admin ADMIN "${hotel_a}" "$(openssl rand -hex 16)" \
    "${RESULT_DIR}/native-admin-users.json")"
[[ "${admin_code}" == "200" ]] || native_fail "ADMIN user-management access returned HTTP ${admin_code}"

user_payload='{"username":"native-auth-tenant-a","password":"NativeAuth2026!!","email":"native-auth-tenant-a@example.test","role":"RECEPTIONIST"}'
create_user_code="$(signed_request POST http://127.0.0.1:18087/api/v1/auth/users \
    ci-admin ADMIN "${hotel_a}" "$(openssl rand -hex 16)" \
    "${RESULT_DIR}/native-created-user.json" "${user_payload}")"
[[ "${create_user_code}" == "201" ]] || native_fail "ADMIN user creation returned HTTP ${create_user_code}"
jq -e '.username == "native-auth-tenant-a" and .role == "RECEPTIONIST"' \
    "${RESULT_DIR}/native-created-user.json" >/dev/null \
    || native_fail "created user response did not preserve the real contract"

cross_tenant_code="$(signed_request GET http://127.0.0.1:18087/api/v1/auth/users \
    ci-admin ADMIN "${hotel_b}" "$(openssl rand -hex 16)" \
    "${RESULT_DIR}/native-admin-users-hotel-b.json")"
[[ "${cross_tenant_code}" == "200" ]] || native_fail "hotel B user list returned HTTP ${cross_tenant_code}"
jq -e 'all(.[]; .username != "native-auth-tenant-a")' \
    "${RESULT_DIR}/native-admin-users-hotel-b.json" >/dev/null \
    || native_fail "hotel A user leaked into hotel B user management"

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
replay_first_code="$(curl --silent --output "${RESULT_DIR}/native-replay-first.json" \
    --write-out '%{http_code}' "${replay_headers[@]}" \
    http://127.0.0.1:18087/api/v1/auth/users)"
replay_second_code="$(curl --silent --output "${RESULT_DIR}/native-replay-second.json" \
    --write-out '%{http_code}' "${replay_headers[@]}" \
    http://127.0.0.1:18087/api/v1/auth/users)"
[[ "${replay_first_code}" == "200" && "${replay_second_code}" == "401" ]] \
    || native_fail "HMAC nonce replay gate returned ${replay_first_code}/${replay_second_code}"
nonce_exists="$(docker exec "${REDIS_CONTAINER}" redis-cli --pass "${CI_REDIS_PASSWORD}" \
    exists "internal-auth:nonce:${replay_nonce}" 2>/dev/null || true)"
[[ "${nonce_exists}" == "1" ]] || native_fail "Redis did not retain the consumed HMAC nonce"

native_flyway_latest="$(docker exec "${POSTGRES_CONTAINER}" psql --username postgres --dbname hotel_auth \
    --tuples-only --no-align --command 'select max(version::integer) from flyway_schema_history where success = true;')"
[[ "${native_flyway_latest}" == "8" ]] || native_fail "Flyway latest version was ${native_flyway_latest}, expected 8"
native_migration_count="$(docker exec "${POSTGRES_CONTAINER}" psql --username postgres --dbname hotel_auth \
    --tuples-only --no-align --command 'select count(*) from flyway_schema_history where success = true;')"
[[ "${native_migration_count}" == "8" ]] || native_fail "Flyway applied ${native_migration_count} successful migrations, expected 8"
native_image_size_bytes="$(docker image inspect hotel-pms/auth-service-native:ci --format '{{.Size}}')"

docker stop "${NATIVE_CONTAINER}" >/dev/null || native_fail "could not stop Native container for JVM control"

echo "Starting auth-service JVM control"
jvm_start_ms="$(date +%s%3N)"
docker run --detach --name "${JVM_CONTAINER}" --network "${NETWORK_NAME}" \
    --publish 28087:8087 --publish 28090:8090 \
    --env SPRING_PROFILES_ACTIVE=auth-service \
    --env CONFIG_SERVER_URL=http://${CONFIG_CONTAINER}:8888 \
    --env CONFIG_SERVER_USERNAME=configuser --env "CONFIG_SERVER_PASSWORD=${CI_CONFIG_PASSWORD}" \
    --env AUTH_REDIS_HOST="${REDIS_CONTAINER}" \
    --env INTERNAL_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" \
    --env SPRING_DATA_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" \
    --env SPRING_DATASOURCE_URL=jdbc:postgresql://${POSTGRES_CONTAINER}:5432/hotel_auth_jvm \
    --env SPRING_DATASOURCE_USERNAME=postgres --env "SPRING_DATASOURCE_PASSWORD=${CI_POSTGRES_PASSWORD}" \
    --env "INTERNAL_HMAC_SECRET=${CI_HMAC_SECRET}" --env "JWT_SECRET=${CI_JWT_SECRET}" \
    --env 'JAVA_TOOL_OPTIONS=-Xmx512m -XX:MaxMetaspaceSize=256m -XX:+ExitOnOutOfMemoryError' \
    hotel-pms/auth-service-jvm:ci >/dev/null
wait_for_health auth-service-jvm http://127.0.0.1:28090/actuator/health 120 "${JVM_CONTAINER}" \
    || global_fail "JVM control health/liveness gate failed"
jvm_ready_ms="$(date +%s%3N)"
jvm_startup_ms="$((jvm_ready_ms - jvm_start_ms))"
jvm_health="$(curl --silent --show-error --fail http://127.0.0.1:28090/actuator/health)" \
    || global_fail "JVM health endpoint was not readable"
printf '%s\n' "${jvm_health}" > "${RESULT_DIR}/jvm-health.json"
jvm_memory="$(docker stats --no-stream --format '{{.MemUsage}}|{{.MemPerc}}' "${JVM_CONTAINER}")"
run_basic_flow jvm 28087 jvm
jvm_flyway_latest="$(docker exec "${POSTGRES_CONTAINER}" psql --username postgres --dbname hotel_auth_jvm \
    --tuples-only --no-align --command 'select max(version::integer) from flyway_schema_history where success = true;')"
[[ "${jvm_flyway_latest}" == "8" ]] || global_fail "JVM control Flyway latest version was ${jvm_flyway_latest}"

cat > "${RESULT_DIR}/metrics.txt" <<METRICS
auth-service Native runtime evidence
native_build_mode=${CI_NATIVE_BUILD_MODE:-unknown}
native_health=UP
native_login=${native_login_code}
native_refresh=${native_refresh_code}
native_me=${native_me_code}
native_missing_hmac=${missing_hmac_code}
native_rbac_receptionist=${reception_code}
native_rbac_admin=${admin_code}
native_tenant_b_cross_list=${cross_tenant_code}
native_hmac_replay_first=${replay_first_code}
native_hmac_replay_second=${replay_second_code}
native_flyway_latest=${native_flyway_latest}
native_flyway_successful_migrations=${native_migration_count}
native_startup_ms=${native_startup_ms}
native_memory=${native_memory}
native_image_size_bytes=${native_image_size_bytes}
jvm_health=UP
jvm_login=${jvm_login_code:-unknown}
jvm_refresh=${jvm_refresh_code:-unknown}
jvm_me=${jvm_me_code:-unknown}
jvm_flyway_latest=${jvm_flyway_latest}
jvm_startup_ms=${jvm_startup_ms}
jvm_memory=${jvm_memory}
METRICS

cat "${RESULT_DIR}/metrics.txt"
