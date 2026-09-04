#!/usr/bin/env python3
"""Exercise the existing quotation REST/PDF contract against a running service."""

import hashlib
import hmac
import json
import os
from pathlib import Path
from datetime import datetime
import re
import secrets
import subprocess
import sys
import time
import urllib.error
import urllib.request


def main():
    mode, base, directory = sys.argv[1:]
    root = Path(directory)
    result = {"mode": mode, "status": "FAIL"}
    hotel = "00000000-0000-0000-0000-000000000101"

    def request(method, path, name, *, body=None, tenant=hotel, authenticated=True, expected=200):
        headers = {}
        if authenticated:
            timestamp, nonce = str(time.time_ns() // 1_000_000), secrets.token_hex(16)
            signature = hmac.new(os.environ["CI_HMAC_SECRET"].encode(),
                                 f"ci-admin:ADMIN:{tenant}:{timestamp}:{nonce}".encode(),
                                 hashlib.sha256).hexdigest()
            headers = {"X-Auth-User": "ci-admin", "X-Auth-Role": "ADMIN", "X-Auth-Hotel": tenant,
                       "X-Auth-Timestamp": timestamp, "X-Auth-Nonce": nonce,
                       "X-Internal-Signature": signature}
        data = None if body is None else json.dumps(body).encode()
        if data is not None:
            headers["Content-Type"] = "application/json"
        req = urllib.request.Request(base + path, data=data, headers=headers, method=method)
        try:
            response = urllib.request.urlopen(req, timeout=60)
        except urllib.error.HTTPError as error:
            response = error
        with response:
            content, status, response_headers = response.read(), response.status, dict(response.headers)
        (root / name).write_bytes(content)
        (root / (name + ".http.json")).write_text(json.dumps({"method": method, "path": path,
            "status": status, "headers": response_headers}, indent=2))
        assert status == expected, f"{method} {path}: expected {expected}, actual {status}; see {name}"
        return content, {key.lower(): value for key, value in response_headers.items()}

    try:
        room = json.loads((root / "room.json").read_text())
        guest = json.loads((root / "guest-create.json").read_text())
        reservation = json.loads((root / "reservation.json").read_text())
        payload = {"guestId": guest["id"], "checkInDate": reservation["checkInDate"],
                   "checkOutDate": reservation["checkOutDate"], "validUntil": reservation["checkInDate"],
                   "expectedGuests": 1, "options": [{"label": "Opción PDF Native", "roomIds": [room["id"]]}]}
        (root / "quotation-request.json").write_text(json.dumps(payload, indent=2))
        content, _ = request("POST", "/api/v1/quotations", "quotation-create.json", body=payload, expected=201)
        quotation = json.loads(content)
        assert quotation["status"] == "DRAFT" and quotation["guestId"] == guest["id"], quotation
        assert quotation["totalPrice"] > 0 and len(quotation["options"]) == 1, quotation
        quote_path = "/api/v1/quotations/" + quotation["id"]
        persisted, _ = request("GET", quote_path, "quotation-get.json")
        persisted = json.loads(persisted)
        # PostgreSQL stores timestamps at microsecond precision, while the POST
        # response can still carry Java nanoseconds. Compare the stable REST
        # contract exactly and timestamps as instants instead of comparing raw
        # JSON serialization precision.
        stable_fields = ("id", "guestId", "guestFullName", "prospectEmail",
                         "checkInDate", "checkOutDate", "expectedGuests", "status",
                         "validUntil", "totalPrice", "options", "acceptedOptionId",
                         "sendFailed", "sendFailureReason")
        for field in stable_fields:
            assert persisted[field] == quotation[field], \
                f"Persisted quotation field changed: {field}"
        for field in ("createdAt", "updatedAt"):
            # PostgreSQL rounds Java's nanoseconds to its microsecond storage
            # precision. Accept only that representation boundary (<= 1 us).
            delta = abs(datetime.fromisoformat(persisted[field])
                        - datetime.fromisoformat(quotation[field]))
            assert delta.total_seconds() <= 0.000001, \
                f"Persisted quotation timestamp changed beyond database precision: {field}"
        request("GET", quote_path + "/pdf", "quotation-cross-tenant.json",
                tenant="00000000-0000-0000-0000-000000000202", expected=404)
        request("GET", quote_path + "/pdf", "quotation-missing-hmac.json", authenticated=False, expected=401)
        expected_text = ["COTIZACIÓN", "Habitación", quotation["guestFullName"], room["roomNumber"],
                         "Opción PDF Native", f'{quotation["totalPrice"]:,.2f}']
        checks = []
        for index in (1, 2):
            name = f"quotation-{index}"
            content, headers = request("GET", quote_path + "/pdf", name + ".pdf")
            assert content.startswith(b"%PDF-") and "application/pdf" in headers.get("content-type", ""), headers
            assert "attachment" in headers.get("content-disposition", "") and quotation["id"] in headers["content-disposition"], headers
            subprocess.run(["pdftotext", "-layout", str(root / (name + ".pdf")), str(root / (name + "-text.txt"))], check=True)
            text = (root / (name + "-text.txt")).read_text()
            for expected in expected_text:
                assert expected in text, f"{name}: missing text {expected!r}"
            fonts = subprocess.check_output(["pdffonts", str(root / (name + ".pdf"))], text=True)
            (root / (name + "-fonts.txt")).write_text(fonts)
            rows = [row.split() for row in fonts.splitlines()[2:] if row.strip()]
            assert len(rows) >= 2, f"{name}: missing fonts: {fonts}"
            assert all("NotoSans" in row[0] and row[-5] == "yes" and row[-3] == "yes" for row in rows), fonts
            assert any("Bold" in row[0] for row in rows) and any("Regular" in row[0] for row in rows), fonts
            info = subprocess.check_output(["pdfinfo", str(root / (name + ".pdf"))], text=True)
            (root / (name + "-info.txt")).write_text(info)
            pages = int(re.search(r"^Pages:\s+(\d+)", info, re.MULTILINE)[1])
            assert pages > 0, info
            checks.append({"bytes": len(content), "sha256": hashlib.sha256(content).hexdigest(),
                           "pages": pages, "fonts": [row[0] for row in rows], "expected_text": expected_text})
        subprocess.run(["pdftoppm", "-scale-to", "1400", "-png", str(root / "quotation-1.pdf"),
                        str(root / "quotation-preview")], check=True)
        result.update(status="PASS", quotation_id=quotation["id"], renders=checks,
                      cross_tenant=404, missing_hmac=401,
                      persistence="STABLE_FIELDS_MATCH_AND_TIMESTAMPS_WITHIN_POSTGRES_PRECISION")
    except Exception as error:
        result["error"] = str(error)
        raise
    finally:
        (root / "pdf-result.json").write_text(json.dumps(result, indent=2, ensure_ascii=False))
        (root / "pdf-metrics.txt").write_text(f"{mode}_quotation_pdf={result['status']}\n"
            f"{mode}_quotation_pdf_render_count={len(result.get('renders', []))}\n")
        print(json.dumps(result, ensure_ascii=False))


if __name__ == "__main__":
    main()
