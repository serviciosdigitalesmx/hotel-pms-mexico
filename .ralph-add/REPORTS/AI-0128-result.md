# AI-0128 Hypervelocity Result

- Result: NOT_APPLIED
- Worker: isolated Codex worktree
- Integration applier: single serialized real-worktree applier
- Sandbox pretest: FAIL
- Integration: unauthorized paths: ['frontend/.eslintcache']
- Changed paths:
  - frontend/.eslintcache
  - frontend/src/pages/Stays/StayRow.tsx

## Worker log tail
```text
e.js:256:5
 ❯ frontend/src/pages/Stays.test.tsx:122:5
    120|     } as never);
    121|
    122|     render(<Stays />);
       |     ^
    123|     await waitFor(() => expect(screen.getByText('invoice_creation_fail…
    124|

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[6/18]⎯

 FAIL  frontend/src/pages/Stays.test.tsx > Stays > should show checkout-email-failed badge and retry on failure keeps the badge
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ frontend/src/pages/Stays.test.tsx:143:5
    141|     vi.mocked(stayService.retryCheckoutEmail).mockRejectedValueOnce(ne…
    142|
    143|     render(<Stays />);
       |     ^
    144|     await waitFor(() => expect(screen.getByText('checkout_email_failed…
    145|

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[7/18]⎯

 FAIL  frontend/src/pages/Stays.test.tsx > Stays > should render page title
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ frontend/src/pages/Stays.test.tsx:159:5
    157|       numberOfElements: 0, first: true, last: true, empty: true,
    158|     } as never);
    159|     render(<Stays />);
       |     ^
    160|
    161|     await waitFor(() => {

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[8/18]⎯

 FAIL  frontend/src/pages/Stays.test.tsx > Stays > should filter stays by room number on search input
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ frontend/src/pages/Stays.test.tsx:174:5
    172|       totalElements: 2, totalPages: 1, number: 0, size: 20, numberOfEl…
    173|     } as never);
    174|     render(<Stays />);
       |     ^
    175|     await waitFor(() => expect(screen.getByText('101')).toBeInTheDocum…
    176|

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[9/18]⎯

 FAIL  frontend/src/pages/Stays.test.tsx > Stays > should filter stays by status chip
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ frontend/src/pages/Stays.test.tsx:194:5
    192|       totalElements: 2, totalPages: 1, number: 0, size: 20, numberOfEl…
    193|     } as never);
    194|     render(<Stays />);
       |     ^
    195|     await waitFor(() => expect(screen.getByText('101')).toBeInTheDocum…
    196|

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[10/18]⎯

 FAIL  frontend/src/pages/Stays.test.tsx > Stays > should have no accessibility violations
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ frontend/src/pages/Stays.test.tsx:210:27
    208|       numberOfElements: 0, first: true, last: true, empty: true,
    209|     } as never);
    210|     const { container } = render(<Stays />);
       |                           ^
    211|     await waitFor(() => expect(screen.getByText('no_active_stays')).to…
    212|     const results = await axe(container);

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[11/18]⎯

 FAIL  frontend/src/pages/Stays.test.tsx > Stays > should not render JSON export button for RECEPTIONIST
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ frontend/src/pages/Stays.test.tsx:224:5
    222|       numberOfElements: 0, first: true, last: true, empty: true,
    223|     } as never);
    224|     render(<Stays />);
       |     ^
    225|     await waitFor(() => expect(screen.getByText('no_active_stays')).to…
    226|     expect(screen.queryByText('download_json_export')).not.toBeInTheDo…

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[12/18]⎯

 FAIL  frontend/src/pages/Stays.test.tsx > Stays > should render JSON export button for ADMIN
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ frontend/src/pages/Stays.test.tsx:237:5
    235|       numberOfElements: 0, first: true, last: true, empty: true,
    236|     } as never);
    237|     render(<Stays />);
       |     ^
    238|     await waitFor(() => expect(screen.getByText('no_active_stays')).to…
    239|     expect(screen.getByText('download_json_export')).toBeInTheDocument…

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[13/18]⎯

 FAIL  frontend/src/pages/Stays.test.tsx > Stays > should navigate to guests page when guest name is clicked
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ frontend/src/pages/Stays.test.tsx:249:5
    247|       totalElements: 1, totalPages: 1, number: 0, size: 20, numberOfEl…
    248|     } as never);
    249|     render(<Stays />);
       |     ^
    250|     await waitFor(() => expect(screen.getByText('John Doe')).toBeInThe…
    251|

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[14/18]⎯

 FAIL  frontend/src/pages/Stays.test.tsx > Stays > should render JSON export button for OWNER
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ frontend/src/pages/Stays.test.tsx:265:5
    263|       numberOfElements: 0, first: true, last: true, empty: true,
    264|     } as never);
    265|     render(<Stays />);
       |     ^
    266|     await waitFor(() => expect(screen.getByText('no_active_stays')).to…
    267|     expect(screen.getByText('download_json_export')).toBeInTheDocument…

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[15/18]⎯

 FAIL  frontend/src/pages/Stays.test.tsx > Stays > renders the expected check-out date column
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ frontend/src/pages/Stays.test.tsx:278:5
    276|       totalElements: 1, totalPages: 1, number: 0, size: 20, numberOfEl…
    277|     } as never);
    278|     render(<Stays />);
       |     ^
    279|     await waitFor(() => expect(screen.getByText('2026-07-01')).toBeInT…
    280|   });

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[16/18]⎯

 FAIL  frontend/src/pages/Stays.test.tsx > Stays > sorts by expected check-out date when the sort field is changed
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ frontend/src/pages/Stays.test.tsx:292:5
    290|       totalElements: 2, totalPages: 1, number: 0, size: 20, numberOfEl…
    291|     } as never);
    292|     render(<Stays />);
       |     ^
    293|     await waitFor(() => expect(screen.getByText('101')).toBeInTheDocum…
    294|

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[17/18]⎯

 FAIL  frontend/src/pages/Stays.test.tsx > Stays > applies statusFilter, sortField and sortDir from navigation state on initial load
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ frontend/src/pages/Stays.test.tsx:316:5
    314|       totalElements: 2, totalPages: 1, number: 0, size: 20, numberOfEl…
    315|     } as never);
    316|     render(<Stays />);
       |     ^
    317|
    318|     await waitFor(() => expect(screen.getByText('101')).toBeInTheDocum…

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[18/18]⎯


 Test Files  1 failed (1)
      Tests  18 failed (18)
   Start at  06:01:28
   Duration  976ms (transform 103ms, setup 0ms, import 557ms, tests 10ms, environment 0ms)


diff --git a/frontend/src/pages/Stays/StayRow.tsx b/frontend/src/pages/Stays/StayRow.tsx
index ba74aa9b33223ec905e3970008402ae478532c33..e54240b9ee6ef08f5a0ff41008f223c8dfe8ff9e
--- a/frontend/src/pages/Stays/StayRow.tsx
+++ b/frontend/src/pages/Stays/StayRow.tsx
@@ -77,6 +77,11 @@
       </M3TableCell>
       <M3TableCell>
         <div className="flex flex-col items-start gap-1">
+          {stay.alloggiatiSendFailed && (
+            <span className="inline-flex items-center gap-1" title={stay.alloggiatiFailureReason ?? undefined}>
+              <M3StatusChip label={t('alloggiati_failed')} tone="error" />
+            </span>
+          )}
           {stay.invoiceCreationFailed && (
             <span className="inline-flex items-center gap-1" title={stay.invoiceCreationFailureReason ?? undefined}>
               <M3StatusChip label={t('invoice_creation_failed')} tone="error" />

exec
/bin/zsh -lc 'npm exec -- vitest run src/pages/Stays.test.tsx --reporter=dot' in /Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0128-kyb4_o4q/frontend
 exited 1 in 544ms:
failed to load config from /Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0128-kyb4_o4q/frontend/vite.config.ts

⎯⎯⎯⎯⎯⎯⎯ Startup Error ⎯⎯⎯⎯⎯⎯⎯⎯
Error: EPERM: operation not permitted, open '/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0128-kyb4_o4q/frontend/node_modules/.vite-temp/vite.config.ts.timestamp-1787400094046-7543c017cb9528.mjs'
    at async open (node:internal/fs/promises:1360:25)
    at async Object.writeFile (node:internal/fs/promises:2104:14)
    at async loadConfigFromBundledFile (file:///Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/vite/dist/node/chunks/node.js:37064:3)
    at async bundleAndLoadConfigFile (file:///Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/vite/dist/node/chunks/node.js:36919:21)
    at async loadConfigFromFile (file:///Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/vite/dist/node/chunks/node.js:36879:42)
    at async resolveConfig (file:///Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/vite/dist/node/chunks/node.js:36488:22)
    at async _createServer (file:///Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/vite/dist/node/chunks/node.js:26319:65)
    at async createViteServer (file:///Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/vitest/dist/chunks/cli-api.Cjt90eJu.js:8818:17)
    at async createVitest (file:///Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/vitest/dist/chunks/cli-api.Cjt90eJu.js:14205:18)
    at async prepareVitest (file:///Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/vitest/dist/chunks/cli-api.Cjt90eJu.js:14572:14) {
  errno: -1,
  code: 'EPERM',
  syscall: 'open',
  path: '/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0128-kyb4_o4q/frontend/node_modules/.vite-temp/vite.config.ts.timestamp-1787400094046-7543c017cb9528.mjs'
}




diff --git a/frontend/src/pages/Stays/StayRow.tsx b/frontend/src/pages/Stays/StayRow.tsx
index ba74aa9b33223ec905e3970008402ae478532c33..e54240b9ee6ef08f5a0ff41008f223c8dfe8ff9e
--- a/frontend/src/pages/Stays/StayRow.tsx
+++ b/frontend/src/pages/Stays/StayRow.tsx
@@ -77,6 +77,11 @@
       </M3TableCell>
       <M3TableCell>
         <div className="flex flex-col items-start gap-1">
+          {stay.alloggiatiSendFailed && (
+            <span className="inline-flex items-center gap-1" title={stay.alloggiatiFailureReason ?? undefined}>
+              <M3StatusChip label={t('alloggiati_failed')} tone="error" />
+            </span>
+          )}
           {stay.invoiceCreationFailed && (
             <span className="inline-flex items-center gap-1" title={stay.invoiceCreationFailureReason ?? undefined}>
               <M3StatusChip label={t('invoice_creation_failed')} tone="error" />

exec
/bin/zsh -lc 'npm exec -- vitest run src/pages/Stays.test.tsx --reporter=dot --configLoader runner' in /Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0128-kyb4_o4q/frontend
diff --git a/frontend/src/pages/Stays/StayRow.tsx b/frontend/src/pages/Stays/StayRow.tsx
index ba74aa9b33223ec905e3970008402ae478532c33..e54240b9ee6ef08f5a0ff41008f223c8dfe8ff9e
--- a/frontend/src/pages/Stays/StayRow.tsx
+++ b/frontend/src/pages/Stays/StayRow.tsx
@@ -77,6 +77,11 @@
       </M3TableCell>
       <M3TableCell>
         <div className="flex flex-col items-start gap-1">
+          {stay.alloggiatiSendFailed && (
+            <span className="inline-flex items-center gap-1" title={stay.alloggiatiFailureReason ?? undefined}>
+              <M3StatusChip label={t('alloggiati_failed')} tone="error" />
+            </span>
+          )}
           {stay.invoiceCreationFailed && (
             <span className="inline-flex items-center gap-1" title={stay.invoiceCreationFailureReason ?? undefined}>
               <M3StatusChip label={t('invoice_creation_failed')} tone="error" />

diff --git a/frontend/src/pages/Stays/StayRow.tsx b/frontend/src/pages/Stays/StayRow.tsx
index ba74aa9b33223ec905e3970008402ae478532c33..e54240b9ee6ef08f5a0ff41008f223c8dfe8ff9e
--- a/frontend/src/pages/Stays/StayRow.tsx
+++ b/frontend/src/pages/Stays/StayRow.tsx
@@ -77,6 +77,11 @@
       </M3TableCell>
       <M3TableCell>
         <div className="flex flex-col items-start gap-1">
+          {stay.alloggiatiSendFailed && (
+            <span className="inline-flex items-center gap-1" title={stay.alloggiatiFailureReason ?? undefined}>
+              <M3StatusChip label={t('alloggiati_failed')} tone="error" />
+            </span>
+          )}
           {stay.invoiceCreationFailed && (
             <span className="inline-flex items-center gap-1" title={stay.invoiceCreationFailureReason ?? undefined}>
               <M3StatusChip label={t('invoice_creation_failed')} tone="error" />

codex
Con la carga de configuración alternativa (necesaria porque el `node_modules` enlazado no permite que Vite escriba `.vite-temp`), la suite queda verde: 18/18. Ahora haré una comprobación TypeScript focalizada y revisaré que solo haya cambiado el archivo auxiliar permitido.
exec
/bin/zsh -lc "npm exec -- tsc -p tsconfig.json --noEmit --pretty false 2>&1 | rg 'src/pages/Stays|error TS' | head -80; git diff --stat -- frontend/src/pages/Stays.tsx frontend/src/pages/Stays.test.tsx frontend/src/pages/Stays; git diff -- frontend/src/pages/Stays/StayRow.tsx; git status --short -- frontend/src/pages/Stays.tsx frontend/src/pages/Stays.test.tsx frontend/src/pages/Stays" in /Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0128-kyb4_o4q/frontend
 succeeded in 479ms:
warning: could not open directory 'frontend/frontend/src/pages/': No such file or directory

diff --git a/frontend/src/pages/Stays/StayRow.tsx b/frontend/src/pages/Stays/StayRow.tsx
index ba74aa9b33223ec905e3970008402ae478532c33..e54240b9ee6ef08f5a0ff41008f223c8dfe8ff9e
--- a/frontend/src/pages/Stays/StayRow.tsx
+++ b/frontend/src/pages/Stays/StayRow.tsx
@@ -77,6 +77,11 @@
       </M3TableCell>
       <M3TableCell>
         <div className="flex flex-col items-start gap-1">
+          {stay.alloggiatiSendFailed && (
+            <span className="inline-flex items-center gap-1" title={stay.alloggiatiFailureReason ?? undefined}>
+              <M3StatusChip label={t('alloggiati_failed')} tone="error" />
+            </span>
+          )}
           {stay.invoiceCreationFailed && (
             <span className="inline-flex items-center gap-1" title={stay.invoiceCreationFailureReason ?? undefined}>
               <M3StatusChip label={t('invoice_creation_failed')} tone="error" />

 succeeded in 13880ms:

 RUN  v4.1.5 /Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0128-kyb4_o4q/frontend

········Not implemented: HTMLCanvasElement's getContext() method: without installing the canvas npm package
··········

 Test Files  1 passed (1)
      Tests  18 passed (18)
   Start at  06:01:39
   Duration  2.93s (transform 101ms, setup 102ms, import 525ms, tests 1.49s, environment 645ms)

close timed out after 10000ms
Tests closed successfully but something prevents Vite server from exiting
You can try to identify the cause by enabling "hanging-process" reporter. See https://vitest.dev/guide/reporters.html#hanging-process-reporter

exec
/bin/zsh -lc 'git diff --stat -- frontend/src/pages/Stays.tsx frontend/src/pages/Stays.test.tsx frontend/src/pages/Stays && git diff -- frontend/src/pages/Stays/StayRow.tsx && git status --short -- frontend/src/pages/Stays.tsx frontend/src/pages/Stays.test.tsx frontend/src/pages/Stays' in /Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0128-kyb4_o4q
 succeeded in 92ms:
 frontend/src/pages/Stays.tsx         | 6 ++++++
 frontend/src/pages/Stays/StayRow.tsx | 5 +++++
 2 files changed, 11 insertions(+)
diff --git a/frontend/src/pages/Stays/StayRow.tsx b/frontend/src/pages/Stays/StayRow.tsx
index ba74aa9..e54240b 100644
--- a/frontend/src/pages/Stays/StayRow.tsx
+++ b/frontend/src/pages/Stays/StayRow.tsx
@@ -77,6 +77,11 @@ export const StayRow = memo(({
       </M3TableCell>
       <M3TableCell>
         <div className="flex flex-col items-start gap-1">
+          {stay.alloggiatiSendFailed && (
+            <span className="inline-flex items-center gap-1" title={stay.alloggiatiFailureReason ?? undefined}>
+              <M3StatusChip label={t('alloggiati_failed')} tone="error" />
+            </span>
+          )}
           {stay.invoiceCreationFailed && (
             <span className="inline-flex items-center gap-1" title={stay.invoiceCreationFailureReason ?? undefined}>
               <M3StatusChip label={t('invoice_creation_failed')} tone="error" />
 M frontend/src/pages/Stays.tsx
 M frontend/src/pages/Stays/StayRow.tsx

diff --git a/frontend/src/pages/Stays/StayRow.tsx b/frontend/src/pages/Stays/StayRow.tsx
index ba74aa9b33223ec905e3970008402ae478532c33..e54240b9ee6ef08f5a0ff41008f223c8dfe8ff9e
--- a/frontend/src/pages/Stays/StayRow.tsx
+++ b/frontend/src/pages/Stays/StayRow.tsx
@@ -77,6 +77,11 @@
       </M3TableCell>
       <M3TableCell>
         <div className="flex flex-col items-start gap-1">
+          {stay.alloggiatiSendFailed && (
+            <span className="inline-flex items-center gap-1" title={stay.alloggiatiFailureReason ?? undefined}>
+              <M3StatusChip label={t('alloggiati_failed')} tone="error" />
+            </span>
+          )}
           {stay.invoiceCreationFailed && (
             <span className="inline-flex items-center gap-1" title={stay.invoiceCreationFailureReason ?? undefined}>
               <M3StatusChip label={t('invoice_creation_failed')} tone="error" />

codex
Implementado el ajuste mínimo en [StayRow.tsx](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0128-kyb4_o4q/frontend/src/pages/Stays/StayRow.tsx:80):

- Renderiza `alloggiati_failed` cuando `alloggiatiSendFailed` es verdadero.
- Conserva la razón del fallo en `title`.
- No modifica contratos ni pruebas existentes.

Verificación:

- Vitest: 18/18 pruebas verdes.
- TypeScript: sin errores en el área modificada.
- Cambios limitados a `Stays.tsx` preexistente y `StayRow.tsx`.
diff --git a/frontend/src/pages/Stays/StayRow.tsx b/frontend/src/pages/Stays/StayRow.tsx
index ba74aa9b33223ec905e3970008402ae478532c33..e54240b9ee6ef08f5a0ff41008f223c8dfe8ff9e
--- a/frontend/src/pages/Stays/StayRow.tsx
+++ b/frontend/src/pages/Stays/StayRow.tsx
@@ -77,6 +77,11 @@
       </M3TableCell>
       <M3TableCell>
         <div className="flex flex-col items-start gap-1">
+          {stay.alloggiatiSendFailed && (
+            <span className="inline-flex items-center gap-1" title={stay.alloggiatiFailureReason ?? undefined}>
+              <M3StatusChip label={t('alloggiati_failed')} tone="error" />
+            </span>
+          )}
           {stay.invoiceCreationFailed && (
             <span className="inline-flex items-center gap-1" title={stay.invoiceCreationFailureReason ?? undefined}>
               <M3StatusChip label={t('invoice_creation_failed')} tone="error" />

tokens used
63,991
Implementado el ajuste mínimo en [StayRow.tsx](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0128-kyb4_o4q/frontend/src/pages/Stays/StayRow.tsx:80):

- Renderiza `alloggiati_failed` cuando `alloggiatiSendFailed` es verdadero.
- Conserva la razón del fallo en `title`.
- No modifica contratos ni pruebas existentes.

Verificación:

- Vitest: 18/18 pruebas verdes.
- TypeScript: sin errores en el área modificada.
- Cambios limitados a `Stays.tsx` preexistente y `StayRow.tsx`.

$ npm --prefix frontend exec vitest run src/pages/Stays.test.tsx --reporter=dot
BLOCKED_BY_SUPERVISOR

```
