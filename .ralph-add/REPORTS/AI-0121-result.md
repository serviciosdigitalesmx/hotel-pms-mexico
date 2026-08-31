# AI-0121 Hypervelocity Result

- Result: VERIFY_FAIL
- Worker: isolated Codex worktree
- Integration applier: single serialized real-worktree applier
- Sandbox pretest: FAIL
- Integration: applied 3 paths; backup=/Users/usuario/.ralph-hotel/backups/AI-0121-20260822-052246
- Changed paths:
  - docs/FINAL_AUDIT_ULTRA_SEVERE.md
  - docs/OPERATIONS_RUNBOOK.md
  - docs/ROADMAP.md

## Worker log tail
```text
tain 14gg (P3 parz. — manca copia off-site) — no K8s HA (SPOF singolo host) |
+| Affidabilità | 8.5/10 | Circuit breaker, saga checkIn, `@Version` Invoice, pgBackRest con WAL archiving e backup full/incrementali (P3 parz. — copia off-site opt-in e restore CI pendenti) — no K8s HA (SPOF singolo host) |
 | Osservabilità | 8.5/10 | Zipkin + Prometheus + Loki + Alertmanager con 6 alert rule (P4 ✅) + Runbook (P5 ✅) — Zipkin → Tempo rimane C9 opzionale |
 | Scalabilità | 7.0/10 | 8 servizi, GIN pg_trgm, frontdesk consolidato (ADR-001 ✅), Dependabot (P9 ✅) — SimpleDiscovery statico, no K8s (E5) |
 | Qualità codice | 9.0/10 | PMD zero, Testcontainers billing+frontdesk (P7 ✅), coverage gate 90/80/88/92% (P15 ✅), Zod validation (P11 ✅), SRP refactor (P10 ✅) |
@@ -40,8 +40,8 @@
 |---|---|---|---|---|
 | P1 | ~~`@Version` su `Invoice` + migration Flyway~~ | ✅ **Fatto** | — | Implementato: `Invoice.java` campo `@Version Long version`, Flyway V3. Audit Sprint 1 (2026-07-25): trovato e corretto un gap reale — `billing-service` non aveva l'`@ExceptionHandler(ObjectOptimisticLockingFailureException.class)` (presente invece in frontdesk-service), quindi un conflitto di scrittura concorrente su fattura tornava HTTP 500 invece di 409. Aggiunto handler + test reale (2 letture della stessa riga, 1a save ok, 2a save stale → eccezione verificata su Testcontainers) + unit test sul mapping a 409 |
 | P2 | ~~`restart: unless-stopped` in docker-compose~~ | ✅ **Fatto** | — | Tutti i 17 container in `docker-compose.yml` già configurati (numero corretto 2026-07-25, era stale a 16 da prima di Alertmanager/notification-service) |
-| P0 | ~~Hardening porte Docker — compose dev/prod separati~~ | ✅ **Fatto** | — | `docker-compose.prod.yml` creato: usa il merge-tag `!reset` per azzerare `ports` su Postgres/Redis/Prometheus/Zipkin/Loki/Grafana/config-server/tutti i backend — solo frontend (:80) e api-gateway (:8080) restano pubblicati sull'host. `docker-compose.yml` invariato (dev). Verificato con `docker compose ... config`. Uso: `docker compose -f docker-compose.yml -f docker-compose.prod.yml --profile observability --profile backup up -d` (2026-07-17: loki/grafana/zipkin/alertmanager/prometheus/db-backup sono ora dietro profilo opt-in in `docker-compose.yml` per uno stack dev più leggero — i flag `--profile` sono obbligatori anche in prod, un override non riattiva un servizio gated). OWASP A05 |
-| P3 | ~~Backup PostgreSQL automatizzato (pg_dump cron)~~ | 🟡 **Parziale** | — | Container `db-backup` in `docker-compose.yml`: `pg_dumpall` ogni 24h (configurabile), gzip, retention 14gg, volume dedicato `postgres_backups`, nessuna porta host. Verificato con dump reale (240K, 46 CREATE TABLE/DATABASE). RPO 24h accettato esplicitamente, non PITR (`backup/DECISIONS.md §3.5`). Restano da fare: copia esterna cifrata automatica (S3/B2) — oggi il volume Docker resta single-host, single-disk, single point of failure fisico — e un test di restore end-to-end reale (piano di rifinitura Fase 6, item 4/6) |
+| P0 | ~~Hardening porte Docker — compose dev/prod separati~~ | ✅ **Fatto** | — | `docker-compose.prod.yml` creato: usa il merge-tag `!reset` per azzerare `ports` su Postgres/Redis/Prometheus/Zipkin/Loki/Grafana/config-server/tutti i backend — solo frontend (:80) e api-gateway (:8080) restano pubblicati sull'host. `docker-compose.yml` invariato (dev). Verificato con `docker compose ... config`. Uso: `docker compose -f docker-compose.yml -f docker-compose.prod.yml --profile observability up -d`; non esiste più un profilo `backup`, perché pgBackRest è integrato nel servizio `postgres`. OWASP A05 |
+| P3 | ~~Backup PostgreSQL automatizzato (pg_dump cron)~~ | 🟡 **Parziale** | — | **Modello storico:** `db-backup`/`pg_dumpall` ogni 24h, gzip e retention 14gg. Modello attuale nel checkout: pgBackRest integrato in `postgres`, WAL archiving e backup full/incrementali; repo2 off-site resta opt-in. Restano pendenti la copia off-site configurata e il drill CI di restore (nessuna approvazione operativa implicita dalla verifica statica) |
 | P4 | ~~Prometheus alert rules (error rate, latency, restarts)~~ | ✅ **Fatto** | — | `alert_rules.yml` con 6 regole (ServiceDown/HighErrorRate/HighLatencyP99/JvmHeapHigh/CircuitBreakerOpen/DbConnectionPoolNearExhaustion); Alertmanager v0.27 aggiunto a compose; histogrammi Micrometer abilitati via shared `config/application.yml` |
 | P5 | ~~Operations Runbook~~ | ✅ **Fatto** | — | `docs/OPERATIONS_RUNBOOK.md` creato con 10 procedure operative |
 | P6 | ~~GIN index + `pg_trgm` su `GuestRepository`~~ | ✅ **Fatto** | — | Flyway V7 `V7__add_trgm_search_indexes.sql`: 4 indici GIN su first_name/last_name/email/city, espressione `lower(col)` verificata combaciare esattamente con la query JPQL. Gap noto (audit 2026-07-25): guest-service non ha Testcontainers, quindi V7 (unica migration con `CREATE EXTENSION`, operazione privilegiata) non è mai validata contro Postgres reale in CI |
@@ -159,7 +159,7 @@
 
 | Fase | Durata | Team | Output |
 |---|---|---|---|
-| Production-ready | ~~4-6 sett~~ **~1 sett residua** | 1 dev | ~~@Version ✅~~ ~~restart ✅~~ ~~runbook ✅~~ ~~GIN ✅~~ — resta: backup DB, alert rules, Dependabot |
+| Production-ready | ~~4-6 sett~~ **~1 sett residua** | 1 dev | ~~@Version ✅~~ ~~restart ✅~~ ~~runbook ✅~~ ~~GIN ✅~~ — restano: copia off-site/drill CI backup e altri gap operativi |
 | Quick wins commerciali | 2-3 mesi | 2 persone | Email, mobile, KPI avanzati, fattura legale (C2 sequenziale) |
 | Enterprise core | 3-6 mesi | 2-3 persone | Channel manager, booking engine, K8s |
 | Enterprise SaaS | 6-12 mesi | 3-4 persone | API pubblica, revenue mgmt, HA |

codex
Implementado A.SPEC AI-0121 únicamente en:

- [OPERATIONS_RUNBOOK.md](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0121-gyrdpb2l/docs/OPERATIONS_RUNBOOK.md)
- [FINAL_AUDIT_ULTRA_SEVERE.md](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0121-gyrdpb2l/docs/FINAL_AUDIT_ULTRA_SEVERE.md)
- [ROADMAP.md](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0121-gyrdpb2l/docs/ROADMAP.md)

Cambios: comando sin `--profile backup`, modelo pgBackRest actual documentado, afirmaciones obsoletas etiquetadas como históricas y pendientes off-site/restore CI explícitos.

Verificación: búsqueda contractual ejecutada y `git diff --check` sin errores. No se modificaron otros archivos ni se ejecutaron operaciones runtime.
diff --git a/docs/FINAL_AUDIT_ULTRA_SEVERE.md b/docs/FINAL_AUDIT_ULTRA_SEVERE.md
index 75d29577807acc53507f0038452ac6676ba591c2..d624fc4e6acfab8cc09702d0169161a02335351c
--- a/docs/FINAL_AUDIT_ULTRA_SEVERE.md
+++ b/docs/FINAL_AUDIT_ULTRA_SEVERE.md
@@ -35,7 +35,7 @@
    non sono ancora unificati: il lavoro GDPR retention + THREAT_MODEL + LaTeX report è
    inaccessibile dal branch corrente.
 
-Per produzione reale mancano inoltre Kubernetes, alerting rules, backup/restore DB,
+Per produzione reale mancavano inoltre Kubernetes, alerting rules, backup/restore DB,
 load testing, Testcontainers, e contratti API formali.
 
 ---
@@ -111,7 +111,7 @@
 | HMAC inter-service | ✅ Eccellente | ✅ Eccellente | ✅ Adeguato | Constant-time compare, segreto da script dedicato, startup check bloccante | Nessun mTLS; HMAC è unico layer di auth interna |
 | RBAC | ✅ Buono | ✅ Adeguato | ✅ Adeguato | Gateway route-level + `@PreAuthorize` su endpoint sensibili. Fix `52e869c` (2026-05-17): `@PreAuthorize("hasAnyRole('ADMIN','OWNER')")` su submit Alloggiati, JSON export, PUT hotel-settings. `GlobalExceptionHandler` → 403 su `AccessDeniedException`. | — |
 | GDPR / PII | ✅ Buono | ✅ Adeguato | ✅ Adeguato | Retention job, hard-anonymize, `gdprConsentDate` su `main` (branch unificato). | — |
-| Alerting & audit | ✅ Buono | ⚠️ Passivo | ❌ Mancante | X-Correlation-ID via MDC, Prometheus + Grafana + Loki presenti | Nessuna alert rule configurata |
+| Alerting & audit | ✅ Buono | ⚠️ Passivo | ❌ Mancante | X-Correlation-ID via MDC, Prometheus + Grafana + Loki presenti | **Stato storico:** nessuna alert rule configurata |
 
 **Rischio esame — branch non unificato:**  
 Il lavoro GDPR retention (T-GST-05), `THREAT_MODEL.md`, e il report LaTeX
@@ -156,9 +156,9 @@
 |---|---|---|---|---|---|
 | Docker-compose | ✅ Eccellente | ✅ Adeguato | ⚠️ Dev-only | Healthcheck, resource limits, 5 reti isolate | **Nessuna `restart:` policy** — grep conferma zero occorrenze |
 | Logging | ✅ Eccellente | ✅ Adeguato | ✅ Adeguato | AsyncAppender Loki (`queueSize=512`, `neverBlock=true`), X-Correlation-ID via MDC | — |
-| Monitoring stack | ✅ Buono | ✅ Adeguato | ⚠️ Passivo | Prometheus + Grafana + Loki + Zipkin presenti e configurati | Nessuna alert rule |
+| Monitoring stack | ✅ Buono | ✅ Adeguato | ⚠️ Passivo | Prometheus + Grafana + Loki + Zipkin presenti e configurati | **Stato storico:** nessuna alert rule |
 | Secrets | ✅ Buono | ✅ Adeguato | ⚠️ Env-var only | `.env` in `.gitignore`, Spring Cloud Config, nessun hardcoding | Nessun Vault/KMS |
-| Backup DB | ❌ Assente | ❌ Assente | ❌ Gap critico | Nessuna strategia pg_dump o WAL archiving | Data loss catastrofico su crash disco |
+| Backup DB | ❌ Assente | ❌ Assente | ❌ Gap critico | **Stato storico:** nessuna strategia pg_dump o WAL archiving | Data loss catastrofico su crash disco |
 | CI/CD | ⚠️ Parziale | ⚠️ Parziale | ❌ Gap critico | Dependabot configurato; nessun GitHub Actions workflow per build/test su PR | Nessuna automazione pipeline |
 | Kubernetes | ❌ Assente | ❌ Accettabile | ❌ Gap critico | Single docker-compose per tutto | Da costruire per scaling e failover |
 
@@ -245,7 +245,7 @@
 ~~**M3 — Nessun ErrorBoundary React a livello route**~~  
 ✅ **Risolto** (commit `fc3e86c`) — `ErrorBoundary.tsx` class component avvolge `<Suspense>` in `App.tsx`. Fallback UI con messaggio utente + pulsante reload. 4 Vitest test. Residuale: nessuna boundary granulare per singola route.
 
-**M4 — Nessuna alert rule Prometheus/Grafana**  
+**M4 — Nessuna alert rule Prometheus/Grafana (rilievo storico)**
 Stack osservabilità presente (Prometheus + Grafana + Loki), ma nessuna regola di alerting.
 Errori 5xx, latenza, riavvii container: invisibili finché un utente non segnala un problema.
 
@@ -396,8 +396,8 @@
 10. **GIN index + `pg_trgm`** su `Guest.firstName/lastName`; rimuovere LIKE query.
 11. **GitHub Actions CI/CD**: build + test + lint su ogni PR verso main.
 12. **Kubernetes manifests** o docker swarm per scaling e failover.
-13. **Backup PostgreSQL schedulato** (pg_dump cron o WAL archiving).
-14. **Prometheus alert rules**: error rate, latency p99, container restarts.
+13. **Backup PostgreSQL schedulato** (pg_dump cron o WAL archiving; rilievo storico).
+14. **Prometheus alert rules**: error rate, latency p99, container restarts (rilievo storico).
 15. **Secrets management** migrato a Vault o equivalente.
 16. **`CONTRIBUTING.md`** per onboarding di un nuovo sviluppatore.
 
diff --git a/docs/OPERATIONS_RUNBOOK.md b/docs/OPERATIONS_RUNBOOK.md
index f3f0412a262f63e22c2baa816301a199af76c4a7..e30a09604aef7945b046660bf2022d57de5ff3cd
--- a/docs/OPERATIONS_RUNBOOK.md
+++ b/docs/OPERATIONS_RUNBOOK.md
@@ -11,13 +11,14 @@
 ### Avvio completo
 
 ```bash
-# Stack core (11 servizi) — SENZA osservabilità/backup
+# Stack core (11 servizi) — senza osservabilità; pgBackRest locale è già
+# integrato nel servizio postgres
 docker compose up -d
 
-# Stack completo (raccomandato in produzione): aggiunge Loki/Grafana/Zipkin/
-# Alertmanager/Prometheus (profilo "observability") e il backup automatico
-# Postgres (profilo "backup") — entrambi i --profile sono opt-in, senza non partono
-docker compose --profile observability --profile backup up -d
+# Stack completo: aggiunge Loki/Grafana/Zipkin/Alertmanager/Prometheus
+# (profilo "observability"). Non esiste più un profilo "backup": il backup
+# locale pgBackRest è parte del servizio postgres sempre avviato.
+docker compose --profile observability up -d
 
 # Verifica che tutti i servizi siano healthy
 docker compose ps
@@ -258,7 +259,7 @@
 GitHub: `PGBACKREST_CIPHER_PASS`, `S3_ENDPOINT`, `S3_BUCKET`,
 `S3_ACCESS_KEY_ID`, `S3_SECRET_ACCESS_KEY`, `S3_REGION` (stessi valori di `.env`).
 
-**Stato**: ✅ **pgBackRest verificato dal vivo, 2026-08-01.** WAL archiving
+**Evidenza storica (2026-08-01)**: ✅ **pgBackRest verificato dal vivo.** WAL archiving
 confermato su `repo1` e `repo2` (`pg_switch_wal()` forzato, segmento
 comparso su entrambi entro pochi secondi). Backup full+incrementale
 confermati su entrambi i repository — **bug trovato e corretto nello stesso
@@ -276,6 +277,7 @@
 transazione esatta dell'insert. Verifica visiva sul bucket B2 reale
 (console `secure.backblaze.com`) richiede login con le credenziali
 dell'utente — da fare manualmente, non eseguibile da un agente automatico.
+Questa è evidenza runtime storica, non una nuova approvazione dell'ambiente attuale.
 Drill CI non ancora eseguito dal vivo — richiede prima la configurazione dei
 secret GitHub elencati sopra.
 
diff --git a/docs/ROADMAP.md b/docs/ROADMAP.md
index 83569d87c2e2a207910d58a4bfa092a61a460d3b..f843246fef34765af3d888969aa21ca13392c780
--- a/docs/ROADMAP.md
+++ b/docs/ROADMAP.md
@@ -16,7 +16,7 @@
 | Dimensione | Livello | Note |
 |---|---|---|
 | Sicurezza | 9.0/10 | Argon2id, HMAC anti-replay nonce+timestamp (E7bis ✅), RBAC doppio livello, GDPR, CodeQL extended (P13 ✅) — gap residui: 2FA (E9), rate limiting per-utente (E14) |
-| Affidabilità | 8.5/10 | Circuit breaker, saga checkIn, `@Version` Invoice, backup pg_dump 24h retain 14gg (P3 parz. — manca copia off-site) — no K8s HA (SPOF singolo host) |
+| Affidabilità | 8.5/10 | Circuit breaker, saga checkIn, `@Version` Invoice, pgBackRest con WAL archiving e backup full/incrementali (P3 parz. — copia off-site opt-in e restore CI pendenti) — no K8s HA (SPOF singolo host) |
 | Osservabilità | 8.5/10 | Zipkin + Prometheus + Loki + Alertmanager con 6 alert rule (P4 ✅) + Runbook (P5 ✅) — Zipkin → Tempo rimane C9 opzionale |
 | Scalabilità | 7.0/10 | 8 servizi, GIN pg_trgm, frontdesk consolidato (ADR-001 ✅), Dependabot (P9 ✅) — SimpleDiscovery statico, no K8s (E5) |
 | Qualità codice | 9.0/10 | PMD zero, Testcontainers billing+frontdesk (P7 ✅), coverage gate 90/80/88/92% (P15 ✅), Zod validation (P11 ✅), SRP refactor (P10 ✅) |
@@ -40,8 +40,8 @@
 |---|---|---|---|---|
 | P1 | ~~`@Version` su `Invoice` + migration Flyway~~ | ✅ **Fatto** | — | Implementato: `Invoice.java` campo `@Version Long version`, Flyway V3. Audit Sprint 1 (2026-07-25): trovato e corretto un gap reale — `billing-service` non aveva l'`@ExceptionHandler(ObjectOptimisticLockingFailureException.class)` (presente invece in frontdesk-service), quindi un conflitto di scrittura concorrente su fattura tornava HTTP 500 invece di 409. Aggiunto handler + test reale (2 letture della stessa riga, 1a save ok, 2a save stale → eccezione verificata su Testcontainers) + unit test sul mapping a 409 |
 | P2 | ~~`restart: unless-stopped` in docker-compose~~ | ✅ **Fatto** | — | Tutti i 17 container in `docker-compose.yml` già configurati (numero corretto 2026-07-25, era stale a 16 da prima di Alertmanager/notification-service) |
-| P0 | ~~Hardening porte Docker — compose dev/prod separati~~ | ✅ **Fatto** | — | `docker-compose.prod.yml` creato: usa il merge-tag `!reset` per azzerare `ports` su Postgres/Redis/Prometheus/Zipkin/Loki/Grafana/config-server/tutti i backend — solo frontend (:80) e api-gateway (:8080) restano pubblicati sull'host. `docker-compose.yml` invariato (dev). Verificato con `docker compose ... config`. Uso: `docker compose -f docker-compose.yml -f docker-compose.prod.yml --profile observability --profile backup up -d` (2026-07-17: loki/grafana/zipkin/alertmanager/prometheus/db-backup sono ora dietro profilo opt-in in `docker-compose.yml` per uno stack dev più leggero — i flag `--profile` sono obbligatori anche in prod, un override non riattiva un servizio gated). OWASP A05 |
-| P3 | ~~Backup PostgreSQL automatizzato (pg_dump cron)~~ | 🟡 **Parziale** | — | Container `db-backup` in `docker-compose.yml`: `pg_dumpall` ogni 24h (configurabile), gzip, retention 14gg, volume dedicato `postgres_backups`, nessuna porta host. Verificato con dump reale (240K, 46 CREATE TABLE/DATABASE). RPO 24h accettato esplicitamente, non PITR (`backup/DECISIONS.md §3.5`). Restano da fare: copia esterna cifrata automatica (S3/B2) — oggi il volume Docker resta single-host, single-disk, single point of failure fisico — e un test di restore end-to-end reale (piano di rifinitura Fase 6, item 4/6) |
+| P0 | ~~Hardening porte Docker — compose dev/prod separati~~ | ✅ **Fatto** | — | `docker-compose.prod.yml` creato: usa il merge-tag `!reset` per azzerare `ports` su Postgres/Redis/Prometheus/Zipkin/Loki/Grafana/config-server/tutti i backend — solo frontend (:80) e api-gateway (:8080) restano pubblicati sull'host. `docker-compose.yml` invariato (dev). Verificato con `docker compose ... config`. Uso: `docker compose -f docker-compose.yml -f docker-compose.prod.yml --profile observability up -d`; non esiste più un profilo `backup`, perché pgBackRest è integrato nel servizio `postgres`. OWASP A05 |
+| P3 | ~~Backup PostgreSQL automatizzato (pg_dump cron)~~ | 🟡 **Parziale** | — | **Modello storico:** `db-backup`/`pg_dumpall` ogni 24h, gzip e retention 14gg. Modello attuale nel checkout: pgBackRest integrato in `postgres`, WAL archiving e backup full/incrementali; repo2 off-site resta opt-in. Restano pendenti la copia off-site configurata e il drill CI di restore (nessuna approvazione operativa implicita dalla verifica statica) |
 | P4 | ~~Prometheus alert rules (error rate, latency, restarts)~~ | ✅ **Fatto** | — | `alert_rules.yml` con 6 regole (ServiceDown/HighErrorRate/HighLatencyP99/JvmHeapHigh/CircuitBreakerOpen/DbConnectionPoolNearExhaustion); Alertmanager v0.27 aggiunto a compose; histogrammi Micrometer abilitati via shared `config/application.yml` |
 | P5 | ~~Operations Runbook~~ | ✅ **Fatto** | — | `docs/OPERATIONS_RUNBOOK.md` creato con 10 procedure operative |
 | P6 | ~~GIN index + `pg_trgm` su `GuestRepository`~~ | ✅ **Fatto** | — | Flyway V7 `V7__add_trgm_search_indexes.sql`: 4 indici GIN su first_name/last_name/email/city, espressione `lower(col)` verificata combaciare esattamente con la query JPQL. Gap noto (audit 2026-07-25): guest-service non ha Testcontainers, quindi V7 (unica migration con `CREATE EXTENSION`, operazione privilegiata) non è mai validata contro Postgres reale in CI |
@@ -159,7 +159,7 @@
 
 | Fase | Durata | Team | Output |
 |---|---|---|---|
-| Production-ready | ~~4-6 sett~~ **~1 sett residua** | 1 dev | ~~@Version ✅~~ ~~restart ✅~~ ~~runbook ✅~~ ~~GIN ✅~~ — resta: backup DB, alert rules, Dependabot |
+| Production-ready | ~~4-6 sett~~ **~1 sett residua** | 1 dev | ~~@Version ✅~~ ~~restart ✅~~ ~~runbook ✅~~ ~~GIN ✅~~ — restano: copia off-site/drill CI backup e altri gap operativi |
 | Quick wins commerciali | 2-3 mesi | 2 persone | Email, mobile, KPI avanzati, fattura legale (C2 sequenziale) |
 | Enterprise core | 3-6 mesi | 2-3 persone | Channel manager, booking engine, K8s |
 | Enterprise SaaS | 6-12 mesi | 3-4 persone | API pubblica, revenue mgmt, HA |

tokens used
56,664
Implementado A.SPEC AI-0121 únicamente en:

- [OPERATIONS_RUNBOOK.md](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0121-gyrdpb2l/docs/OPERATIONS_RUNBOOK.md)
- [FINAL_AUDIT_ULTRA_SEVERE.md](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0121-gyrdpb2l/docs/FINAL_AUDIT_ULTRA_SEVERE.md)
- [ROADMAP.md](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0121-gyrdpb2l/docs/ROADMAP.md)

Cambios: comando sin `--profile backup`, modelo pgBackRest actual documentado, afirmaciones obsoletas etiquetadas como históricas y pendientes off-site/restore CI explícitos.

Verificación: búsqueda contractual ejecutada y `git diff --check` sin errores. No se modificaron otros archivos ni se ejecutaron operaciones runtime.

$ rg -n "profile backup|no alert rules|14 days|pg_dumpall|pgbackrest" docs/OPERATIONS_RUNBOOK.md docs/FINAL_AUDIT_ULTRA_SEVERE.md docs/ROADMAP.md
BLOCKED_BY_SUPERVISOR

```
