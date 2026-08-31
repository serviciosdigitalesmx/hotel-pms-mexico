# FRONTEND-0000 Hypervelocity Result

- Result: VERIFY_FAIL
- Worker: isolated Codex worktree
- Integration applier: single serialized real-worktree applier
- Sandbox pretest: FAIL
- Integration: applied 1 paths; backup=/Users/usuario/.ralph-hotel/backups/FRONTEND-0000-20260822-081643
- Changed paths:
  - frontend/src/pages/HotelProfile.tsx

## Worker log tail
```text
rom /Users/usuario/.ralph-hotel/hypervelocity-worktrees/FRONTEND-0000-l2oijq_n/frontend/vite.config.ts

⎯⎯⎯⎯⎯⎯⎯ Startup Error ⎯⎯⎯⎯⎯⎯⎯⎯
Error: EPERM: operation not permitted, open '/Users/usuario/.ralph-hotel/hypervelocity-worktrees/FRONTEND-0000-l2oijq_n/frontend/node_modules/.vite-temp/vite.config.ts.timestamp-1787408176596-b5d376bb035ca8.mjs'
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
  path: '/Users/usuario/.ralph-hotel/hypervelocity-worktrees/FRONTEND-0000-l2oijq_n/frontend/node_modules/.vite-temp/vite.config.ts.timestamp-1787408176596-b5d376bb035ca8.mjs'
}




diff --git a/frontend/src/pages/HotelProfile.tsx b/frontend/src/pages/HotelProfile.tsx
index ffd71e9f960533612f339639cb2d9cc86d2fd900..3925d01ae2b5cb382849544eaf7b0c1f968e9baf
--- a/frontend/src/pages/HotelProfile.tsx
+++ b/frontend/src/pages/HotelProfile.tsx
@@ -281,7 +281,7 @@
       </M3Card>
 
       <div className="flex justify-end">
-        <M3Button icon="save" onClick={handleSave} disabled={saving}>
+        <M3Button icon="save" onClick={() => void handleSave()} disabled={saving}>
           {saving ? t('btn_saving') : t('btn_save_profile')}
         </M3Button>
       </div>

codex
Implementado dentro de la superficie autorizada:

- El botón ahora ejecuta explícitamente el callback async: `onClick={() => void handleSave()}`.
- Se preservaron servicio, validaciones, payload, toast de éxito y manejo de errores.
- No se tocaron endpoints, DTOs, migraciones ni archivos fuera de alcance.

Verificación bloqueada por `EPERM` al escribir `frontend/node_modules/.vite-temp/vite.config.ts...` durante Vitest.
diff --git a/frontend/src/pages/HotelProfile.tsx b/frontend/src/pages/HotelProfile.tsx
index ffd71e9f960533612f339639cb2d9cc86d2fd900..3925d01ae2b5cb382849544eaf7b0c1f968e9baf
--- a/frontend/src/pages/HotelProfile.tsx
+++ b/frontend/src/pages/HotelProfile.tsx
@@ -281,7 +281,7 @@
       </M3Card>
 
       <div className="flex justify-end">
-        <M3Button icon="save" onClick={handleSave} disabled={saving}>
+        <M3Button icon="save" onClick={() => void handleSave()} disabled={saving}>
           {saving ? t('btn_saving') : t('btn_save_profile')}
         </M3Button>
       </div>

tokens used
52,632
Implementado dentro de la superficie autorizada:

- El botón ahora ejecuta explícitamente el callback async: `onClick={() => void handleSave()}`.
- Se preservaron servicio, validaciones, payload, toast de éxito y manejo de errores.
- No se tocaron endpoints, DTOs, migraciones ni archivos fuera de alcance.

Verificación bloqueada por `EPERM` al escribir `frontend/node_modules/.vite-temp/vite.config.ts...` durante Vitest.

$ npm --prefix frontend exec -- vitest run --root frontend src/pages/HotelProfile.test.tsx --pool=threads --maxWorkers=1 --reporter=dot
exit=1
r[22m[39m: expected "vi.fn()" to be called with arguments: [ 'INVALID_VAT_NUMBER', 'error' ][90m

Number of calls: [1m0[22m
[39m

Ignored nodes: comments, script, style
[36m<html>[39m
  [36m<head />[39m
  [36m<body>[39m
    [36m<div>[39m
      [36m<main[39m
        [33maria-labelledby[39m=[32m"hotel-profile-title"[39m
        [33mclass[39m=[32m"max-w-xl mx-auto p-6 space-y-6"[39m
      [36m>[39m
        [36m<div>[39m
          [36m<h1[39m
            [33mclass[39m=[32m"text-2xl font-semibold text-on-surface flex items-center gap-2"[39m
            [33mid[39m=[32m"hotel-profile-title"[39m
          [36m>[39m
            [36m<span[39m
              [33maria-hidden[39m=[32m"true"[39m
              [33mclass[39m=[32m"material-symbols-outlined text-primary"[39m
            [36m>[39m
              [0mapartment[0m
            [36m</span>[39m
            [0mhotel_profile_title[0m
          [36m</h1>[39m
          [36m<p[39m
            [33mclass[39m=[32m"text-sm text-on-surface-variant mt-1"[39m
          [36m>[39m
            [0mhotel_profile_subtitle[0m
          [36m</p>[39m
        [36m</div>[39m
        [36m<div[39m
          [33mclass[39m=[32m"bg-surface shadow-elevation-1 rounded-shape-md p-6 space-y-4"[39m
        [36m>[39m
          [36m<div>[39m
            [36m<label[39m
              [33mclass[39m=[32m"block text-sm font-medium text-on-surface mb-1"[39m
              [33mfor[39m=[32m"profile-hotel-name"[39m
            [36m>[39m
              [0mlabel_hotel_name[0m
            [36m</label>[39m
            [36m<div[39m
              [33mclass[39m=[32m"relative"[39m
            [36m>[39m
              [36m<input[39m
                [33maria-invalid[39m=[32m"false"[39m
                [33mclass[39m=[32m"w-full rounded-md border border-outline bg-surface px-3 py-2 text-sm text-on-surface
            focus:outline-none focus:ring-2 focus:ring-primary"[39m
                [33mid[39m=[32m"profile-hotel-name"[39m
                [33mplaceholder[39m=[32m"placeholder_hotel_name"[39m
                [33mtype[39m=[32m"text"[39m
                [33mvalue[39m=[32m"Hotel Test"[39m
              [36m/>[39m
            [36m</div>[39m
          [36m</div>[39m
          [36m<div>[39m
            [36m<label[39m
              [33mclass[39m=[32m"block text-sm font-medium text-on-surface mb-1"[39m
              [33mfor[39m=[32m"profile-address"[39m
            [36m>[39m
              [0mlabel_hotel_address[0m
            [36m</label>[39m
            [36m<div[39m
              [33mclass[39m=[32m"relative"[39m
            [36m>[39m
              [36m<input[39m
                [33maria-invalid[39m=[32m"false"[39m
                [33mclass[39m=[32m"w-full rounded-md border border-outline bg-surface px-3 py-2 text-sm text-on-surface
            focus:outline-none focus:ring-2 focus:ring-primary"[39m
                [33mid[39m=[32m"profile-address"[39m
                [33mplaceholder[39m=[32m"placeholder_address"[39m
                [33mtype[39m=[32m"text"[39m
                [33mvalue[39m=[32m"Via Roma 1"[39m
              [36m/>[39m
            [36m</div>[39m
          [36m</div>[39m
          [36m<div[39m
            [33mclass[39m=[32m"grid grid-cols-2 gap-4"[39m
          [36m>[39m
            [36m<div>[39m
              [36m<label[39m
                [33mclass[39m=[32m"block text-sm font-medium text-on-surface mb-1"[39m
                [33mfor[39m=[32m"profile-city"[39m
              [36m>[39m
                [0mlabel_city[0m
              [36m</label>[39m
              [36m<div[39m
                [33mclass[39m=[32m"relative"[39m
              [36m>[39m
                [36m<input[39m
                  [33maria-invalid[39m=[32m"false"[39m
                  [33mclass[39m=[32m"w-full rounded-md border border-outline bg-surface px-3 py-2 text-sm text-on-surface
            focus:outline-none focus:ring-2 focus:ring-primary"[39m
                  [33mid[39m=[32m"profile-city"[39m
                  [33mtype[39m=[32m"text"[39m
                  [33mvalue[39m=[32m""[39m
                [36m/>[39m
              [36m</div>[39m
            [36m</div>[39m
            [36m<div>[39m
              [36m<label[39m
                [33mclass[39m=[32m"block text-sm font-medium text-on-surface mb-1"[39m
                [33mfor[39m=[32m"profile-state"[39m
              [36m>[39m
                [0mlabel_state[0m
              [36m</label>[39m
              [36m<div[39m
                [33mclass[39m=[32m"relative"[39m
              [36m>[39m
                [36m<input[39m
                  [33maria-invalid[39m=[32m"false"[39m
                  [33mclass[39m=[32m"w-full rounded-md border border-outline bg-surface px-3 py-2 text-sm text-on-surface
            focus:outline-none focus:ring-2 focus:ring-primary"[39m
                  [33mid[39m=[32m"profile-state"[39m
                  [33mtype[39m=[32m"text"[39m
                  [33mvalue[39m=[32m""[39m
                [36m/>[39m
              [36m</div>[39m
            [36m</div>[39m
            [36m<div>[39m
              [36m<label[39m
                [33mclass[39m=[32m"block text-sm font-medium text-on-surface mb-1"[39m
                [33mfor[39m=[32m"profile-postal-code"[39m
              [36m>[39m
                [0mlabel_postal_code[0m
              [36m</label>[39m
              [36m<div[39m
                [33mclass[39m=[32m"relative"[39m
              [36m>[39m
                [36m<input[39m
                  [33maria-invalid[39m=[32m"false"[39m
                  [33mclass[39m=[32m"w-full rounded-md border border-outline bg-surface px-3 py-2 text-sm text-on-surface
            focus:outline-none focus:ring-2 focus:ring-primary"[39m
                  [33mid[39m=[32m"profile-postal-code"[39m
                  [33mtype[39m=[32m"text"[39m
                  [33mvalue[39m=[32m""[39m
                [36m/>[39m
              [36m</div>[39m
            [36m</div>[39m
            [36m<div>[39m
              [36m<label[39m
                [33mclass[39m=[32m"block text-sm font-medium text-on-surface mb-1"[39m
                [33mfor[39m=[32m"profile-country"[39m
              [36m>[39m
                [0mlabel_country[0m
              [36m</label>[39m
              [36m<div[39m
                [33mclass[39m=[32m"relative"[39m
              [36m>[39m
                [36m<input[39m
                  [33maria-invalid[39m=[32m"false"[39m
                  [33mclass[39m=[32m"w-full rounded-md border border-outline bg-surface px-3 py-2 text-sm text-on-surface
            focus:outline-none focus:ring-2 focus:ring-primary"[39m
                  [33mid[39m=[32m"profile-country"[39m
                  [33mtype[39m=[32m"text"[39m
                  [33mvalue[39m=[32m"México"[39m
                [36m/>[39m
              [36m</div>[39m
            [36m</div>[39m
          [36m</div>[39m
          [36m<div>[39m
            [36m<label[39m
              [33mclass[39m=[32m"block text-sm font-medium text-on-surface mb-1"[39m
              [33mfor[39m=[32m"profile-rfc"[39m
            [36m>[39m
              [0mlabel_vat_number[0m
            [36m</label>[39m
            [36m<div[39m
              [33mclass[39m=[32m"relative"[39m
            [36m>[39m
              [36m<input[39m
                [33maria-describedby[39m=[32m"profile-rfc-error"[39m
                [33maria-invalid[39m=[32m"true"[39m
                [33mclass[39m=[32m"w-full rounded-md border border-outline bg-surface px-3 py-2 text-sm text-on-surface
            focus:outline-none focus:ring-2 focus:ring-primary"[39m
                [33mid[39m=[32m"profile-rfc"[39m
                [33mplaceholder[39m=[32m"placeholder_vat_number"[39m
                [33mtype[39m=[32m"text"[39m
                [33mvalue[39m=[32m"12345678901"[39m
              [36m/>[39m
            [36m</div>[39m
            [36m<p[39m
              [33mclass[39m=[32m"mt-1 text-sm text-error"[39m
              [33mid[39m=[32m"profile-rfc-error"[39m
              [33mrole[39m=[32m"alert"[39m
            [36m>[39m
              [0mcommon:err_invalid_fiscal_code[0m
            [36m</p>[39m
          [36m</div>[39m
          [36m<div[39m
            [33mclass[39m=[32m"grid grid-cols-2 gap-4"[39m
          [36m>[39m
            [36m<div>[39m
              [36m<label[39m
                [33mclass[39m=[32m"block text-sm font-medium text-on-surface mb-1"[39m
                [33mfor[39m=[32m"profile-currency"[39m
              [36m>[39m
                [0mlabel_currency[0m
              [36m</label>[39m
              [36m<div[39m
                [33mclass[39m=[32m"relative"[39m
              [36m>[39m
                [36m<input[39m
                  [33maria-invalid[39m=[32m"false"[39m
                  [33mclass[39m=[32m"w-full rounded-md border border-outline bg-surface px-3 py-2 text-sm text-on-surface
            focus:outline-none focus:ring-2 focus:ring-primary"[39m
                  [33mid[39m=[32m"profile-currency"[39m
                  [33mtype[39m=[32m"text"[39m
                  [33mvalue[39m=[32m"MXN"[39m
                [36m/>[39m
              [36m</div>[39m
            [36m</div>[39m
            [36m<div>[39m
              [36m<label[39m
                [33mclass[39m=[32m"block text-sm font-medium text-on-surface mb-1"[39m
                [33mfor[39m=[32m"profile-locale"[39m
              [36m>[39m
                [0mlabel_locale[0m
              [36m</label>[39m
              [36m<div[39m
                [33mclass[39m=[32m"relative"[39m
              [36m>[39m
                [36m<input[39m
                  [33maria-invalid[39m=[32m"false"[39m
                  [33mclass[39m=[32m"w-full rounded-md border border-outline bg-surface px-3 py-2 text-sm text-on-surface
            focus:outline-none focus:ring-2 focus:ring-primary"[39m
                  [33mid[39m=[32m"profile-locale"[39m
                  [33mtype[39m=[32m"text"[39m
                  [33mvalue[39m=[32m"es-MX"[39m
                [36m/>[39m
              [36m</div>[39m
            [36m</div>[39m
            [36m<div>[39m
              [36m<label[39m
                [33mclass[39m=[32m"block text-sm font-medium text-on-surface mb-1"[39m
                [33mfor[39m=[32m"profile-timezone"[39m
              [36m>[39m
                [0mlabel_timezone[0m
              [36m</label>[39m
              [36m<div[39m
                [33mclass[39m=[32m"relative"[39m
              [36m>[39m
                [36m<input[39m
                  [33maria-invalid[39m=[32m"false"[39m
                  [33mclass[39m=[32m"w-full rounded-md border border-outline bg-surface px-3 py-2 text-sm text-on-surface
            focus:outline-none focus:ring-2 focus:ring-primary"[39m
                  [33mid[39m=[32m"profile-timezone"[39m
                  [33mtype[39m=[32m"text"[39m
                  [33mvalue[39m=[32m"America/Monterrey"[39m
                [36m/>[39m
              [36m</div>[39m
            [36m</div>[39m
            [36m<div>[39m
              [36m<label[39m
                [33mclass[39m=[32m"block text-sm font-medium text-on-surface mb-1"[39m
                [33mfor[39m=[32m"profile-public-slug"[39m
              [36m>[39m
                [0mlabel_public_slug[0m
              [36m</label>[39m
              [36m<div[39m
                [33mclass[39m=[32m"relative"[39m
              [36m>[39m
                [36m<input[39m
                  [33maria-invalid[39m=[32m"false"[39m
                  [33mclass[39m=[32m"w-full rounded-md border border-outline bg-surface px-3 py-2 text-sm text-on-surface
            focus:outline-none focus:ring-2 focus:ring-primary"[39m
                  [33mid[39m=[32m"profile-public-slug"[39m
                  [33mtype[39m=[32m"text"[39m
                  [33mvalue[39m=[32m""[39m
                [36m/>[39m
              [36m</div>[39m
            [36m</div>[39m
          [36m</div>[39m
          [36m<div>[39m
            [36m<label[39m
              [33mclass[39m=[32m"block text-sm font-medium text-on-surface mb-1"[39m
              [33mfor[39m=[32m"profile-logo"[39m
            [36m>[39m
              [0mlabel_logo_url[0m
            [36m</label>[39m
            [36m<div[39m
              [33mclass[39m=[32m"relative"[39m
            [36m>[39m
              [36m<input[39m
                [33maria-invalid[39m=[32m"false"[39m
                [33mclass[39m=[32m"w-full rounded-md border border-outline bg-surface px-3 py-2 text-sm text-on-surface
            focus:outline-none focus:ring-2 focus:ring-primary"[39m
                [33mid[39m=[32m"profile-logo"[39m
                [33mplaceholder[39m=[32m"placeholder_logo_url"[39m
                [33mtype[39m=[32m"url"[39m
                [33mvalue[39m=[32m""[39m
              [36m/>[39m
            [36m</div>[39m
          [36m</div>[39m
        [36m</div>[39m
        [36m<div[39m
          [33mclass[39m=[32m"flex justify-end"[39m
        [36m>[39m
          [36m<button[39m
            [33mclass[39m=[32m"inline-flex items-center justify-center gap-2 px-6 h-10 rounded-shape-full text-sm font-medium font-body transition-all
        bg-primary text-on-primary hover:shadow-elevation-1 active:shadow-elevation-0 focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2
        
        "[39m
          [36m>[39m
            [36m<span[39m
              [33maria-hidden[39m=[32m"true"[39m
              [33mclass[39m=[32m"material-symbols-outlined"[39m
              [33mstyle[39m=[32m"font-size: 18px; width: 18px; height: 18px;"[39m
            [36m>[39m
              [0msave[0m
            [36m</span>[39m
            [0mbtn_save_profile[0m
          [36m</button>[39m
        [36m</div>[39m
      [36m</main>[39m
    [36m</div>[39m
  [36m</body>[39m
[36m</html>[39m
[36m [2m❯[22m src/pages/HotelProfile.test.tsx:[2m264:46[22m[39m
    [90m262|[39m
    [90m263|[39m     fireEvent.click(screen.getByRole('button', { name: /btn_save_profi…
    [90m264|[39m     await waitFor(() => expect(mockAddToast).toHaveBeenCalledWith('INV…
    [90m   |[39m                                              [31m^[39m
    [90m265|[39m   })[33m;[39m
    [90m266|[39m
[90m [2m❯[22m runWithExpensiveErrorDiagnosticsDisabled ../../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/dom/dist/config.js:[2m47:12[22m[39m
[90m [2m❯[22m checkCallback ../../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/dom/dist/wait-for.js:[2m124:77[22m[39m
[90m [2m❯[22m Timeout.checkRealTimersCallback ../../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/dom/dist/wait-for.js:[2m118:16[22m[39m

[31m[2m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[15/15]⎯[22m[39m


[2m Test Files [22m [1m[31m1 failed[39m[22m[90m (1)[39m
[2m      Tests [22m [1m[31m15 failed[39m[22m[2m | [22m[1m[32m3 passed[39m[22m[90m (18)[39m
[2m   Start at [22m 08:16:23
[2m   Duration [22m 7.95s[2m (transform 177ms, setup 116ms, import 724ms, tests 6.32s, environment 667ms)[22m

close timed out after 10000ms
Tests closed successfully but something prevents Vite server from exiting
You can try to identify the cause by enabling "hanging-process" reporter. See https://vitest.dev/guide/reporters.html#hanging-process-reporter


```
