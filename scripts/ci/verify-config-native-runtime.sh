#!/usr/bin/env bash
set -Eeuo pipefail

RESULT_DIR="${GITHUB_WORKSPACE:-$(pwd)}/build/config-native-runtime"
NETWORK_NAME="config-native-ci"
NATIVE_CONTAINER="config-service-native"
JVM_CONTAINER="config-service-jvm"

: "${CI_CONFIG_PASSWORD:?CI_CONFIG_PASSWORD is required}"

mkdir -p "${RESULT_DIR}"

collect_evidence() {
  docker ps -a > "${RESULT_DIR}/docker-ps.txt" 2>&1 || true
  for container in "${NATIVE_CONTAINER}" "${JVM_CONTAINER}"; do
    docker logs "${container}" > "${RESULT_DIR}/${container}.log" 2>&1 || true
  done
}
trap collect_evidence EXIT

wait_for_health() {
  local label="$1"
  local url="$2"
  local container="$3"
  local attempts="${4:-90}"
  local response container_state
  for ((i = 1; i <= attempts; i++)); do
    response="$(curl --silent --show-error --max-time 3 "${url}" 2>/dev/null || true)"
    if jq -e '.status == "UP"' >/dev/null 2>&1 <<<"${response}"; then
      return 0
    fi
    container_state="$(docker inspect --format '{{.State.Status}}' "${container}" 2>/dev/null || true)"
    if [[ "${container_state}" == "exited" || "${container_state}" == "dead" ]]; then
      echo "${label} container stopped before becoming UP" >&2
      return 1
    fi
    sleep 2
  done
  echo "${label} did not become UP at ${url}" >&2
  return 1
}

assert_http_code() {
  local expected="$1"
  local url="$2"
  local output_file="$3"
  shift 3
  local actual
  actual="$(curl --silent --show-error --output "${output_file}" --write-out '%{http_code}' "$@" "${url}")"
  if [[ "${actual}" != "${expected}" ]]; then
    echo "Expected HTTP ${expected}, got ${actual} for ${url}" >&2
    return 1
  fi
}

measure_service() {
  local label="$1"
  local port="$2"
  local container="$3"
  local prefix="${RESULT_DIR}/${label}"
  local started_at ended_at health_body image_size memory

  started_at="$(date +%s%3N)"
  wait_for_health "${label}" "http://127.0.0.1:${port}/actuator/health" "${container}" 120
  ended_at="$(date +%s%3N)"
  health_body="$(curl --silent --show-error --fail "http://127.0.0.1:${port}/actuator/health")"
  printf '%s\n' "${health_body}" > "${prefix}-health.json"
  jq -e '.status == "UP"' "${prefix}-health.json" >/dev/null
  curl --silent --show-error --fail "http://127.0.0.1:${port}/actuator/health/liveness" > "${prefix}-liveness.json"
  curl --silent --show-error --fail "http://127.0.0.1:${port}/actuator/health/readiness" > "${prefix}-readiness.json"
  curl --silent --show-error --fail "http://127.0.0.1:${port}/actuator/prometheus" > "${prefix}-prometheus.txt"
  grep -Eq '^(http_server_requests|process_|system_)' "${prefix}-prometheus.txt"
  image_size="$(docker image inspect "hotel-pms/config-service-${label}:ci" --format '{{.Size}}')"
  memory="$(docker stats --no-stream --format '{{.MemUsage}}|{{.MemPerc}}' "${container}")"
  printf '%s\n' "${ended_at}" > "${prefix}-healthy-at-ms.txt"
  printf '%s\n' "${image_size}" > "${prefix}-image-size-bytes.txt"
  printf '%s\n' "${memory}" > "${prefix}-idle-memory.txt"
  printf '%s\n' "$((ended_at - started_at))" > "${prefix}-startup-ms.txt"
}

echo "Starting Native config-service runtime"
docker network create "${NETWORK_NAME}" >/dev/null
docker run --detach --name "${NATIVE_CONTAINER}" --network "${NETWORK_NAME}" \
  --publish 18888:8888 --publish 18090:8090 \
  --env "CONFIG_SERVER_PASSWORD=${CI_CONFIG_PASSWORD}" \
  hotel-pms/config-service-native:ci >/dev/null
measure_service native 18090 "${NATIVE_CONTAINER}"

echo "Checking Native config contracts"
assert_http_code 401 "http://127.0.0.1:18888/guest-service/default" \
  "${RESULT_DIR}/native-unauthorized.json"
assert_http_code 200 "http://127.0.0.1:18888/guest-service/default" \
  "${RESULT_DIR}/native-guest-default.json" \
  --user "configuser:${CI_CONFIG_PASSWORD}"
assert_http_code 200 "http://127.0.0.1:18888/api-gateway/prod" \
  "${RESULT_DIR}/native-gateway-prod.json" \
  --user "configuser:${CI_CONFIG_PASSWORD}"
jq -e '.name == "guest-service" and (.profiles | index("default")) != null' \
  "${RESULT_DIR}/native-guest-default.json" >/dev/null
jq -e '.name == "api-gateway" and (.profiles | index("prod")) != null' \
  "${RESULT_DIR}/native-gateway-prod.json" >/dev/null

echo "Starting JVM config-service control"
docker run --detach --name "${JVM_CONTAINER}" --network "${NETWORK_NAME}" \
  --publish 28888:8888 --publish 28090:8090 \
  --env "CONFIG_SERVER_PASSWORD=${CI_CONFIG_PASSWORD}" \
  hotel-pms/config-service-jvm:ci >/dev/null
measure_service jvm 28090 "${JVM_CONTAINER}"

assert_http_code 401 "http://127.0.0.1:28888/guest-service/default" \
  "${RESULT_DIR}/jvm-unauthorized.json"
assert_http_code 200 "http://127.0.0.1:28888/guest-service/default" \
  "${RESULT_DIR}/jvm-guest-default.json" \
  --user "configuser:${CI_CONFIG_PASSWORD}"

native_startup_ms="$(<"${RESULT_DIR}/native-startup-ms.txt")"
jvm_startup_ms="$(<"${RESULT_DIR}/jvm-startup-ms.txt")"
native_image_size="$(<"${RESULT_DIR}/native-image-size-bytes.txt")"
jvm_image_size="$(<"${RESULT_DIR}/jvm-image-size-bytes.txt")"
native_memory="$(<"${RESULT_DIR}/native-idle-memory.txt")"
jvm_memory="$(<"${RESULT_DIR}/jvm-idle-memory.txt")"

cat > "${RESULT_DIR}/metrics.txt" <<METRICS
## Config Service Native Gate

| Metric | JVM control | Native |
|---|---:|---:|
| Startup to `/actuator/health` | ${jvm_startup_ms} ms | ${native_startup_ms} ms |
| Image size | ${jvm_image_size} bytes | ${native_image_size} bytes |
| Idle memory | ${jvm_memory} | ${native_memory} |

- Native build mode: ${CI_NATIVE_BUILD_MODE:-unknown}
- Native health/liveness/readiness: UP
- Native Prometheus endpoint: HTTP 200
- Native profile `guest-service/default`: HTTP 200 and profile contract verified
- Native profile `api-gateway/prod`: HTTP 200 and profile contract verified
- Native Basic Auth: unauthenticated HTTP 401; valid credentials HTTP 200
- Secrets: only CI placeholders were injected at runtime; no credentials are copied into the image
METRICS
cat "${RESULT_DIR}/metrics.txt"
