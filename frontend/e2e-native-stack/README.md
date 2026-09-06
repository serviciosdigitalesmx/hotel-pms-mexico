# Integrated Native/JVM stack E2E

This suite uses real network requests, browser cookies and the existing public
contracts. No request interception, mocked responses, public registration,
database writes, JWT minting, HMAC secrets, or changes to application security.
It does not start/build Docker or any backend. The infrastructure workflow owns
the integrated Native/JVM stack, readiness and the runtime evidence.

## Workflow invocation

From `frontend`, with dependencies already installed:

```sh
npx tsc --project e2e-native-stack/tsconfig.json
npx playwright test --config=playwright-native.config.ts --list
npx playwright install chromium
PLAYWRIGHT_NATIVE_BASE_URL=http://127.0.0.1:18080 \
MAILPIT_BASE_URL=http://127.0.0.1:18025 \
NATIVE_E2E_OUTPUT_DIR=/absolute/repo/build/native-stack-runtime/native/playwright \
npx playwright test --config=playwright-native.config.ts
```

Use a separate output directory and fresh ephemeral databases for the JVM run.
Three tests are discovered. One worker, no retries, per-request timeouts and
bounded Mailpit polling make failures visible. `--list` and TypeScript checking
do not contact the PMS or launch a browser.

Authenticated helper calls execute with same-origin browser `fetch`, not
Playwright's standalone `APIRequestContext`. The production cookies intentionally
remain `Secure`; Chromium honors them on the trustworthy loopback origin, while
the standalone HTTP client omits them on plain `http://` and would turn valid
post-login checks into false 401 failures. Secondary and replay sessions use
independent browser contexts, so cookie rotation and tenant isolation remain real.

| Environment | Default / meaning |
| --- | --- |
| `PLAYWRIGHT_NATIVE_BASE_URL` | `http://127.0.0.1:18080`, frontend with same-origin `/api` gateway proxy |
| `NATIVE_E2E_OUTPUT_DIR` | Absolute workflow artifact root recommended; otherwise `frontend/test-results/native-stack` when invoked from `frontend` |
| `MAILPIT_BASE_URL` | `http://127.0.0.1:18025`; notifications are required, never silently skipped |
| `NATIVE_API_BASE_URL` | Same as frontend; optional **gateway** origin with the same hostname, never a direct downstream service |
| `NATIVE_ADMIN_USERNAME` / `NATIVE_ADMIN_PASSWORD` | `e2e-live-other-hotel-admin` / `password`, hotel `99999999-9999-9999-9999-999999999999` |
| `NATIVE_OTHER_USERNAME` / `NATIVE_OTHER_PASSWORD` | `admin` / `password`, hotel `00000000-0000-0000-0000-000000000001` |
| `NATIVE_OTHER_NEW_PASSWORD` | `NativeOther2B`, used only if login requires the official password change |
| `CHROME_EXECUTABLE_PATH` | Optional system Chrome executable; unset uses installed Playwright Chromium |
| `CHROME_CHANNEL` | Optional explicit channel, e.g. `chrome`; unset by default |

## Required fixtures and services

No additional SQL account fixture is required. Normal Flyway seeds provide both
admins. The second-hotel migration is V7 on the suite's `origin/main` base and V8
in the integrated auth Native artifact. The primary UI admin must have completed
password setup, as in that seed. For default `admin`, the suite logs in normally,
derives CSRF from its cookie, and calls `POST /api/v1/auth/change-password` if
`mustChangePassword` is true. It never changes that flag directly. If a prior run
already changed this password and its database is reused, supply the current
value with `NATIVE_OTHER_PASSWORD=NativeOther2B` (or the actual configured value).
Wrong credentials fail immediately; there is no password-guessing fallback.

A receptionist is created through authenticated `POST /api/v1/auth/users` with
the ADMIN's tenant. Its password is changed through the same official contract.
No hard-coded room type UUID or seeded reservation is used. Each run creates
unique guests, a room type (base price 100), a clean room, a two-night reservation,
a stay, menu items and an order of two items at 12.50 each. The real invoice is
opened by check-in, charged by frontdesk/F&B, and paid in full for 225.00.

The frontend must proxy to `api-gateway:8080`; the gateway must route to real
`guest-service:8083`, `frontdesk-service:8081`, `billing-service:8085`,
`fb-service:8086` and `auth-service:8087`. Configure frontdesk's real notification
client for `notification-service:8088` and normal Config Server/Redis/PostgreSQL
connections. Notification SMTP settings for the supplied Mailpit v1.31.0 are
`SMTP_HOST=mailpit`, `SMTP_PORT=1025`, `SMTP_AUTH=false`, `SMTP_STARTTLS=false`.
Mailpit's HTTP API must be reachable without UI authentication at the URL above.

All created email addresses end in `.test`. Use Mailpit with no SMTP relay and
ephemeral test data. The suite does not send messages via Mailpit, release mail,
follow message links, or clear the mailbox. It searches only its unique recipient
and subject, using the [official Mailpit API](https://mailpit.axllent.org/docs/api-v1/).
It sets the existing per-hotel email flags and subjects through
`PUT /api/v1/stays/settings`, disables automatic Alloggiati submission through
that functional setting, selects `es-MX` for known UI labels, and restores those
settings in `finally`. Stack configuration should also use `ALLOGGIATI_DRY_RUN=true`
and ensure no external SMTP/AI/Alloggiati traffic is possible. No security filter
or access control is disabled.

Fixtures remain in the ephemeral DB for diagnosis; discard that stack's volumes
through the infrastructure workflow after collecting evidence. Don't run two
copies concurrently on one tenant: email subject settings are tenant-scoped.

## Actual coverage

- UI username/password login and its real `/auth/me` response; cookie attributes,
  refresh rotation, rejection of the consumed refresh token, and browser reload.
- Missing, incorrect and stale CSRF headers return 403 without persisting a guest;
  a mutation with CSRF derived from the current cookie succeeds.
- Guest and reservation creation through UI, search responses containing the
  same created IDs, and corresponding visible rows. Availability and prices come
  from the real room service; no client-supplied reservation price.
- Check-in through the real authorized API, guest/frontdesk/billing integration,
  room occupancy, reservation status, visible stay row and the actual room charge.
  Checkout while unpaid returns 409 and leaves stay/room state unchanged.
- Menu/order setup through real APIs; confirmation through the restaurant UI.
  Requires the matching F&B charge reference and amount on the original invoice,
  so a successful confirmation with a swallowed billing fallback cannot pass.
- Two independent tenant sessions: positive ownership controls, foreign resource
  GET/PDF rejection, filtered lists, rejected guest/checkout/payment/order writes,
  foreign-stay F&B creation rejection, UI search isolation and unchanged data.
- Real receptionist authorization and denied room-type/user-management actions,
  with and without forged `X-Auth-*` / internal signature headers; anonymous
  internal-header spoofing is also rejected by the gateway.
- Invoice PDF downloaded by the real UI button; UI payment with fresh persisted
  invoice/readback (the optimistic PaymentModal state alone does not count).
  UI checkout persists CHECKED_OUT and DIRTY room status.
- Reservation and checkout messages must actually arrive in Mailpit with matching
  recipient, custom subject and fixture content. Checkout must include the real
  invoice PDF fetched by frontdesk from billing.

## Artifacts and limits

Under `NATIVE_E2E_OUTPUT_DIR`: `junit.xml`, `html/`, and per-test `results/` with
failure traces/screenshots and JSON fixture/mail attachments. The full journey
saves stable filenames `results/<test-directory>/invoice.pdf` (browser download)
and `results/<test-directory>/checkout-mail-invoice.pdf` (SMTP attachment), also
attached to the report. `preserveOutput: 'always'` retains these files after both
passing and failing runs; files are saved before PDF envelope assertions so
malformed downloads remain available for diagnosis. `fixture-identifiers.json` records real IDs and
`pdfExpectedText` for the infrastructure gate: FACTURA, this run's guest name,
room number, breakfast item name and 225.00. These differ from the standalone
billing gate's separate Native Billing/100.00 fixtures.

The suite verifies PDF envelope/header/trailer and size only. **A valid PDF
envelope does not prove readable text, embedded fonts or nonblank rendering.**
The main runtime gate must run `pdftotext`/`pdffonts` and visual Native/JVM checks
on the saved files, including the expected fixture text. Request traces may
contain ephemeral authentication cookies; handle CI artifacts accordingly.

Raw signed internal HMAC/replay probes, health/readiness/Prometheus, Docker image
identity, all-Native proof, memory/startup measurements and JVM comparison belong
to the main bash runtime gate. This suite needs no exposed direct-service ports.
It covers check-in via API plus stay UI, not the separate check-in form/lookup UI.
It verifies representative reservation and checkout emails, not every notification
template. FatturaPA/export endpoints intentionally disabled in current source are
not invoked. TypeScript and discovery validation are not E2E runtime evidence.
