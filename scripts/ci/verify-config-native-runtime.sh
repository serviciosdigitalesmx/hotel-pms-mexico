#!/usr/bin/env bash
set -Eeuo pipefail
RESULT_DIR="${RESULT_DIR:-build/config-native-runtime}"
mkdir -p "${RESULT_DIR}"
exec > >(tee "${RESULT_DIR}/gate.log") 2>&1
export RESULT_DIR
python3 scripts/ci/config-native-runtime.py
