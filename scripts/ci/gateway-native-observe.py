#!/usr/bin/env python3
"""Measure a real gateway container, with identical bounded traffic for JVM/O2."""

import argparse
import concurrent.futures
import http.cookiejar
import json
import statistics
import subprocess
import time
import urllib.request
from pathlib import Path


def docker(*args):
    return subprocess.check_output(["docker", *args], text=True, timeout=15)


def memory_sample(container):
    # cgroup v2 working set, matching Docker's exclusion of inactive file cache.
    current = int(docker("exec", container, "cat", "/sys/fs/cgroup/memory.current"))
    memory_stat = dict(line.split() for line in docker(
        "exec", container, "cat", "/sys/fs/cgroup/memory.stat").splitlines())
    return {"at_unix_ms": int(time.time() * 1000), "memory_current_bytes": current,
            "inactive_file_bytes": int(memory_stat["inactive_file"]),
            "working_set_bytes": max(0, current - int(memory_stat["inactive_file"]))}


def state(container):
    value = json.loads(docker("inspect", container))[0]
    result = {"running": value["State"]["Running"], "oom_killed": value["State"]["OOMKilled"],
              "restart_count": value["RestartCount"], "started_at": value["State"]["StartedAt"]}
    if not result["running"] or result["oom_killed"] or result["restart_count"] != 0:
        raise RuntimeError(f"Unhealthy container state: {result}")
    return result


def request(url, headers=None):
    with urllib.request.urlopen(urllib.request.Request(url, headers=headers or {}), timeout=5) as response:
        if response.status != 200:
            raise RuntimeError(f"Unexpected HTTP {response.status} for {url}")
        return response.read(), response.headers


def health(base):
    for path in ("health", "health/liveness", "health/readiness"):
        body, _ = request(f"{base}/actuator/{path}")
        if json.loads(body)["status"] != "UP":
            raise RuntimeError(f"{path} is not UP")


def observe(args):
    result = {"status": "FAIL", "mode": args.mode, "phase": args.phase,
              "memory_method": "cgroup-v2 memory.current minus inactive_file (bytes)", "samples": []}
    destination = Path(args.output) / f"{args.mode}-{args.phase}.json"
    try:
        initial_state = state(args.container)
        health(args.management_url)
        if args.phase == "idle":
            # No application traffic before this measurement, for either mode.
            time.sleep(10)
            for _ in range(3):
                result["samples"].append(memory_sample(args.container))
                time.sleep(1)
            result["idle_memory_bytes"] = int(statistics.median(
                sample["working_set_bytes"] for sample in result["samples"]))
        else:
            if args.duration < 180:
                raise ValueError("The sustained gate requires at least 180 seconds")
            cookies = http.cookiejar.MozillaCookieJar(args.cookies)
            cookies.load(ignore_discard=True)
            # Explicit local-CI Cookie header also covers Secure cookies on loopback HTTP.
            cookie_header = "; ".join(f"{cookie.name}={cookie.value}" for cookie in cookies)
            if not any(cookie.name == "jwt" for cookie in cookies):
                raise RuntimeError("Missing authenticated JWT cookie")
            started = time.monotonic()
            deadline = started + args.duration

            def worker(worker_id):
                count, failures, latencies = 0, [], []
                while time.monotonic() < deadline:
                    tick = time.monotonic()
                    endpoint = "/api/v1/rooms" if count % 2 == 0 else "/api/v1/auth/me"
                    correlation = f"gateway-{args.mode}-{worker_id}-{count}"
                    try:
                        body, headers = request(args.app_url + endpoint,
                                                {"Cookie": cookie_header, "X-Correlation-ID": correlation})
                        value = json.loads(body)
                        if endpoint.endswith("/rooms") and (
                            not isinstance(value, dict)
                            or not isinstance(value.get("content"), list)
                            or not isinstance(value.get("totalElements"), int)
                            or value.get("number") != 0
                        ):
                            raise RuntimeError("Rooms response does not match its existing paginated contract")
                        if endpoint.endswith("/me") and value.get("username") != "e2e-live-other-hotel-admin":
                            raise RuntimeError("Wrong authenticated identity")
                        if headers.get("X-Correlation-ID") != correlation:
                            raise RuntimeError("Correlation ID was not propagated")
                    except Exception as error:
                        failures.append({"request": count, "endpoint": endpoint, "error": str(error)})
                    latencies.append((time.monotonic() - tick) * 1000)
                    count += 1
                    time.sleep(max(0, min(tick + 1, deadline) - time.monotonic()))
                return {"requests": count, "failures": failures, "latencies_ms": latencies}

            with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:
                jobs = [pool.submit(worker, index) for index in range(4)]
                while time.monotonic() < deadline:
                    health(args.management_url)
                    if state(args.container) != initial_state:
                        raise RuntimeError("Container restarted during sustained traffic")
                    result["samples"].append(memory_sample(args.container))
                    time.sleep(max(0, min(5, deadline - time.monotonic())))
                workers = [job.result() for job in jobs]
            result["duration_seconds"] = round(time.monotonic() - started, 3)
            result["workers"] = 4
            result["target_requests_per_second"] = 4
            result["requests"] = sum(worker["requests"] for worker in workers)
            result["errors"] = [failure for worker in workers for failure in worker["failures"]]
            latencies = sorted(latency for worker in workers for latency in worker["latencies_ms"])
            result["latency_p95_ms"] = round(latencies[int((len(latencies) - 1) * 0.95)], 3)
            working_sets = [sample["working_set_bytes"] for sample in result["samples"]]
            result["loaded_memory_bytes"] = max(working_sets)
            result["loaded_memory_mean_bytes"] = int(statistics.mean(working_sets))
            result["loaded_memory_first_bytes"] = working_sets[0]
            result["loaded_memory_last_bytes"] = working_sets[-1]
            prometheus, _ = request(f"{args.management_url}/actuator/prometheus")
            (Path(args.output) / f"{args.mode}-prometheus.txt").write_bytes(prometheus)
            if b"http_server_requests_seconds_count" not in prometheus:
                raise RuntimeError("Prometheus is missing HTTP request counters")
            if result["errors"] or result["requests"] < args.duration * 2 or len(working_sets) < 30:
                raise RuntimeError("Sustained traffic failed or insufficient traffic/memory samples")
        health(args.management_url)
        result["container_state"] = state(args.container)
        if result["container_state"] != initial_state:
            raise RuntimeError("Container state changed during observation")
        result["status"] = "PASS"
    except Exception as error:
        result["error"] = str(error)
        raise
    finally:
        destination.write_text(json.dumps(result, indent=2) + "\n")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--mode", choices=("native", "jvm"), required=True)
    parser.add_argument("--phase", choices=("idle", "loaded"), required=True)
    parser.add_argument("--container", required=True)
    parser.add_argument("--management-url", required=True)
    parser.add_argument("--app-url")
    parser.add_argument("--cookies")
    parser.add_argument("--output", required=True)
    parser.add_argument("--duration", type=int, default=180)
    observe(parser.parse_args())
