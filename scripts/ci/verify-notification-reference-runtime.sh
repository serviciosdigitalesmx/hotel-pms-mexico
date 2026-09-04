#!/usr/bin/env bash
set -Eeuo pipefail
: "${CI_CONFIG_PASSWORD:?}" "${CI_REDIS_PASSWORD:?}" "${CI_HMAC_SECRET:?}"
result_dir="${GITHUB_WORKSPACE:-$(pwd)}/build/notification-reference-runtime"
mkdir -p "$result_dir"
collect() {
  for container in notification-reference-native notification-reference-jvm notification-reference-config notification-reference-redis notification-reference-mail; do
    docker logs "$container" > "$result_dir/$container.log" 2>&1 || true
  done
}
trap collect EXIT
wait_health() {
  local url="$1"
  for _ in {1..120}; do
    if curl -fsS --max-time 2 "$url" 2>/dev/null | jq -e '.status == "UP"' >/dev/null; then return 0; fi
    sleep 1
  done
  echo "health not UP: $url" >&2
  return 1
}
docker network create notification-reference-ci >/dev/null
docker run -d --name notification-reference-config --network notification-reference-ci -p 18990:8090 \
  -e "CONFIG_SERVER_PASSWORD=$CI_CONFIG_PASSWORD" -e JAVA_TOOL_OPTIONS=-Xmx256m hotel-pms/config-service:ci >/dev/null
docker run -d --name notification-reference-redis --network notification-reference-ci \
  redis:8.8.1-alpine redis-server --requirepass "$CI_REDIS_PASSWORD" >/dev/null
docker run -d --name notification-reference-mail --network notification-reference-ci -p 18025:8025 axllent/mailpit:v1.31.0 >/dev/null
wait_health http://127.0.0.1:18990/actuator/health
echo 'native_build_mode=optimized-O2' > "$result_dir/metrics.txt"
for mode in native jvm; do
  container="notification-reference-$mode"
  started_ms="$(date +%s%3N)"
  docker run -d --name "$container" --network notification-reference-ci -p 18088:8088 -p 18098:8090 \
    -e SPRING_PROFILES_ACTIVE=notification-service -e CONFIG_SERVER_URL=http://notification-reference-config:8888 \
    -e "CONFIG_SERVER_PASSWORD=$CI_CONFIG_PASSWORD" -e "INTERNAL_HMAC_SECRET=$CI_HMAC_SECRET" \
    -e INTERNAL_REDIS_HOST=notification-reference-redis -e "INTERNAL_REDIS_PASSWORD=$CI_REDIS_PASSWORD" \
    -e "SPRING_DATA_REDIS_PASSWORD=$CI_REDIS_PASSWORD" -e SMTP_HOST=notification-reference-mail -e SMTP_PORT=1025 \
    -e JAVA_TOOL_OPTIONS=-Xmx512m "hotel-pms/notification-service-$mode:validated" >/dev/null
  wait_health http://127.0.0.1:18098/actuator/health
  startup_ms=$(( $(date +%s%3N) - started_ms ))
  idle_memory="$(docker stats --no-stream --format '{{.MemUsage}}' "$container")"
  for probe in liveness readiness; do
    curl -fsS "http://127.0.0.1:18098/actuator/health/$probe" | jq -e '.status == "UP"' >/dev/null
  done
  curl -fsS http://127.0.0.1:18098/actuator/prometheus > "$result_dir/$mode-prometheus.txt"
  grep -q '^http_server_requests' "$result_dir/$mode-prometheus.txt"
  unsigned="$(curl -sS -o "$result_dir/$mode-unsigned.json" -w '%{http_code}' -X POST http://127.0.0.1:18088/internal/notifications/checkin -H 'Content-Type: application/json' -d '{}')"
  [[ "$unsigned" == 401 ]]
  nonce="$(openssl rand -hex 16)"
  timestamp="$(date +%s%3N)"
  hotel=00000000-0000-0000-0000-000000000001
  signature="$(printf '%s' "ci-admin:ADMIN:$hotel:$timestamp:$nonce" | openssl dgst -sha256 -hmac "$CI_HMAC_SECRET" -r | awk '{print $1}')"
  headers=(-H 'X-Auth-User: ci-admin' -H 'X-Auth-Role: ADMIN' -H "X-Auth-Hotel: $hotel" -H "X-Auth-Timestamp: $timestamp" -H "X-Auth-Nonce: $nonce" -H "X-Internal-Signature: $signature" -H 'Content-Type: application/json')
  body="{\"guestEmail\":\"$mode@example.test\",\"guestName\":\"Native reference\",\"hotelName\":\"CI Hotel\",\"roomNumber\":\"101\",\"expectedCheckOutDate\":\"$(date -u -d tomorrow +%F)\",\"locale\":\"en\"}"
  first="$(curl -sS -o "$result_dir/$mode-delivery.json" -w '%{http_code}' -X POST http://127.0.0.1:18088/internal/notifications/checkin "${headers[@]}" -d "$body")"
  [[ "$first" == 200 ]]
  jq -e '. == true' "$result_dir/$mode-delivery.json" >/dev/null
  replay="$(curl -sS -o "$result_dir/$mode-replay.json" -w '%{http_code}' -X POST http://127.0.0.1:18088/internal/notifications/checkin "${headers[@]}" -d "$body")"
  [[ "$replay" == 401 ]]
  [[ "$(docker exec notification-reference-redis redis-cli --no-auth-warning -a "$CI_REDIS_PASSWORD" exists "internal-auth:nonce:$nonce")" == 1 ]]
  curl -fsS http://127.0.0.1:18025/api/v1/messages > "$result_dir/$mode-mailbox.json"
  jq -e --arg recipient "$mode@example.test" 'any(.messages[]; any(.To[]; .Address == $recipient))' "$result_dir/$mode-mailbox.json" >/dev/null
  loaded_memory="$(docker stats --no-stream --format '{{.MemUsage}}' "$container")"
  for _ in {1..30}; do
    curl -fsS http://127.0.0.1:18098/actuator/health | jq -e '.status == "UP"' >/dev/null
    sleep 2
  done
  [[ "$(docker inspect -f '{{.RestartCount}}' "$container")" == 0 ]]
  {
    echo "${mode}_startup_ms=$startup_ms"
    echo "${mode}_idle_memory=$idle_memory"
    echo "${mode}_loaded_memory=$loaded_memory"
    echo "${mode}_image_size_bytes=$(docker image inspect "hotel-pms/notification-service-$mode:validated" -f '{{.Size}}')"
    echo "${mode}_health=UP"
    echo "${mode}_liveness=UP"
    echo "${mode}_readiness=UP"
    echo "${mode}_prometheus=PASS"
    echo "${mode}_smtp_delivery=PASS"
    echo "${mode}_hmac_redis_replay=PASS"
    echo "${mode}_stability=30/30_over_60_seconds_zero_restarts"
  } >> "$result_dir/metrics.txt"
  docker stop "$container" >/dev/null
done
