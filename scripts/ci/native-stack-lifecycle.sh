#!/usr/bin/env bash
# Sourced by the integration gate and its failure-path regression tests.
stop_load_sampler() {
  local pid="${load_sampler_pid:-}" deadline
  load_sampler_pid=''
  [[ -n "$pid" ]] || return 0
  if kill -0 "$pid" 2>/dev/null; then
    kill -TERM "$pid" 2>/dev/null || : # It may have exited between checks.
    deadline=$((SECONDS + 5))
    while kill -0 "$pid" 2>/dev/null && (( SECONDS < deadline )); do sleep 0.1; done
    if kill -0 "$pid" 2>/dev/null; then
      echo "Sampler $pid ignored TERM; sending KILL" >&2
      kill -KILL "$pid" 2>/dev/null || :
      deadline=$((SECONDS + 2))
      while kill -0 "$pid" 2>/dev/null && (( SECONDS < deadline )); do sleep 0.1; done
    fi
  fi
  if kill -0 "$pid" 2>/dev/null; then
    echo "Sampler $pid did not exit after KILL; refusing an unbounded wait" >&2
    return 1
  fi
  # Only reap a child already known to be dead. Never wait on a live docker CLI.
  wait "$pid" 2>/dev/null || :
}

assert_ci_project() {
  [[ "${GITHUB_ACTIONS:-}" == true && "${GITHUB_RUN_ID:-}" =~ ^[0-9]+$ ]] &&
    [[ "$1" == "pms-native-ci-$GITHUB_RUN_ID-native" || "$1" == "pms-native-ci-$GITHUB_RUN_ID-jvm" ]] || {
      echo "Refusing cleanup outside this run's CI projects: $1" >&2
      return 1
    }
}

assert_project_removed() {
  local target="$1" kind remaining
  assert_ci_project "$target" || return 1
  for kind in container network volume; do
    remaining="$(timeout --signal=TERM --kill-after=5s 15s docker "$kind" ls \
      $([[ "$kind" == container ]] && echo --all) --quiet --filter "label=com.docker.compose.project=$target")" || return 1
    if [[ -n "$remaining" ]]; then
      echo "CI project $target still owns $kind resources: $remaining" >&2
      return 1
    fi
  done
}

cleanup_ci_project() {
  local target="$1" target_mode="$2" kind ids id failed=0
  assert_ci_project "$target" || return 1
  [[ "$target" == "pms-native-ci-$GITHUB_RUN_ID-$target_mode" ]] || return 1
  echo "Removing disposable CI project $target (including its volumes)"
  if ! NATIVE_STACK_MODE="$target_mode" timeout --signal=TERM --kill-after=10s 60s \
    docker compose --project-name "$target" -f docker-compose.yml -f docker-compose.native-stack-ci.yml \
      --profile observability down --timeout 20 --volumes --remove-orphans; then
    echo "Compose down failed for $target; attempting exact project-label cleanup" >&2
  fi
  # Also inspect after a successful down: orphans/partial previous starts must
  # not retain shared host ports. Never prune or touch another Compose project.
  for kind in container network volume; do
    ids="$(timeout --signal=TERM --kill-after=5s 15s docker "$kind" ls \
      $([[ "$kind" == container ]] && echo --all) --quiet --filter "label=com.docker.compose.project=$target")" || return 1
    for id in $ids; do
      if [[ "$kind" == container ]]; then
        timeout --signal=TERM --kill-after=5s 20s docker container rm --force --volumes "$id" || failed=1
      else
        timeout --signal=TERM --kill-after=5s 20s docker "$kind" rm "$id" || failed=1
      fi
    done
  done
  assert_project_removed "$target" || return 1
  [[ "$failed" == 0 ]]
}
