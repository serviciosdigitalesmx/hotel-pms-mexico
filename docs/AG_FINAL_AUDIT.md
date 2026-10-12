# AG final audit

Evidence date: 2026-10-05.

## AG01 — final hotel leftover scan

The repository still contains 5,892 textual references to `hotel`/`camra` in
runtime and documentation. The references are not an undifferentiated residue:
`hotel_id` is the tenant isolation key, hotel settings are an active contract,
and the remaining hotel routes are consumed by the frontend and integrations.
The scan therefore confirms the AE03–AE06 decisions: no blind deletion is safe.

## AG02 — security final audit

The native Docker suite passed auth cookie rotation/replay/CSRF, tenant
isolation, receptionist RBAC, forged-header rejection, and tenant onboarding.
Gateway-generated HMAC headers remain the only accepted internal identity path.

## AG03 — dead code and duplicate contracts

The controller scan found 48 REST controllers. Backup references are live
documentation/ADR references (for example `backup/DECISIONS.md`) and are not
dead source files. The two source backup copies with zero consumers were
removed; active hotel classes and routes remain intentionally retained.

## AG04 — critical regression

Native Docker suite result: **8 passed** — auth/CSRF, real PMS journey
including payment/PDF/SMTP/checkout, portal OTP/replay, RBAC, tenant
onboarding, service orders and workflow transitions, plus assistant
prompt-injection disclosure protection. The complete `frontdesk-service` test
suite, Checkstyle and JaCoCo verification also pass, as do api-gateway tests.
Core Docker services are healthy; Grafana, Prometheus, Alertmanager, Loki and
Zipkin readiness endpoints return HTTP 200.

AF evidence is recorded in `CAMRA_TO_FIXI_PROGRESS.md`: AF01–AF10 and AF13–AF20
are complete; AF11 has a complete local Evolution webhook/inbox/outbound contract
including tenant-scoped idempotent conversation normalization (covered by
`WhatsAppConversationServiceTest`), but remains provider-pending for live
delivery, and AF12 has a complete local
public booking, token, OTP, authorization and document/UI contract but remains
runtime-browser-pending. AE03–AE07 are complete under their actual termination
criteria: active accommodation contracts are legitimate vertical bounded
contexts, applied Flyway history is append-only, tenant isolation keys are
preserved, and only zero-consumer backup source files were removed. AG05 remains
open only for the explicitly identified runtime/provider evidence, not because a
safe AE03–AE07 deletion or a local FIXI contract was left undone.
