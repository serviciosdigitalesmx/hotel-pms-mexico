#!/usr/bin/env bash
set -Eeuo pipefail

RESULT_DIR="${GITHUB_WORKSPACE:-$(pwd)}/build/billing-native-runtime"
NETWORK_NAME=billing-native-ci
POSTGRES_CONTAINER=billing-native-postgres
REDIS_CONTAINER=billing-native-redis
CONFIG_CONTAINER=billing-native-config
GUEST_CONTAINER=billing-native-guest
FRONTDESK_CONTAINER=billing-native-frontdesk
BILLING_CONTAINER=billing-native
JVM_BILLING_CONTAINER=billing-jvm-control

: "${CI_POSTGRES_PASSWORD:?CI_POSTGRES_PASSWORD is required}"
: "${CI_REDIS_PASSWORD:?CI_REDIS_PASSWORD is required}"
: "${CI_CONFIG_PASSWORD:?CI_CONFIG_PASSWORD is required}"
: "${CI_HMAC_SECRET:?CI_HMAC_SECRET is required}"
mkdir -p "${RESULT_DIR}"

collect_evidence() {
  docker ps -a > "${RESULT_DIR}/docker-ps.txt" 2>&1 || true
  for container in "${CONFIG_CONTAINER}" "${POSTGRES_CONTAINER}" "${REDIS_CONTAINER}" \
      "${GUEST_CONTAINER}" "${FRONTDESK_CONTAINER}" "${BILLING_CONTAINER}" "${JVM_BILLING_CONTAINER}"; do
    docker logs "${container}" > "${RESULT_DIR}/${container}.log" 2>&1 || true
  done
}
trap collect_evidence EXIT

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
  local args=(--silent --show-error --output "${output_file}" --write-out '%{http_code}'
    --request "${method}" --header 'X-Auth-User: ci-admin' --header 'X-Auth-Role: ADMIN'
    --header "X-Auth-Hotel: ${hotel_id}" --header "X-Auth-Timestamp: ${timestamp}"
    --header "X-Auth-Nonce: ${nonce}" --header "X-Internal-Signature: ${signature}")
  if [[ -n "${body}" ]]; then
    args+=(--header 'Content-Type: application/json' --data "${body}")
  fi
  curl "${args[@]}" "${url}"
}

verify_pdf_content() {
  local pdf_file="$1" evidence_prefix="$2"
  pdftotext -layout "${pdf_file}" "${RESULT_DIR}/${evidence_prefix}-text.txt"
  for expected_text in 'FACTURA' 'Native Billing' 'Native room night' 'Habitación' '100.00'; do
    grep --fixed-strings --quiet "${expected_text}" "${RESULT_DIR}/${evidence_prefix}-text.txt" || {
      echo "${evidence_prefix}: missing PDF text: ${expected_text}" >&2
      return 1
    }
  done
  pdffonts "${pdf_file}" | tee "${RESULT_DIR}/${evidence_prefix}-fonts.txt" \
    | awk 'NR > 2 { if ($1 !~ /NotoSans/ || $(NF-4) != "yes") exit 1; count++ }
           END { if (count < 2) exit 1 }'
}

echo "Starting Config Server, PostgreSQL, and Redis"
docker network create "${NETWORK_NAME}" >/dev/null
docker run --detach --name "${CONFIG_CONTAINER}" --network "${NETWORK_NAME}" \
  --publish 18888:8888 --publish 18091:8090 --env "CONFIG_SERVER_PASSWORD=${CI_CONFIG_PASSWORD}" \
  --env 'JAVA_TOOL_OPTIONS=-Xmx256m -XX:MaxMetaspaceSize=128m -XX:+ExitOnOutOfMemoryError' \
  hotel-pms/config-service:ci >/dev/null
wait_for_health config-service http://127.0.0.1:18091/actuator/health 90 "${CONFIG_CONTAINER}"
curl --silent --show-error --fail --user "configuser:${CI_CONFIG_PASSWORD}" \
  http://127.0.0.1:18888/billing-service/default | jq -e '.name == "billing-service"' >/dev/null

docker run --detach --name "${POSTGRES_CONTAINER}" --network "${NETWORK_NAME}" \
  --env POSTGRES_USER=postgres --env "POSTGRES_PASSWORD=${CI_POSTGRES_PASSWORD}" \
  postgres:15-alpine >/dev/null
docker run --detach --name "${REDIS_CONTAINER}" --network "${NETWORK_NAME}" \
  redis:8.8.1-alpine redis-server --requirepass "${CI_REDIS_PASSWORD}" >/dev/null
streak=0
for _ in {1..60}; do
  if docker exec "${POSTGRES_CONTAINER}" pg_isready --username postgres >/dev/null 2>&1 \
      && docker exec "${POSTGRES_CONTAINER}" psql --username postgres --dbname postgres \
        --tuples-only --no-align --command 'select 1;' 2>/dev/null | grep -qx 1; then
    streak=$((streak + 1))
    [[ "${streak}" -ge 2 ]] && break
  else
    streak=0
  fi
  sleep 2
done
[[ "${streak}" -ge 2 ]]
for database in hotel_billing hotel_billing_jvm hotel_guest hotel_frontdesk; do
  docker exec "${POSTGRES_CONTAINER}" createdb --username postgres "${database}"
done
for _ in {1..30}; do
  docker exec "${REDIS_CONTAINER}" redis-cli --pass "${CI_REDIS_PASSWORD}" ping 2>/dev/null \
    | grep -qx PONG && break
  sleep 1
done
docker exec "${REDIS_CONTAINER}" redis-cli --pass "${CI_REDIS_PASSWORD}" ping 2>/dev/null | grep -qx PONG

echo "Starting JVM downstream services for real Feign calls"
docker run --detach --name "${GUEST_CONTAINER}" --network "${NETWORK_NAME}" \
  --network-alias guest-service --publish 18083:8083 --publish 18090:8090 \
  --env SPRING_PROFILES_ACTIVE=guest-service --env CONFIG_SERVER_URL=http://billing-native-config:8888 \
  --env CONFIG_SERVER_PASSWORD="${CI_CONFIG_PASSWORD}" --env INTERNAL_REDIS_HOST=billing-native-redis \
  --env INTERNAL_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" --env SPRING_DATA_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" \
  --env SPRING_DATASOURCE_URL=jdbc:postgresql://billing-native-postgres:5432/hotel_guest \
  --env SPRING_DATASOURCE_USERNAME=postgres --env SPRING_DATASOURCE_PASSWORD="${CI_POSTGRES_PASSWORD}" \
  --env INTERNAL_HMAC_SECRET="${CI_HMAC_SECRET}" \
  --env 'JAVA_TOOL_OPTIONS=-Xmx512m -XX:MaxMetaspaceSize=256m -XX:+ExitOnOutOfMemoryError' \
  hotel-pms/guest-service-jvm:ci >/dev/null
docker run --detach --name "${FRONTDESK_CONTAINER}" --network "${NETWORK_NAME}" \
  --network-alias frontdesk-service --publish 18081:8081 --publish 18092:8090 \
  --env SPRING_PROFILES_ACTIVE=frontdesk-service --env CONFIG_SERVER_URL=http://billing-native-config:8888 \
  --env CONFIG_SERVER_PASSWORD="${CI_CONFIG_PASSWORD}" --env INTERNAL_REDIS_HOST=billing-native-redis \
  --env INTERNAL_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" --env SPRING_DATA_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" \
  --env SPRING_DATASOURCE_URL=jdbc:postgresql://billing-native-postgres:5432/hotel_frontdesk \
  --env SPRING_DATASOURCE_USERNAME=postgres --env SPRING_DATASOURCE_PASSWORD="${CI_POSTGRES_PASSWORD}" \
  --env INTERNAL_HMAC_SECRET="${CI_HMAC_SECRET}" --env ALLOGGIATI_DRY_RUN=true \
  --env ALLOGGIATI_USERNAME=ci_placeholder_user --env ALLOGGIATI_PASSWORD=ci_placeholder_password \
  --env ALLOGGIATI_WS_KEY=ci_placeholder_wskey \
  --env ALLOGGIATI_CREDENTIALS_ENCRYPTION_KEY=ci_placeholder_encryption_key \
  --env ALLOGGIATI_CREDENTIALS_ENCRYPTION_SALT=deadbeefdeadbeefdeadbeefdeadbeef \
  --env 'JAVA_TOOL_OPTIONS=-Xmx512m -XX:MaxMetaspaceSize=256m -XX:+ExitOnOutOfMemoryError' \
  hotel-pms/frontdesk-service-jvm:ci >/dev/null
wait_for_health guest-service http://127.0.0.1:18090/actuator/health 120 "${GUEST_CONTAINER}"
wait_for_health frontdesk-service http://127.0.0.1:18092/actuator/health 120 "${FRONTDESK_CONTAINER}"

hotel_a=00000000-0000-0000-0000-000000000101
hotel_b=00000000-0000-0000-0000-000000000202
guest_payload='{"firstName":"Native","lastName":"Billing","email":"native-billing@example.test"}'
guest_create_code="$(signed_request POST http://127.0.0.1:18083/api/v1/guests "${hotel_a}" \
  "$(openssl rand -hex 16)" "${RESULT_DIR}/guest-create.json" "${guest_payload}")"
[[ "${guest_create_code}" == 201 ]]
guest_id="$(jq -er '.id' "${RESULT_DIR}/guest-create.json")"

echo "Starting billing Native Image"
native_started_ms="$(date +%s%3N)"
docker run --detach --name "${BILLING_CONTAINER}" --network "${NETWORK_NAME}" \
  --network-alias billing-service --publish 18085:8085 --publish 18095:8090 \
  --env SPRING_PROFILES_ACTIVE=billing-service --env CONFIG_SERVER_URL=http://billing-native-config:8888 \
  --env CONFIG_SERVER_PASSWORD="${CI_CONFIG_PASSWORD}" --env INTERNAL_REDIS_HOST=billing-native-redis \
  --env INTERNAL_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" --env SPRING_DATA_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" \
  --env SPRING_DATASOURCE_URL=jdbc:postgresql://billing-native-postgres:5432/hotel_billing \
  --env SPRING_DATASOURCE_USERNAME=postgres --env SPRING_DATASOURCE_PASSWORD="${CI_POSTGRES_PASSWORD}" \
  --env INTERNAL_HMAC_SECRET="${CI_HMAC_SECRET}" hotel-pms/billing-service-native:ci >/dev/null
wait_for_health billing-service-native http://127.0.0.1:18095/actuator/health 120 "${BILLING_CONTAINER}"
native_ready_ms="$(date +%s%3N)"
native_startup_ms=$((native_ready_ms - native_started_ms))
curl --silent --show-error --fail http://127.0.0.1:18095/actuator/health \
  | tee "${RESULT_DIR}/health.json" | jq -e '.status == "UP"' >/dev/null
native_idle_memory="$(docker stats --no-stream --format '{{.MemUsage}}|{{.MemPerc}}' "${BILLING_CONTAINER}")"

missing_hmac_code="$(curl --silent --output "${RESULT_DIR}/missing-hmac.json" \
  --write-out '%{http_code}' http://127.0.0.1:18085/api/v1/invoices)"
[[ "${missing_hmac_code}" == 401 ]]

stay_id=00000000-0000-0000-0000-000000000301
invoice_payload="{\"stayId\":\"${stay_id}\",\"guestId\":\"${guest_id}\",\"reservationId\":null}"
invoice_code="$(signed_request POST http://127.0.0.1:18085/api/v1/invoices/stay "${hotel_a}" \
  "$(openssl rand -hex 16)" "${RESULT_DIR}/invoice-create.json" "${invoice_payload}")"
[[ "${invoice_code}" == 201 ]]
invoice_id="$(jq -er '.id' "${RESULT_DIR}/invoice-create.json")"

charge_payload='{"type":"ROOM_NIGHT","description":"Native room night","amount":100.00,"unitPrice":100.00,"nights":1}'
charge_code="$(signed_request POST "http://127.0.0.1:18085/api/v1/invoices/stay/${stay_id}/charges" "${hotel_a}" \
  "$(openssl rand -hex 16)" "${RESULT_DIR}/charge-create.json" "${charge_payload}")"
[[ "${charge_code}" == 201 ]]
jq -e '.amount == 100 and .vatRate == 0.10 and .nights == 1' "${RESULT_DIR}/charge-create.json" >/dev/null

payment_code="$(signed_request POST "http://127.0.0.1:18085/api/v1/invoices/${invoice_id}/payments" "${hotel_a}" \
  "$(openssl rand -hex 16)" "${RESULT_DIR}/payment-create.json" '{"amount":100.00,"paymentMethod":"CASH"}')"
[[ "${payment_code}" == 201 ]]

same_tenant_code="$(signed_request GET "http://127.0.0.1:18085/api/v1/invoices/${invoice_id}" "${hotel_a}" \
  "$(openssl rand -hex 16)" "${RESULT_DIR}/same-tenant.json")"
cross_tenant_code="$(signed_request GET "http://127.0.0.1:18085/api/v1/invoices/${invoice_id}" "${hotel_b}" \
  "$(openssl rand -hex 16)" "${RESULT_DIR}/cross-tenant.json")"
[[ "${same_tenant_code}" == 200 && "${cross_tenant_code}" == 404 ]]
jq -e '.status == "PAID" and .totalAmount == 100 and (.charges | length) == 1 and (.payments | length) == 1' \
  "${RESULT_DIR}/same-tenant.json" >/dev/null

overspend_stay_id=00000000-0000-0000-0000-000000000302
overspend_payload="{\"stayId\":\"${overspend_stay_id}\",\"guestId\":\"${guest_id}\",\"reservationId\":null}"
signed_request POST http://127.0.0.1:18085/api/v1/invoices/stay "${hotel_a}" \
  "$(openssl rand -hex 16)" "${RESULT_DIR}/overspend-invoice.json" "${overspend_payload}" >/dev/null
overspend_charge_code="$(signed_request POST "http://127.0.0.1:18085/api/v1/invoices/stay/${overspend_stay_id}/charges" "${hotel_a}" \
  "$(openssl rand -hex 16)" "${RESULT_DIR}/overspend-charge.json" "${charge_payload}")"
[[ "${overspend_charge_code}" == 201 ]]
overspend_invoice_id="$(jq -er '.invoiceId' "${RESULT_DIR}/overspend-charge.json")"
overspend_code="$(signed_request POST "http://127.0.0.1:18085/api/v1/invoices/${overspend_invoice_id}/payments" "${hotel_a}" \
  "$(openssl rand -hex 16)" "${RESULT_DIR}/overspend-payment.json" '{"amount":101.00,"paymentMethod":"CASH"}')"
[[ "${overspend_code}" == 400 ]]

replay_nonce="$(openssl rand -hex 16)"
replay_timestamp="$(date +%s%3N)"
replay_signature="$(printf '%s' "ci-admin:ADMIN:${hotel_a}:${replay_timestamp}:${replay_nonce}" \
  | openssl dgst -sha256 -hmac "${CI_HMAC_SECRET}" -r | awk '{print $1}')"
replay_headers=(--header 'X-Auth-User: ci-admin' --header 'X-Auth-Role: ADMIN'
  --header "X-Auth-Hotel: ${hotel_a}" --header "X-Auth-Timestamp: ${replay_timestamp}"
  --header "X-Auth-Nonce: ${replay_nonce}" --header "X-Internal-Signature: ${replay_signature}")
first_replay_code="$(curl --silent --output "${RESULT_DIR}/replay-first.json" --write-out '%{http_code}' \
  "${replay_headers[@]}" "http://127.0.0.1:18085/api/v1/invoices/${invoice_id}")"
second_replay_code="$(curl --silent --output "${RESULT_DIR}/replay-second.json" --write-out '%{http_code}' \
  "${replay_headers[@]}" "http://127.0.0.1:18085/api/v1/invoices/${invoice_id}")"
[[ "${first_replay_code}" == 200 && "${second_replay_code}" == 401 ]]
redis_nonce_exists="$(docker exec "${REDIS_CONTAINER}" redis-cli --pass "${CI_REDIS_PASSWORD}" \
  exists "internal-auth:nonce:${replay_nonce}" 2>/dev/null)"
[[ "${redis_nonce_exists}" == 1 ]]

nonce_count_before="$(docker exec "${REDIS_CONTAINER}" redis-cli --pass "${CI_REDIS_PASSWORD}" \
  --scan --pattern 'internal-auth:nonce:*' 2>/dev/null | wc -l | tr -d ' ')"
search_code="$(signed_request GET 'http://127.0.0.1:18085/api/v1/invoices/search?query=Native' "${hotel_a}" \
  "$(openssl rand -hex 16)" "${RESULT_DIR}/invoice-search.json")"
[[ "${search_code}" == 200 ]]
pdf_code="$(signed_request GET "http://127.0.0.1:18085/api/v1/invoices/${invoice_id}/pdf" "${hotel_a}" \
  "$(openssl rand -hex 16)" "${RESULT_DIR}/invoice.pdf")"
[[ "${pdf_code}" == 200 ]]
file "${RESULT_DIR}/invoice.pdf" | grep -qi PDF
verify_pdf_content "${RESULT_DIR}/invoice.pdf" native-pdf
nonce_count_after="$(docker exec "${REDIS_CONTAINER}" redis-cli --pass "${CI_REDIS_PASSWORD}" \
  --scan --pattern 'internal-auth:nonce:*' 2>/dev/null | wc -l | tr -d ' ')"
feign_nonce_delta=$((nonce_count_after - nonce_count_before))
[[ "${feign_nonce_delta}" -ge 3 ]]

flyway_latest="$(docker exec "${POSTGRES_CONTAINER}" psql --username postgres --dbname hotel_billing \
  --tuples-only --no-align --command 'select max(version::integer) from flyway_schema_history where success = true;')"
persisted_invoice="$(docker exec "${POSTGRES_CONTAINER}" psql --username postgres --dbname hotel_billing \
  --tuples-only --no-align --command "select count(*) from invoices where id = '${invoice_id}' and hotel_id = '${hotel_a}';")"
persisted_charges="$(docker exec "${POSTGRES_CONTAINER}" psql --username postgres --dbname hotel_billing \
  --tuples-only --no-align --command "select count(*) from invoice_charges c join invoices i on i.id = c.invoice_id where i.id = '${invoice_id}' and i.hotel_id = '${hotel_a}';")"
persisted_payments="$(docker exec "${POSTGRES_CONTAINER}" psql --username postgres --dbname hotel_billing \
  --tuples-only --no-align --command "select count(*) from payments p join invoices i on i.id = p.invoice_id where i.id = '${invoice_id}' and i.hotel_id = '${hotel_a}';")"
[[ "${flyway_latest}" == 12 && "${persisted_invoice}" == 1 && "${persisted_charges}" == 1 && "${persisted_payments}" == 1 ]]

for _ in {1..10}; do
  curl --silent --show-error --fail http://127.0.0.1:18095/actuator/health \
    | jq -e '.status == "UP"' >/dev/null
  sleep 1
done
native_after_use_memory="$(docker stats --no-stream --format '{{.MemUsage}}|{{.MemPerc}}' "${BILLING_CONTAINER}")"
native_image_size_bytes="$(docker image inspect hotel-pms/billing-service-native:ci --format '{{.Size}}')"

echo "Starting billing JVM control"
jvm_started_ms="$(date +%s%3N)"
docker run --detach --name "${JVM_BILLING_CONTAINER}" --network "${NETWORK_NAME}" \
  --network-alias billing-service-jvm --publish 28085:8085 --publish 28095:8090 \
  --env SPRING_PROFILES_ACTIVE=billing-service --env CONFIG_SERVER_URL=http://billing-native-config:8888 \
  --env CONFIG_SERVER_PASSWORD="${CI_CONFIG_PASSWORD}" --env INTERNAL_REDIS_HOST=billing-native-redis \
  --env INTERNAL_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" --env SPRING_DATA_REDIS_PASSWORD="${CI_REDIS_PASSWORD}" \
  --env SPRING_DATASOURCE_URL=jdbc:postgresql://billing-native-postgres:5432/hotel_billing_jvm \
  --env SPRING_DATASOURCE_USERNAME=postgres --env SPRING_DATASOURCE_PASSWORD="${CI_POSTGRES_PASSWORD}" \
  --env INTERNAL_HMAC_SECRET="${CI_HMAC_SECRET}" \
  --env 'JAVA_TOOL_OPTIONS=-Xmx512m -XX:MaxMetaspaceSize=256m -XX:+ExitOnOutOfMemoryError' \
  hotel-pms/billing-service-jvm:ci >/dev/null
wait_for_health billing-service-jvm http://127.0.0.1:28095/actuator/health 120 "${JVM_BILLING_CONTAINER}"
jvm_ready_ms="$(date +%s%3N)"
jvm_startup_ms=$((jvm_ready_ms - jvm_started_ms))
jvm_missing_hmac_code="$(curl --silent --output "${RESULT_DIR}/jvm-missing-hmac.json" \
  --write-out '%{http_code}' http://127.0.0.1:28085/api/v1/invoices)"
[[ "${jvm_missing_hmac_code}" == 401 ]]
jvm_idle_memory="$(docker stats --no-stream --format '{{.MemUsage}}|{{.MemPerc}}' "${JVM_BILLING_CONTAINER}")"
jvm_invoice_code="$(signed_request POST http://127.0.0.1:28085/api/v1/invoices/stay "${hotel_a}" \
  "$(openssl rand -hex 16)" "${RESULT_DIR}/jvm-invoice-create.json" "${invoice_payload}")"
[[ "${jvm_invoice_code}" == 201 ]]
jvm_invoice_id="$(jq -er '.id' "${RESULT_DIR}/jvm-invoice-create.json")"
jvm_charge_code="$(signed_request POST "http://127.0.0.1:28085/api/v1/invoices/stay/${stay_id}/charges" "${hotel_a}" \
  "$(openssl rand -hex 16)" "${RESULT_DIR}/jvm-charge-create.json" "${charge_payload}")"
[[ "${jvm_charge_code}" == 201 ]]
jvm_payment_code="$(signed_request POST "http://127.0.0.1:28085/api/v1/invoices/${jvm_invoice_id}/payments" "${hotel_a}" \
  "$(openssl rand -hex 16)" "${RESULT_DIR}/jvm-payment-create.json" '{"amount":100.00,"paymentMethod":"CASH"}')"
[[ "${jvm_payment_code}" == 201 ]]
jvm_pdf_code="$(signed_request GET "http://127.0.0.1:28085/api/v1/invoices/${jvm_invoice_id}/pdf" "${hotel_a}" \
  "$(openssl rand -hex 16)" "${RESULT_DIR}/invoice-jvm.pdf")"
[[ "${jvm_pdf_code}" == 200 ]]
verify_pdf_content "${RESULT_DIR}/invoice-jvm.pdf" jvm-pdf
jvm_after_use_memory="$(docker stats --no-stream --format '{{.MemUsage}}|{{.MemPerc}}' "${JVM_BILLING_CONTAINER}")"
jvm_image_size_bytes="$(docker image inspect hotel-pms/billing-service-jvm:ci --format '{{.Size}}')"
jvm_flyway_latest="$(docker exec "${POSTGRES_CONTAINER}" psql --username postgres --dbname hotel_billing_jvm \
  --tuples-only --no-align --command 'select max(version::integer) from flyway_schema_history where success = true;')"
[[ "${jvm_flyway_latest}" == 12 ]]

printf '%s\n' \
  "native_build_mode=${CI_NATIVE_BUILD_MODE:-unknown}" \
  "native_startup_ms=${native_startup_ms}" \
  "native_idle_memory=${native_idle_memory}" \
  "native_after_basic_use_memory=${native_after_use_memory}" \
  "native_image_size_bytes=${native_image_size_bytes}" \
  "native_health_status=UP" \
  "native_flyway_latest_version=${flyway_latest}" \
  "native_persisted_invoice=${persisted_invoice}" \
  "native_persisted_charges=${persisted_charges}" \
  "native_persisted_payments=${persisted_payments}" \
  "native_hmac_missing_headers=${missing_hmac_code}" \
  "native_hmac_replay_first=${first_replay_code}" \
  "native_hmac_replay_second=${second_replay_code}" \
  "native_tenant_same_hotel=${same_tenant_code}" \
  "native_tenant_cross_hotel=${cross_tenant_code}" \
  "native_charge_vat_rate=0.10" \
  "native_cash_payment_status=PAID" \
  "native_overspend_rejected=${overspend_code}" \
  "native_feign_nonce_delta=${feign_nonce_delta}" \
  "native_pdf_status=${pdf_code}" \
  "native_pdf_text_and_embedded_fonts=PASS" \
  "native_stability_health_checks=10/10" \
  "jvm_startup_ms=${jvm_startup_ms}" \
  "jvm_idle_memory=${jvm_idle_memory}" \
  "jvm_after_basic_use_memory=${jvm_after_use_memory}" \
  "jvm_image_size_bytes=${jvm_image_size_bytes}" \
  "jvm_health_status=UP" \
  "jvm_flyway_latest_version=${jvm_flyway_latest}" \
  "jvm_hmac_missing_headers=${jvm_missing_hmac_code}" \
  "jvm_pdf_status=${jvm_pdf_code}" \
  "jvm_pdf_text_and_embedded_fonts=PASS" \
  | tee "${RESULT_DIR}/metrics.txt"
