#!/usr/bin/env bash
set -Eeuo pipefail
RESULT_DIR="${RESULT_DIR:-build/config-native-runtime}"
EXPORT_DIR="build/config-native-images"
: "${TESTED_COMMIT:?Missing tested source commit}"
[[ "$TESTED_COMMIT" =~ ^[0-9a-f]{40}$ ]]
jq -e '.gate_status == "PASS" and .native_build_mode == "optimized-O2" and
  .config_content_parity == "PASS" and .native_stability == "PASS" and .jvm_stability == "PASS" and
  .native_docker_health == "healthy" and .jvm_docker_health == "healthy" and
  .native_ca_certificates == "PASS" and .compose_override == "PASS"' \
  "$RESULT_DIR/metrics.json" >/dev/null
mkdir -p "$EXPORT_DIR"
tags=()
for mode in native jvm; do
  repo="hotel-pms/config-service-${mode}"
  docker tag "${repo}:ci" "${repo}:validated"
  docker tag "${repo}:ci" "${repo}:${TESTED_COMMIT}"
  tags+=("${repo}:ci" "${repo}:validated" "${repo}:${TESTED_COMMIT}")
done
docker image inspect "hotel-pms/config-service-native:${TESTED_COMMIT}" \
  "hotel-pms/config-service-jvm:${TESTED_COMMIT}" > "$EXPORT_DIR/image-metadata.json"
# Keep the six explicit tags in the Docker manifest for downstream docker load.
docker save "${tags[@]}" | gzip -1 > "$EXPORT_DIR/config-service-images.tar.gz"
gzip -t "$EXPORT_DIR/config-service-images.tar.gz"
tar -xOzf "$EXPORT_DIR/config-service-images.tar.gz" manifest.json > "$EXPORT_DIR/manifest.json"
jq -e 'length == 2 and ([.[].RepoTags[]] | length == 6)' "$EXPORT_DIR/manifest.json" >/dev/null
# Verify the exact exported bytes are loadable before publishing them.
gzip -dc "$EXPORT_DIR/config-service-images.tar.gz" | docker load
jq -n --arg commit "$TESTED_COMMIT" --arg run_url \
  "https://github.com/${GITHUB_REPOSITORY}/actions/runs/${GITHUB_RUN_ID}" \
  --argjson tags "$(printf '%s\n' "${tags[@]}" | jq -R . | jq -s .)" \
  '{tested_commit: $commit, run_url: $run_url, tags: $tags,
    runtime_network_alias: "config-server", runtime_ports: [8888, 8090],
    runtime_environment: ["CONFIG_SERVER_USERNAME", "CONFIG_SERVER_PASSWORD"],
    platform: "linux/amd64", native_optimization: "O2", docker_load_verified: true}' \
  > "$EXPORT_DIR/provenance.json"
cp "$RESULT_DIR/metrics.json" "$EXPORT_DIR/metrics.json"
(
  cd "$EXPORT_DIR"
  sha256sum config-service-images.tar.gz image-metadata.json manifest.json provenance.json metrics.json > SHA256SUMS
  sha256sum --check SHA256SUMS
)
cp "$EXPORT_DIR/SHA256SUMS" "$RESULT_DIR/image-SHA256SUMS"
cp "$EXPORT_DIR/image-metadata.json" "$RESULT_DIR/image-metadata.json"
cp "$EXPORT_DIR/provenance.json" "$RESULT_DIR/image-provenance.json"
