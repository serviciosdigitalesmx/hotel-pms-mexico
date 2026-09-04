#!/usr/bin/env python3
"""Bounded real HTTP load and Docker memory evidence, identical for Native/JVM."""

import concurrent.futures
import datetime
import hashlib
import hmac
import json
import os
from pathlib import Path
import re
import secrets
import subprocess
import sys
import time
import urllib.error
import urllib.request


def request(base, path, *, authenticated=False, role="ADMIN", bad_signature=False):
    headers = {}
    if authenticated:
        timestamp, nonce = str(time.time_ns() // 1_000_000), secrets.token_hex(16)
        tenant = "00000000-0000-0000-0000-000000000101"
        signature = hmac.new(
            os.environ["CI_HMAC_SECRET"].encode(),
            f"ci-admin:{role}:{tenant}:{timestamp}:{nonce}".encode(),
            hashlib.sha256,
        ).hexdigest()
        headers = {
            "X-Auth-User": "ci-admin", "X-Auth-Role": role, "X-Auth-Hotel": tenant,
            "X-Auth-Timestamp": timestamp, "X-Auth-Nonce": nonce,
            "X-Internal-Signature": "0" * 64 if bad_signature else signature,
        }
    with urllib.request.urlopen(urllib.request.Request(base + path, headers=headers), timeout=10) as response:
        assert response.status == 200, response.status
        return response.read().decode()


def memory_sample(container, elapsed):
    raw = subprocess.check_output(
        ["docker", "stats", "--no-stream", "--format", "{{.MemUsage}}|{{.MemPerc}}", container],
        text=True, timeout=15,
    ).strip()
    match = re.fullmatch(r"([\d.]+)(B|KiB|MiB|GiB|TiB)", raw.split(" / ")[0])
    assert match, raw
    unit = ["B", "KiB", "MiB", "GiB", "TiB"].index(match[2])
    return {"elapsed_seconds": round(elapsed, 2), "memory": raw,
            "memory_bytes": round(float(match[1]) * 1024 ** unit)}


def main():
    mode, app, management, container, directory = sys.argv[1:]
    evidence = Path(directory)
    evidence.mkdir(parents=True, exist_ok=True)
    duration, concurrency = 300, 4
    paths = ["/api/v1/rooms", "/api/v1/reservations", "/api/v1/stays", "/api/v1/room-types"]
    probes, memory, errors = [], [], []

    def probe():
        for path in ["/actuator/health", "/actuator/health/liveness", "/actuator/health/readiness"]:
            body = json.loads(request(management, path))
            assert body["status"] == "UP", body
            probes.append({"path": path, "status": body["status"], "at": time.time()})

    def prometheus(phase):
        body = request(management, "/actuator/prometheus")
        (evidence / f"prometheus-{phase}.txt").write_text(body)
        for metric in ["http_server_requests_seconds_count", "hikaricp_connections", "process_uptime_seconds"]:
            assert re.search(r"^" + metric + r"(?:\{|\s)", body, re.MULTILINE), metric

    probe()
    # Assert rejection by actual security filters, without altering application configuration.
    for path, kwargs, expected in [
        ("/api/v1/rooms", {"bad_signature": True}, 401),
        ("/api/v1/stays/reports/alloggiati/json?date=" + datetime.date.today().isoformat(),
         {"role": "RECEPTIONIST"}, 403),
        ("/api/v1/stays/reports/alloggiati/json?date=" + datetime.date.today().isoformat(),
         {"role": "ADMIN"}, 403),
    ]:
        try:
            request(app, path, authenticated=True, **kwargs)
            raise AssertionError(f"Expected {expected} rejection for {path}")
        except urllib.error.HTTPError as error:
            assert error.code == expected, error.code
    prometheus("before")
    state_before = json.loads(subprocess.check_output(["docker", "inspect", container]))[0]
    (evidence / "container-before.json").write_text(json.dumps({
        "State": state_before["State"], "RestartCount": state_before["RestartCount"],
    }, indent=2))
    started = time.monotonic()
    deadline = started + duration

    def worker(index):
        counts = {path: 0 for path in paths}
        failures = []
        while time.monotonic() < deadline:
            path = paths[index % len(paths)]
            try:
                body = json.loads(request(app, path, authenticated=True))
                # All endpoints must return the persisted seeded data, not an empty success body.
                rows = body if isinstance(body, list) else body.get("content", [])
                assert rows, f"Empty collection: {path}"
                counts[path] += 1
            except Exception as error:
                failures.append({"path": path, "error": str(error)})
            index += 1
            time.sleep(0.1)
        return counts, failures

    with concurrent.futures.ThreadPoolExecutor(max_workers=concurrency) as pool:
        futures = [pool.submit(worker, index) for index in range(concurrency)]
        while time.monotonic() < deadline:
            try:
                probe()
                memory.append(memory_sample(container, time.monotonic() - started))
            except Exception as error:
                errors.append({"probe": str(error)})
            time.sleep(min(10, max(0, deadline - time.monotonic())))
        counts = {path: 0 for path in paths}
        for future in futures:
            worker_counts, failures = future.result()
            for path, count in worker_counts.items():
                counts[path] += count
            errors.extend(failures)

    elapsed = time.monotonic() - started
    state_after = json.loads(subprocess.check_output(["docker", "inspect", container]))[0]
    result = {
        "mode": mode, "duration_seconds": round(elapsed, 2), "workers": concurrency,
        "worker_pause_seconds": 0.1, "requests_by_path": counts, "errors": errors,
        "probes": probes, "memory_samples": memory,
        "state_after": state_after["State"], "restart_count": state_after["RestartCount"],
        "measurement": "docker stats working set; loaded memory is peak sample during concurrent signed reads",
    }
    (evidence / "load-stability.json").write_text(json.dumps(result, indent=2))
    assert not errors, f"Runtime load/probe errors: {errors[:5]}"
    assert all(count >= 100 for count in counts.values()), counts
    assert elapsed >= duration and len(memory) >= 20, "Insufficient sustained samples"
    assert state_after["State"]["Running"] and not state_after["State"]["OOMKilled"], state_after["State"]
    assert state_before["RestartCount"] == state_after["RestartCount"] == 0, "Unexpected restart"
    probe()
    prometheus("after")
    (evidence / "load-stability.json").write_text(json.dumps(result, indent=2))
    peak = max(memory, key=lambda sample: sample["memory_bytes"])
    metrics = {
        "loaded_memory": peak["memory"], "loaded_memory_bytes": peak["memory_bytes"],
        "loaded_memory_last_bytes": memory[-1]["memory_bytes"],
        "loaded_memory_first_bytes": memory[0]["memory_bytes"],
        "loaded_memory_samples": len(memory), "load_workers": concurrency,
        "load_requests": sum(counts.values()), "load_errors": len(errors),
        "stability_duration_seconds": round(elapsed, 2),
        "stability_health_checks": len(probes), "stability_restarts": state_after["RestartCount"],
        "stability_oom_killed": "false", "liveness": "UP", "readiness": "UP",
        "prometheus": "PASS_HTTP_DB_PROCESS_METRICS", "hmac_invalid_signature": 401,
        "legacy_alloggiati_admin_receptionist_denied": 403, "stability": "PASS",
    }
    (evidence / "load-metrics.txt").write_text("".join(f"{mode}_{key}={value}\n" for key, value in metrics.items()))
    print(json.dumps({"mode": mode, "requests": sum(counts.values()), "duration": elapsed, "peak": peak}))


if __name__ == "__main__":
    main()
