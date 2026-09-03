#!/usr/bin/env bash
set -Eeuo pipefail

RESULT_DIR="${RESULT_DIR:-build/config-native-runtime}"
NATIVE_IMAGE="${NATIVE_IMAGE:-hotel-pms/config-service-native:ci}"
JVM_IMAGE="${JVM_IMAGE:-hotel-pms/config-service-jvm:ci}"
CI_CONFIG_USERNAME="${CI_CONFIG_USERNAME:-configuser}"
CI_CONFIG_PASSWORD="${CI_CONFIG_PASSWORD:-native-ci-config-password}"
NETWORK="config-native-ci"
NATIVE_CONTAINER="config-service-native"
JVM_CONTAINER="config-service-jvm"
NATIVE_PORT=28888
NATIVE_MANAGEMENT_PORT=28090
JVM_PORT=28889
JVM_MANAGEMENT_PORT=28091

mkdir -p "${RESULT_DIR}"
exec > >(tee "${RESULT_DIR}/gate.log") 2>&1

cleanup() {
  docker rm --force "${NATIVE_CONTAINER}" "${JVM_CONTAINER}" >/dev/null 2>&1 || true
  docker network rm "${NETWORK}" >/dev/null 2>&1 || true
}
trap cleanup EXIT

fail_gate() {
  echo "NATIVE_GATE_FAIL: $*" >&2
  exit 1
}

wait_for_health() {
  local base_url="$1"
  local log_file="$2"
  local start_ms
  local now_ms
  start_ms="$(date +%s%3N)"
  for _ in $(seq 1 90); do
    if curl --silent --fail "${base_url}/actuator/health" >"${log_file}"; then
      now_ms="$(date +%s%3N)"
      printf '%s\n' "$((now_ms - start_ms))"
      return 0
    fi
    sleep 2
  done
  return 1
}

assert_http() {
  local expected="$1"
  local url="$2"
  local output="$3"
  shift 3
  local actual
  actual="$(curl --silent --show-error --output "${output}" --write-out '%{http_code}' "$@" "${url}")"
  [[ "${actual}" == "${expected}" ]] || fail_gate "expected HTTP ${expected} from ${url}, got ${actual}"
}

check_config_contract() {
  local prefix="$1"
  local main_port="$2"
  local management_port="$3"
  local startup_file="${RESULT_DIR}/${prefix}-startup-health.json"

  echo "Checking ${prefix} health and Config Server contract" >&2
  startup_ms="$(wait_for_health "http://127.0.0.1:${management_port}" "${startup_file}")" \
    || fail_gate "${prefix} management health did not become available"

  jq -e '.status == "UP"' "${startup_file}" >/dev/null \
    || fail_gate "${prefix} health was not UP"
  assert_http 200 "http://127.0.0.1:${management_port}/actuator/health/liveness" \
    "${RESULT_DIR}/${prefix}-liveness.json"
  assert_http 200 "http://127.0.0.1:${management_port}/actuator/health/readiness" \
    "${RESULT_DIR}/${prefix}-readiness.json"
  assert_http 200 "http://127.0.0.1:${management_port}/actuator/prometheus" \
    "${RESULT_DIR}/${prefix}-prometheus.txt"
  assert_http 401 "http://127.0.0.1:${main_port}/auth-service/default" \
    "${RESULT_DIR}/${prefix}-unauthenticated.json"
  assert_http 401 "http://127.0.0.1:${main_port}/auth-service/default" \
    "${RESULT_DIR}/${prefix}-wrong-password.json" \
    --user "${CI_CONFIG_USERNAME}:wrong-password"
  assert_http 200 "http://127.0.0.1:${main_port}/auth-service/default" \
    "${RESULT_DIR}/${prefix}-authenticated.json" \
    --user "${CI_CONFIG_USERNAME}:${CI_CONFIG_PASSWORD}"
  jq -e '.name == "auth-service" and (.propertySources | length) > 0' \
    "${RESULT_DIR}/${prefix}-authenticated.json" >/dev/null \
    || fail_gate "${prefix} did not serve the native profile configuration"

  printf '%s\n' "${startup_ms}"
}

[[ "${CI_CONFIG_PASSWORD}" != "" ]] || fail_gate "CI_CONFIG_PASSWORD is empty"
if rg -n --glob '*.yml' --glob '*.yaml' \
    '^[[:space:]]*(password|secret|token|private-key):[[:space:]]*[^$"[:space:]]' \
    config-service/src/main/resources; then
  fail_gate "a literal sensitive value is present in config-service resources"
fi
if rg -n '^(ARG|ENV)[[:space:]].*(PASSWORD|SECRET|TOKEN)' config-service/Dockerfile.native; then
  fail_gate "native image Dockerfile declares a secret-bearing ENV or ARG"
fi

docker network create "${NETWORK}" >/dev/null
docker run --detach --name "${NATIVE_CONTAINER}" --network "${NETWORK}" \
  --publish "${NATIVE_PORT}:8888" --publish "${NATIVE_MANAGEMENT_PORT}:8090" \
  --env "CONFIG_SERVER_USERNAME=${CI_CONFIG_USERNAME}" \
  --env "CONFIG_SERVER_PASSWORD=${CI_CONFIG_PASSWORD}" \
  "${NATIVE_IMAGE}" >/dev/null
docker run --detach --name "${JVM_CONTAINER}" --network "${NETWORK}" \
  --publish "${JVM_PORT}:8888" --publish "${JVM_MANAGEMENT_PORT}:8090" \
  --env "CONFIG_SERVER_USERNAME=${CI_CONFIG_USERNAME}" \
  --env "CONFIG_SERVER_PASSWORD=${CI_CONFIG_PASSWORD}" \
  "${JVM_IMAGE}" >/dev/null

native_startup_ms="$(check_config_contract native "${NATIVE_PORT}" "${NATIVE_MANAGEMENT_PORT}")"
jvm_startup_ms="$(check_config_contract jvm "${JVM_PORT}" "${JVM_MANAGEMENT_PORT}")"

native_image_size_bytes="$(docker image inspect "${NATIVE_IMAGE}" --format '{{.Size}}')"
jvm_image_size_bytes="$(docker image inspect "${JVM_IMAGE}" --format '{{.Size}}')"
native_memory="$(docker stats --no-stream --format '{{.MemUsage}}|{{.MemPerc}}' "${NATIVE_CONTAINER}")"
jvm_memory="$(docker stats --no-stream --format '{{.MemUsage}}|{{.MemPerc}}' "${JVM_CONTAINER}")"

cat > "${RESULT_DIR}/metrics.txt" <<METRICS
native_build_mode=${CI_NATIVE_BUILD_MODE:-unknown}
native_startup_ms=${native_startup_ms}
native_memory=${native_memory}
native_image_size_bytes=${native_image_size_bytes}
native_health_status=UP
native_liveness=200
native_readiness=200
native_prometheus=200
native_config_unauthenticated=401
native_config_wrong_password=401
native_config_authenticated=200
jvm_startup_ms=${jvm_startup_ms}
jvm_memory=${jvm_memory}
jvm_image_size_bytes=${jvm_image_size_bytes}
jvm_health_status=UP
jvm_liveness=200
jvm_readiness=200
jvm_prometheus=200
jvm_config_unauthenticated=401
jvm_config_wrong_password=401
jvm_config_authenticated=200
METRICS

cat "${RESULT_DIR}/metrics.txt"
