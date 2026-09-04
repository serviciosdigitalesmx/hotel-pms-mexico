#!/usr/bin/env bash
set -Eeuo pipefail
[[ "${GITHUB_ACTIONS:-}" == true ]] || { echo 'This artifact loader is restricted to GitHub Actions.' >&2; exit 1; }
manifest="${1:-docs/native-stack-images.json}"
repository=serviciosdigitalesmx/hotel-pms-mexico
expected='["api-gateway","auth-service","billing-service","config-service","fb-service","frontdesk-service","guest-service","notification-service"]'
jq -e --argjson expected "$expected" '
  ([.images[].module] | sort) == $expected and
  all(.images[]; (.artifact_id | type == "number") and (.run_id | type == "number") and (.sha | test("^[0-9a-f]{40}$")))
' "$manifest" >/dev/null
mkdir -p build/native-stack-artifact-evidence
while IFS=$'\t' read -r module artifact_id source_sha run_id; do
  artifact_dir="${RUNNER_TEMP:?}/native-stack-images/$module"
  mkdir -p "$artifact_dir"
  timeout 180s gh api "repos/$repository/actions/artifacts/$artifact_id" > "build/native-stack-artifact-evidence/$module-artifact.json"
  jq -e --arg sha "$source_sha" --argjson run "$run_id" '.expired == false and .workflow_run.head_sha == $sha and .workflow_run.id == $run' \
    "build/native-stack-artifact-evidence/$module-artifact.json" >/dev/null
  timeout 180s gh api "repos/$repository/actions/runs/$run_id" > "build/native-stack-artifact-evidence/$module-run.json"
  jq -e --arg sha "$source_sha" '.status == "completed" and .conclusion == "success" and .head_sha == $sha' \
    "build/native-stack-artifact-evidence/$module-run.json" >/dev/null
  timeout 600s gh api "repos/$repository/actions/artifacts/$artifact_id/zip" > "$artifact_dir/artifact.zip"
  unzip -q "$artifact_dir/artifact.zip" -d "$artifact_dir/images"
  mapfile -t checksums < <(find "$artifact_dir/images" -type f \( -name SHA256SUMS -o -name '*.sha256' \))
  [[ "${#checksums[@]}" -gt 0 ]] || { echo "$module: artifact has no checksum" >&2; exit 1; }
  for checksum_file in "${checksums[@]}"; do
    (cd "$(dirname "$checksum_file")" && sha256sum --check "$(basename "$checksum_file")")
  done
  mapfile -t archives < <(find "$artifact_dir/images" -type f \( -name '*.tar.gz' -o -name '*.tar' \))
  [[ "${#archives[@]}" -gt 0 ]]
  for archive in "${archives[@]}"; do timeout 300s docker load --input "$archive"; done
  for mode in native jvm; do
    image="hotel-pms/$module-$mode:validated"
    docker image inspect "$image" > "build/native-stack-artifact-evidence/$module-$mode-image.json"
    jq -e '.[0].Os == "linux" and .[0].Architecture == "amd64"' \
      "build/native-stack-artifact-evidence/$module-$mode-image.json" >/dev/null
  done
done < <(jq -r '.images[] | [.module,.artifact_id,.sha,.run_id] | @tsv' "$manifest")
