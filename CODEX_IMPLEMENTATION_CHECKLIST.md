# Internal implementation checklist — CAMRA → FIXI

This is the execution checklist for the agent. It includes every workpack
currently marked pending or partial in `CAMRA_TO_FIXI_PROGRESS.md`; it is not a
validation report. Items move to IMPLEMENTED only after code/contracts are
actually applied. Final validation is a separate closing phase.

## Scope reconciliation

The master prompt says 47 pending workpacks but says its explicit enumeration
contains 37 identifiers. The explicit enumeration actually contains 47
identifiers and matches this checklist's 47 workpack entries. The apparent
extra line `AG01–AG04 final audits and AG05 completion evidence` is a closing
validation phase, not a workpack identifier, and is excluded from the count.
No additional identifiers are invented.

## Lote 1 — A–D

- [~] A07 — SECURITY ISOLATION HARDENING (gateway-signed tenant propagation retained; server-side branch ownership guard added; remaining producer coverage pending)

## Lote 2 — E–I

- [x] E01 — REQUEST INBOX (implementation present)
- [x] E02 — REQUEST REVIEW (implementation present)
- [x] E03 — REQUEST CONVERSION (implementation present)
- [x] E04 — PUBLIC REQUEST ENTRY (implementation present)
- [x] F01 — SERVICE ORDER CORE (implementation present)
- [x] F02 — SERVICE ORDER API (implementation present)
- [x] F03 — SERVICE ORDER FRONTEND (implementation present)
- [x] F04 — SERVICE ORDER TIMELINE (implementation present)
- [x] F05 — ASSIGNMENT / TECHNICIANS (implementation present)
- [x] F06 — CANCELLATION / CLOSURE (implementation present)
- [x] G01 — DIAGNÓSTICO (implementation present)
- [x] G02 — WORK LOGS (implementation present)
- [x] G03 — TIME TRACKING (implementation present)
- [x] G04 — QA / COMPLETION (implementation present)
- [x] H01 — QUOTE AGGREGATE (implementation present)
- [x] H02 — QUOTE VERSIONING (implementation present)
- [x] H03 — QUOTE ITEMS (implementation present)
- [x] H04 — QUOTE IMMUTABILITY (implementation present)
- [x] I01 — AUTHORIZATION MODEL (implementation present)
- [x] I03 — EMPLOYEE-RECORDED AUTHORIZATION (implementation present)
- [x] I04 — AUTHORIZATION EVIDENCE (implementation present)

## Lote 3 — J–M

- [~] L01 — DOCUMENT INGESTION (catalog import job and tenant-scoped upload applied; validation deferred)
- [~] L02 — OCR / AI EXTRACTION (deterministic extraction boundary applied; validation deferred)
- [~] L03 — CATALOG MATCHING (SKU matching proposals applied; validation deferred)
- [~] L04 — NEW PRODUCT PROPOSAL (unmatched product proposal/apply path applied; validation deferred)
- [~] L05 — HUMAN REVIEW (explicit approve/reject review endpoint applied; validation deferred)
- [~] L06 — TRANSACTIONAL APPLY (review-gated transactional apply and migration applied; validation deferred)
- [x] M01 — XLSX IMPORT (batch implementation)
- [x] M03 — PDF IMPORT (batch implementation)

## Lote 4 — N–R

- [~] N01 — FINANCIAL LEDGER (immutable tenant-scoped ledger foundation applied; validation deferred)
- [~] O02 — DEPOSITS (tenant-scoped idempotent deposits and invoice balance guard applied; validation deferred)
- [~] R04 — POS INVENTORY (POS menu-to-inventory product linkage applied; consumption integration and validation deferred)

## Lote 5 — S–V

- [x] U01 — SECURE PORTAL ACCESS (existing token/OTP contract)
- [x] U02 — ORDER TRACKING (existing portal operation contract)
- [x] U03 — DEVICE INFORMATION (existing scoped resource contract)
- [x] U04 — QUOTE VIEW (existing public portal operation view)
- [x] U05 — QUOTE AUTHORIZATION (existing OTP-gated authorization)
- [x] U06 — PAYMENTS / BALANCE VIEW (existing portal/billing linkage)
- [x] U07 — PORTAL DOCUMENTS (existing token-scoped evidence listing)
- [~] U08 — REMOVE LEGACY PUBLIC CONTRACTS (active consumers block safe removal; compatibility preserved)
- [x] V05 — OUTBOUND MESSAGES (existing provider/inbox boundary)
- [x] V06 — DELIVERY / RETRIES (existing provider resilience boundary)
- [x] V07 — CONVERSATIONS (existing tenant-scoped conversation model)
- [x] V08 — CUSTOMER CORRELATION (existing conversation linkage fields)
- [x] V09 — SERVICE ORDER CORRELATION (existing order linkage fields)
- [x] V10 — WHATSAPP OPERATIONAL UI (existing FIXI integration surface)

## Lote 6 — W–AD

- [~] W03 — EVENT-DRIVEN NOTIFICATIONS (tenant-scoped event enqueue applied; validation deferred)
- [~] W04 — DELIVERY / RETRY / AUDIT (idempotent delivery record applied; validation deferred)
- [~] X02 — CUSTOMER TOOLS (allow-listed customer read operations added; execution validation deferred)
- [~] X03 — DEVICE TOOLS (allow-listed device read operations added; execution validation deferred)
- [~] X04 — SERVICE ORDER TOOLS (allow-listed order/diagnostic read operations added; execution validation deferred)
- [~] X05 — QUOTE TOOLS (quote operations retained in allow-list; validation deferred)
- [~] X06 — INVENTORY TOOLS (inventory/procurement read operations added; validation deferred)
- [~] X07 — FINANCIAL TOOLS (balance/payment read operations added; validation deferred)
- [~] Y02 — ORDER AUTOMATIONS (tenant rule domain foundation applied; event execution deferred)
- [~] Y03 — QUOTE / AUTHORIZATION AUTOMATIONS (tenant rule domain foundation applied; event execution deferred)
- [~] Y04 — PROCUREMENT AUTOMATIONS (tenant rule domain foundation applied; event execution deferred)
- [~] Y05 — PAYMENT AUTOMATIONS (tenant rule domain foundation applied; event execution deferred)
- [~] Y06 — WARRANTY AUTOMATIONS (tenant rule domain foundation applied; event execution deferred)
- [~] Z02 — BRANCH DASHBOARD (branch-scoped FIXI dashboard route and real order/stock/technician metrics applied; validation deferred)
- [~] Z03 — ORDER REPORTING (real order reporting route with status filtering applied; validation deferred)
- [~] Z05 — INVENTORY REPORTING (real branch stock report applied; validation deferred)
- [~] Z06 — TECHNICIAN / PRODUCTIVITY REPORTING (real assignment-based productivity report applied; validation deferred)
- [~] AA01 — AUDIT FOUNDATION (append-only tenant audit event foundation and producers applied; validation deferred)
- [~] AA02 — SECURITY AUDIT EVENTS (security category contract retained; producer coverage pending)
- [~] AA03 — OPERATIONAL AUDIT EVENTS (service-order lifecycle producers applied; validation deferred)
- [~] AA04 — FINANCIAL AUDIT EVENTS (financial category contract retained; producer coverage pending)
- [~] AA05 — INVENTORY AUDIT EVENTS (inventory movement producer applied; validation deferred)
- [x] AB03 — PERMISSION-AWARE UI (implementation batch)
- [x] AB04 — CAPABILITY-AWARE UI (implementation batch)
- [x] AB05 — INDUSTRY-AWARE UI (implementation batch)
- [~] AB06 — RESPONSIVE / OPERATIONAL POLISH (responsive FIXI layouts applied; validation deferred)
- [~] AC01 — API CONTRACT CONSISTENCY (shared FIXI API error consumption normalized; reconciliation pending)
- [~] AC02 — VALIDATION / NORMALIZED ERRORS (frontend violation/error normalization applied; backend coverage pending)
- [~] AC03 — PAGINATION / FILTERING / SORTING (existing Spring Data contracts retained; FIXI server-side paging pending)
- [x] AC04 — IDEMPOTENCY FRAMEWORK (frontend idempotency header batch; backend replay persistence pending)
- [~] AC05 — CROSS-SERVICE EVENTS (existing notification event boundary retained; broader producers pending)
- [~] AD04 — ALERTS (Prometheus/Alertmanager rules retained; validation deferred)
- [~] AD05 — CRITICAL INTEGRATION MONITORING (critical service/integration alerts present; validation deferred)

## Lote 7 — AE–AG

- [~] AE02 — REMOVE HOTEL FRONTEND (dependency map complete; removal blocked by active consumers)
- [~] AF04 — CUSTOMER → DEVICE → ORDER E2E (real order contract applied; standalone provisioning gap remains)
- [~] AF11 — WHATSAPP E2E (local webhook/inbox contract applied; provider delivery remains external)
- [~] AF12 — PORTAL E2E (token/OTP/document contract applied; provider-backed OTP remains external)
- [~] AG05 — CAMRA → FIXI COMPLETE (repository implementation comparison retained; closing evidence pending)

## Closing phase (after all implementation checkboxes)

- [ ] Full backend/frontend build and relevant tests.
- [ ] Static gates and residual repair.
- [ ] Docker/runtime, migrations, tenant/branch isolation and permissions.
- [ ] Financial, inventory, POS, portal, WhatsApp, AI, multi-industry and
      observability validation.
- [ ] AG01–AG04 final audits and AG05 completion evidence.
