#!/usr/bin/env python3
"""Exercise actual Config Server images and preserve JVM/Native evidence."""
import base64
import concurrent.futures
import json
import os
from pathlib import Path
import re
import secrets
import subprocess
import sys
import threading
import time
import urllib.error
import urllib.request

ROOT = Path("config-service/src/main/resources")
RESULT = Path(os.environ.get("RESULT_DIR", "build/config-native-runtime"))
USERNAME = "runtime-" + secrets.token_hex(8)
PASSWORD = secrets.token_urlsafe(32)
AUTH = "Basic " + base64.b64encode(f"{USERNAME}:{PASSWORD}".encode()).decode()
NETWORK = "config-native-ci"
LOAD_SECONDS = int(os.environ.get("CONFIG_STABILITY_SECONDS", "180"))
CONCURRENCY = 4
METRICS = {"native_build_mode": os.environ.get("CI_NATIVE_BUILD_MODE", "unknown")}
CONTAINERS = []


def require(condition, message):
    if not condition:
        raise RuntimeError(message)


def docker(*args):
    return subprocess.check_output(["docker", *args], text=True).strip()


def save(name, value):
    (RESULT / name).write_text(json.dumps(value, indent=2, sort_keys=True) + "\n")


def request(port, path, expected=200, authorization=None):
    headers = {"Authorization": authorization} if authorization else {}
    req = urllib.request.Request(f"http://127.0.0.1:{port}{path}", headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=10) as response:
            status, body = response.status, response.read()
    except urllib.error.HTTPError as error:
        status, body = error.code, error.read()
    require(status == expected, f"{path}: expected HTTP {expected}, got {status}")
    return body


def config_matrix():
    files = sorted((ROOT / "config").glob("*.yml"))
    apps = ["application"] + sorted(
        p.name for p in Path(".").iterdir()
        if p.is_dir() and (p / "build.gradle.kts").is_file()
        and (ROOT / "config" / f"{p.name}.yml").is_file()
    )
    profiles = {"default", "dev", "test", "prod"}
    for path in files:
        for app in apps:
            if path.stem.startswith(app + "-"):
                profiles.add(path.stem[len(app) + 1:])
    matrix = []
    for app in apps:
        for profile in sorted(profiles | ({app} if app != "application" else set())):
            expected = {"application.yml", f"{app}.yml"}
            expected.update(p.name for p in files if p.stem in {
                f"application-{profile}", f"{app}-{profile}"
            })
            matrix.append({"app": app, "profile": profile, "sources": sorted(expected)})
    return matrix


def preflight():
    require(LOAD_SECONDS >= 180, "stability must run for at least 180 seconds")
    for path in ROOT.rglob("*.yml"):
        require(not re.search(
            r'^\s*(?:password|secret|token|private-key):\s*[\"\']?(?!\$|[\"\']?$)[A-Za-z0-9]',
            path.read_text(), re.MULTILINE), f"literal sensitive value in {path}")
    dockerfile = Path("config-service/Dockerfile.native").read_text()
    require(not re.search(r"^(ARG|ENV)\s+.*(PASSWORD|SECRET|TOKEN)", dockerfile, re.MULTILINE),
            "secret-bearing native Dockerfile ARG/ENV")
    metadata = ROOT / "META-INF/native-image/com.hotelpms/config-service/resource-config.json"
    patterns = [re.compile(e["pattern"]) for e in json.loads(metadata.read_text())["resources"]["includes"]]
    for path in (ROOT / "config").glob("*.yml"):
        require(any(p.fullmatch(str(path.relative_to(ROOT))) for p in patterns),
                f"missing native resource inclusion: {path.name}")
    return config_matrix()


def check_management(prefix, port):
    for endpoint in ("health", "health/liveness", "health/readiness"):
        data = json.loads(request(port, "/actuator/" + endpoint))
        require(data["status"] == "UP", f"{prefix} {endpoint} is not UP")
        save(f"{prefix}-{endpoint.replace('/', '-')}.json", data)
    prometheus = request(port, "/actuator/prometheus").decode()
    require("# HELP" in prometheus and "process_uptime_seconds" in prometheus,
            f"{prefix} missing real Prometheus process metrics")
    (RESULT / f"{prefix}-prometheus.txt").write_text(prometheus)
    request(port, "/actuator/info", 401)
    request(port, "/actuator/info", authorization=AUTH)
    METRICS.update({f"{prefix}_health_status": "UP", f"{prefix}_liveness": "UP",
                    f"{prefix}_readiness": "UP", f"{prefix}_prometheus": 200,
                    f"{prefix}_info_unauthenticated": 401,
                    f"{prefix}_info_authenticated": 200})


def memory(container):
    used = docker("stats", "--no-stream", "--format", "{{.MemUsage}}", container).split(" / ")[0]
    match = re.fullmatch(r"([0-9.]+)([kKMGT]?i?B)", used)
    require(match is not None, f"cannot parse docker memory: {used}")
    units = {"B": 1, "kB": 1000, "KB": 1000, "MB": 1000**2, "GB": 1000**3,
             "KiB": 1024, "MiB": 1024**2, "GiB": 1024**3, "TiB": 1024**4}
    return {"memory": used, "bytes": round(float(match[1]) * units[match[2]])}


def start(prefix, port, management_port):
    image = os.environ.get(prefix.upper() + "_IMAGE", f"hotel-pms/config-service-{prefix}:ci")
    container = f"config-service-{prefix}"
    CONTAINERS.append(container)
    started = time.monotonic()
    docker("run", "--detach", "--name", container, "--network", NETWORK,
           "--network-alias", f"config-server-{prefix}",
           "--publish", f"127.0.0.1:{port}:8888", "--publish", f"127.0.0.1:{management_port}:8090",
           "--env", f"CONFIG_SERVER_USERNAME={USERNAME}",
           "--env", f"CONFIG_SERVER_PASSWORD={PASSWORD}", image)
    while True:
        try:
            require(json.loads(request(management_port, "/actuator/health"))["status"] == "UP",
                    f"{prefix} startup health is not UP")
            break
        except Exception:
            require(time.monotonic() - started < 180, f"{prefix} startup timed out")
            time.sleep(0.1)
    METRICS[f"{prefix}_startup_ms"] = round((time.monotonic() - started) * 1000)
    METRICS[f"{prefix}_image_size_bytes"] = int(docker("image", "inspect", image, "--format", "{{.Size}}"))
    time.sleep(5)
    idle = memory(container)
    METRICS[f"{prefix}_idle_memory"] = idle["memory"]
    METRICS[f"{prefix}_idle_memory_bytes"] = idle["bytes"]
    check_management(prefix, management_port)
    return container


def check_profiles(prefix, port, matrix):
    responses, statuses = {}, []
    wrong = "Basic " + base64.b64encode(f"{USERNAME}:wrong-password".encode()).decode()
    baked = "Basic " + base64.b64encode(b"configuser:build-only-config-password").decode()
    for item in matrix:
        path = f"/{item['app']}/{item['profile']}"
        request(port, path, 401)
        request(port, path, 401, wrong)
        request(port, path, 401, baked)
        body = request(port, path, authorization=AUTH)
        require(PASSWORD.encode() not in body and b"build-only-config-password" not in body,
                f"{prefix} {path} exposed Config Server credentials")
        data = json.loads(body)
        require(data["name"] == item["app"] and data["profiles"] == [item["profile"]],
                f"{prefix} {path} returned incorrect name/profiles")
        names = {source["name"].rsplit("/", 1)[-1] for source in data["propertySources"]}
        require(set(item["sources"]) <= names,
                f"{prefix} {path} missing required sources {set(item['sources']) - names}")
        require(all(source["source"] for source in data["propertySources"]),
                f"{prefix} {path} contains an empty property source")
        if path == "/api-gateway/prod":
            effective = {}
            for source in reversed(data["propertySources"]):
                effective.update(source["source"])
            require(effective["springdoc.api-docs.enabled"] is False
                    and effective["springdoc.swagger-ui.enabled"] is False,
                    f"{prefix} production profile override was lost")
        responses[path] = data
        statuses.append({"path": path, "authenticated": 200, "unauthenticated": 401,
                         "wrong_password": 401, "build_password": 401, "sources": sorted(names)})
    save(f"{prefix}-config-responses.json", responses)
    save(f"{prefix}-profile-gates.json", statuses)
    METRICS[f"{prefix}_authenticated_profiles"] = len(statuses)
    METRICS.update({f"{prefix}_config_authenticated": 200,
                    f"{prefix}_config_unauthenticated": 401,
                    f"{prefix}_config_wrong_password": 401,
                    f"{prefix}_config_build_password": 401})
    return responses


def stable_load(prefix, container, port, management_port, baseline):
    started, stop = time.monotonic(), threading.Event()
    paths, samples = list(baseline), []
    initial = json.loads(docker("inspect", container))[0]

    def worker(index):
        count = 0
        while not stop.is_set():
            path = paths[(index + count) % len(paths)]
            body = json.loads(request(port, path, authorization=AUTH))
            require(body == baseline[path], f"{prefix} config changed under load: {path}")
            count += 1
            stop.wait(0.05)
        return count

    with concurrent.futures.ThreadPoolExecutor(max_workers=CONCURRENCY) as pool:
        workers = [pool.submit(worker, i) for i in range(CONCURRENCY)]
        try:
            while time.monotonic() - started < LOAD_SECONDS:
                sample = memory(container)
                sample["elapsed_seconds"] = round(time.monotonic() - started, 3)
                samples.append(sample)
                check_management(prefix, management_port)
                state = json.loads(docker("inspect", container))[0]
                require(state["State"]["Running"] and not state["State"]["OOMKilled"]
                        and state["RestartCount"] == initial["RestartCount"]
                        and state["State"]["StartedAt"] == initial["State"]["StartedAt"],
                        f"{prefix} stopped, restarted or OOMed under load")
                for future in workers:
                    if future.done():
                        future.result()
                        raise RuntimeError(f"{prefix} load worker stopped early")
                time.sleep(min(8, max(0, LOAD_SECONDS - (time.monotonic() - started))))
        finally:
            stop.set()
        requests = sum(future.result() for future in workers)
    duration = time.monotonic() - started
    require(requests >= len(paths) * CONCURRENCY and len(samples) >= 10,
            f"{prefix} insufficient sustained load coverage")
    require(all(s["bytes"] > 0 for s in samples), "missing loaded memory samples")
    peak = max(samples, key=lambda sample: sample["bytes"])
    save(f"{prefix}-memory-samples.json", samples)
    save(f"{prefix}-stability.json", {
        "status": "PASS", "duration_seconds": round(duration, 3), "requests": requests,
        "errors": 0, "concurrency": CONCURRENCY, "restarts": 0, "oom_killed": False,
        "profile_response_parity": "PASS", "health_liveness_readiness_prometheus": "PASS",
    })
    METRICS.update({f"{prefix}_loaded_memory": peak["memory"],
                    f"{prefix}_loaded_memory_bytes": peak["bytes"],
                    f"{prefix}_stability_seconds": round(duration, 3),
                    f"{prefix}_load_requests": requests, f"{prefix}_load_errors": 0,
                    f"{prefix}_stability": "PASS"})
    print(f"{prefix}: {requests} valid requests over {duration:.1f}s, peak {peak['memory']}", flush=True)


def main():
    matrix = preflight()
    if "--validate-only" in sys.argv:
        print(f"Validated resource coverage and {len(matrix)} config/profile cases")
        return
    RESULT.mkdir(parents=True, exist_ok=True)
    save("config-matrix.json", matrix)
    docker("network", "create", NETWORK)
    try:
        native = start("native", 28888, 28090)
        native_configs = check_profiles("native", 28888, matrix)
        jvm = start("jvm", 28889, 28091)
        jvm_configs = check_profiles("jvm", 28889, matrix)
        require(native_configs == jvm_configs, "Native/JVM config content or source order differs")
        METRICS["config_content_parity"] = "PASS"
        METRICS["runtime_credentials"] = "PASS"
        METRICS["placeholder_secrets"] = "PASS"
        stable_load("native", native, 28888, 28090, native_configs)
        stable_load("jvm", jvm, 28889, 28091, jvm_configs)
        METRICS["gate_status"] = "PASS"
        METRICS["startup_measurement"] = "docker_run_to_health_UP_100ms_poll"
        METRICS["loaded_memory_measurement"] = "peak_docker_stats_samples_under_4_concurrent_clients"
        save("metrics.json", METRICS)
        lines = [f"{key}={value}" for key, value in METRICS.items()]
        (RESULT / "metrics.txt").write_text("\n".join(lines) + "\n")
        print("\n".join(lines), flush=True)
    finally:
        for container in CONTAINERS:
            logs = subprocess.run(["docker", "logs", container], text=True, capture_output=True)
            (RESULT / f"{container}.log").write_text(logs.stdout + logs.stderr)
            state = subprocess.run(["docker", "inspect", "--format", "{{json .State}}", container],
                                   text=True, capture_output=True)
            (RESULT / f"{container}-state.json").write_text(state.stdout)
            subprocess.run(["docker", "rm", "--force", container], capture_output=True)
        subprocess.run(["docker", "network", "rm", NETWORK], capture_output=True)


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print(f"NATIVE_GATE_FAIL: {error}", file=sys.stderr)
        sys.exit(1)
