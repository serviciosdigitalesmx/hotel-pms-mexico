#!/usr/bin/env python3
"""Real Auth probes and bounded load, using only the runner's standard library.

This is a loopback HTTP harness; Secure cookies remain unchanged on the server.
Each worker explicitly sends its own cookies, without sharing refresh tokens.
Memory is cgroup-v2 memory.current minus inactive_file (Docker working set).
"""

import argparse
import base64
import concurrent.futures
import hmac
import json
import os
from pathlib import Path
from http.cookies import SimpleCookie
import statistics
import subprocess
import threading
import time
import urllib.error
import urllib.parse
import urllib.request


def require(condition, message):
    if not condition:
        raise RuntimeError(message)


def docker(*args):
    return subprocess.check_output(["docker", *args], text=True, timeout=20).strip()


def decode(part):
    return base64.urlsafe_b64decode(part + "=" * (-len(part) % 4))


def encode(data):
    return base64.urlsafe_b64encode(data).decode().rstrip("=")


def claims(token):
    header, payload, signature = token.split(".")
    require(json.loads(decode(header))["alg"] == "HS256", "JWT algorithm changed")
    expected = hmac.digest(base64.b64decode(os.environ["CI_JWT_SECRET"]),
                           f"{header}.{payload}".encode(), "sha256")
    require(hmac.compare_digest(expected, decode(signature)), "JWT signature invalid")
    value = json.loads(decode(payload))
    require(value["sub"] == "admin" and value["role"] == "ADMIN"
            and value["hotelId"] == "00000000-0000-0000-0000-000000000001"
            and value["exp"] > time.time(), "JWT identity/tenant/expiry invalid")
    return value


class Gate:
    def __init__(self, args):
        self.args = args
        self.base = f"http://127.0.0.1:{args.port}"
        self.management = f"http://127.0.0.1:{args.management_port}"
        self.lock = threading.Lock()
        self.counts = {}
        self.samples = []
        self.stop = threading.Event()

    def request(self, url, expected=200, body=None, cookies=None, method=None):
        headers = {}
        if cookies:
            headers["Cookie"] = "; ".join(f"{k}={v}" for k, v in cookies.items())
        if body is not None:
            headers["Content-Type"] = "application/json"
            body = json.dumps(body).encode()
        request = urllib.request.Request(url, data=body, headers=headers, method=method)
        try:
            response = urllib.request.urlopen(request, timeout=10)
        except urllib.error.HTTPError as error:
            response = error
        with response:
            status, data, response_headers = response.status, response.read(), response.headers
        path = urllib.parse.urlparse(url).path
        with self.lock:
            key = f"{request.method} {path} {status}"
            self.counts[key] = self.counts.get(key, 0) + 1
        require(status == expected, f"{path}: HTTP {status}, expected {expected}")
        if cookies is not None and expected == 200:
            for raw in response_headers.get_all("Set-Cookie", []):
                for name, cookie in SimpleCookie(raw).items():
                    require(cookie["secure"] and cookie["samesite"] == "Strict",
                            f"{name}: cookie flags changed")
                    if name in ("jwt", "refresh_token"):
                        require(cookie["httponly"], f"{name}: HttpOnly missing")
                    cookies[name] = cookie.value
        return data

    def flow(self, cookies):
        body = self.request(self.base + "/api/v1/auth/login", cookies=cookies,
                            body={"username": "admin", "password": "password"})
        require(json.loads(body)["mustChangePassword"] is True, "login state changed")
        claims(cookies["jwt"])
        old_refresh = cookies["refresh_token"]
        old_claims = claims(old_refresh)
        self.request(self.base + "/api/v1/auth/refresh", cookies=cookies, method="POST")
        refreshed = claims(cookies["refresh_token"])
        require(refreshed["typ"] == "refresh" and refreshed["jti"] != old_claims["jti"],
                "refresh rotation failed")
        claims(cookies["jwt"])
        body = json.loads(self.request(self.base + "/api/v1/auth/me", cookies=cookies))
        require(body["username"] == "admin" and body["role"] == "ADMIN"
                and body["mustChangePassword"] is True, "auth/me identity changed")
        return old_refresh

    def probes(self, phase):
        for name in ("health", "health/liveness", "health/readiness"):
            data = self.request(self.management + "/actuator/" + name)
            require(json.loads(data)["status"] == "UP", f"{name} not UP")
            (self.args.output / f"{self.args.scope}-{phase}-{name.replace('/', '-')}.json").write_bytes(data)
        data = self.request(self.management + "/actuator/prometheus")
        require(b"http_server_requests_seconds" in data, "Prometheus HTTP metrics missing")
        (self.args.output / f"{self.args.scope}-{phase}-prometheus.txt").write_bytes(data)

    def state(self):
        value = json.loads(docker("inspect", self.args.container))[0]
        require(value["State"]["Running"] and not value["State"]["OOMKilled"]
                and value["RestartCount"] == 0, "container stopped, restarted or OOM killed")
        return {"id": value["Id"], "started_at": value["State"]["StartedAt"],
                "restart_count": value["RestartCount"], "oom_killed": value["State"]["OOMKilled"]}

    def memory(self, phase, started):
        raw = docker("exec", self.args.container, "sh", "-c",
                     "cat /sys/fs/cgroup/memory.current; cat /sys/fs/cgroup/memory.stat")
        lines = raw.splitlines()
        total = int(lines[0])
        inactive = dict(line.split() for line in lines[1:])["inactive_file"]
        working = total - int(inactive)
        require(working > 0, "cgroup working set was empty")
        self.samples.append({"phase": phase, "elapsed_seconds": round(time.monotonic() - started, 3),
                             "memory_bytes": working, "cgroup_current_bytes": total})

    def negatives(self):
        cookies = {}
        old_refresh = self.flow(cookies)
        self.request(self.base + "/api/v1/auth/refresh", expected=401, method="POST",
                     cookies={"refresh_token": old_refresh})
        self.request(self.base + "/api/v1/auth/login", expected=401,
                     body={"username": "admin", "password": "invalid-ci-password"})
        header, payload, signature = cookies["jwt"].split(".")
        tampered = f"{header}.{payload}.{encode(bytes([decode(signature)[0] ^ 1]) + decode(signature)[1:])}"
        expired_claims = json.loads(decode(payload))
        expired_claims["exp"] = int(time.time()) - 60
        expired_payload = encode(json.dumps(expired_claims).encode())
        signed = f"{header}.{expired_payload}"
        expired = signed + "." + encode(hmac.digest(base64.b64decode(os.environ["CI_JWT_SECRET"]),
                                                       signed.encode(), "sha256"))
        for token in (None, "malformed", tampered, expired):
            self.request(self.base + "/api/v1/auth/me", expected=401,
                         cookies={"jwt": token} if token else {})
            self.request(self.base + "/api/v1/auth/refresh", expected=401, method="POST",
                         cookies={"refresh_token": token} if token else {})

    def worker(self, deadline):
        cookies = {}
        cycles = 0
        try:
            while time.monotonic() < deadline and not self.stop.is_set():
                self.flow(cookies)
                cycles += 1
                self.stop.wait(0.25)
        except Exception:
            self.stop.set()
            raise
        return cycles

    def run(self):
        result = {"scope": self.args.scope, "status": "FAIL", "http_counts": self.counts,
                  "memory_samples": self.samples, "workers": 3,
                  "memory_method": "cgroup v2 memory.current minus inactive_file, bytes",
                  "idle_method": "3 samples over 10 seconds before HTTP business load",
                  "loaded_method": "median and peak sampled every 5 seconds during 3 concurrent login/refresh/me loops"}
        started = time.monotonic()
        try:
            before = self.state()
            for _ in range(3):
                time.sleep(5)
                self.memory("idle", started)
            self.negatives()
            self.probes("before-load")
            load_started = time.monotonic()
            deadline = load_started + self.args.seconds
            with concurrent.futures.ThreadPoolExecutor(max_workers=3) as pool:
                workers = [pool.submit(self.worker, deadline) for _ in range(3)]
                while time.monotonic() < deadline and not self.stop.is_set():
                    self.memory("loaded", load_started)
                    self.probes("during-load")
                    require(self.state() == before, "container changed during load")
                    self.stop.wait(min(5, max(0, deadline - time.monotonic())))
                cycles = [worker.result() for worker in workers]
            elapsed = time.monotonic() - load_started
            require(all(count >= 20 for count in cycles), "too few real auth cycles")
            require(elapsed >= self.args.seconds, "stability window did not complete")
            self.probes("after-load")
            require(self.state() == before, "container changed after load")
            idle = [s["memory_bytes"] for s in self.samples if s["phase"] == "idle"]
            loaded = [s["memory_bytes"] for s in self.samples if s["phase"] == "loaded"]
            result.update(status="PASS", idle_memory_bytes=int(statistics.median(idle)),
                          loaded_memory_bytes=int(statistics.median(loaded)),
                          loaded_peak_memory_bytes=max(loaded), stability_seconds=round(elapsed, 3),
                          cycles_per_worker=cycles, completed_auth_cycles=sum(cycles),
                          container_state=before, http_5xx=0, request_failures=0,
                          jwt_signature_and_expiry="PASS", invalid_jwt_and_refresh="PASS",
                          refresh_replay="PASS", cookie_flags="PASS", prometheus="PASS",
                          liveness="UP", readiness="UP")
        except Exception as error:
            result["error"] = str(error)
            raise
        finally:
            (self.args.output / f"{self.args.scope}-measurement.json").write_text(json.dumps(result, indent=2) + "\n")
        print(json.dumps({key: value for key, value in result.items() if key not in ("memory_samples", "http_counts")}, indent=2))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--scope", choices=("native", "jvm"), required=True)
    parser.add_argument("--port", type=int, required=True)
    parser.add_argument("--management-port", type=int, required=True)
    parser.add_argument("--container", required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--seconds", type=int, default=300)
    arguments = parser.parse_args()
    require(arguments.seconds >= 300, "final stability gate requires at least 300 seconds")
    Gate(arguments).run()
