#!/usr/bin/env bash
set -Eeuo pipefail
source scripts/ci/native-stack-lifecycle.sh
load_sampler_pid=''
trap 'stop_load_sampler' EXIT

stop_load_sampler
sleep 30 &
load_sampler_pid=$!
pid="$load_sampler_pid"
stop_load_sampler
[[ -z "$load_sampler_pid" ]] && ! kill -0 "$pid" 2>/dev/null
echo 'PASS: no sampler and normal TERM exit'

# A real child inheriting ignored TERM, not a mocked Docker/service response.
(trap '' TERM; exec sleep 30) &
load_sampler_pid=$!
pid="$load_sampler_pid"
sleep 0.2
started=$SECONDS
stop_load_sampler
(( SECONDS - started <= 8 ))
[[ -z "$load_sampler_pid" ]] && ! kill -0 "$pid" 2>/dev/null
echo 'PASS: TERM-resistant child killed and reaped within eight seconds'

(exit 7) &
load_sampler_pid=$!
sleep 0.2
stop_load_sampler
[[ -z "$load_sampler_pid" ]]
echo 'PASS: already-exited child and repeated cleanup'
stop_load_sampler

export GITHUB_ACTIONS=true GITHUB_RUN_ID=123
assert_ci_project pms-native-ci-123-native
assert_ci_project pms-native-ci-123-jvm
! assert_ci_project pms-native-ci-456-native
! assert_ci_project hotel-pms
echo 'PASS: cleanup rejects other runs and workstation projects'
