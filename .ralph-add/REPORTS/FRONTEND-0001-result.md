# FRONTEND-0001 Hypervelocity Result

- Result: VERIFY_FAIL
- Worker: isolated Codex worktree
- Integration applier: single serialized real-worktree applier
- Sandbox pretest: FAIL
- Integration: applied 2 paths; backup=/Users/usuario/.ralph-hotel/backups/FRONTEND-0001-20260822-081821
- Changed paths:
  - frontend/src/pages/HotelProfile.test.tsx
  - frontend/src/pages/HotelProfile.tsx

## Worker log tail
```text
;
     } finally {
       setSaving(false);
     }
@@ -275,7 +281,7 @@ export function HotelProfile() {
       </M3Card>
 
       <div className="flex justify-end">
-        <M3Button icon="save" onClick={handleSave} disabled={saving}>
+        <M3Button icon="save" onClick={() => void handleSave()} disabled={saving}>
           {saving ? t('btn_saving') : t('btn_save_profile')}
         </M3Button>
       </div>

diff --git a/frontend/src/pages/HotelProfile.test.tsx b/frontend/src/pages/HotelProfile.test.tsx
index 8f3067ec1c70eff9b6f1094109c65ba6d6d603a5..ab45244a3943ebc5e58c72ac72d6b19c64008994
--- a/frontend/src/pages/HotelProfile.test.tsx
+++ b/frontend/src/pages/HotelProfile.test.tsx
@@ -33,7 +33,7 @@
   alloggiatiAutoSend: false,
   hotelName: 'Hotel Test',
   address: 'Via Roma 1',
-  vatNumber: '12345678901',
+  vatNumber: 'ABC123456EF7',
   fiscalCode: 'ABCDEF12G34H567I',
   logoUrl: '',
   alloggiatiUsername: null,
diff --git a/frontend/src/pages/HotelProfile.tsx b/frontend/src/pages/HotelProfile.tsx
index 3925d01ae2b5cb382849544eaf7b0c1f968e9baf..291edc329b13fa50b0667df4a95d7cb3fc656aa7
--- a/frontend/src/pages/HotelProfile.tsx
+++ b/frontend/src/pages/HotelProfile.tsx
@@ -86,7 +86,7 @@
   const imgRef = useRef<HTMLImageElement>(null);
 
   const profileSchema = useMemo(() => z.object({
-    vatNumber: z.union([z.string().regex(RFC_REGEX, t('common:err_invalid_fiscal_code')), z.literal('')]),
+    vatNumber: z.union([z.string().regex(RFC_REGEX, t('common:err_invalid_vat')), z.literal('')]),
     logoUrl: z.union([z.string().url(t('common:err_invalid_url')), z.literal('')]),
   }), [t]);
   const handleLogoError = useCallback(() => {

codex
Implementado el A.SPEC:

- VAT inválido ahora muestra `common:err_invalid_vat`.
- Fixture de `HotelProfile` usa un RFC válido para aislar la prueba del toast backend.
- Se preservó la propagación literal de `response.data.detail`.
- `git diff --check` pasó.

La verificación Vitest quedó bloqueada por `EPERM` al escribir en `frontend/node_modules/.vite-temp`, enlazado fuera del worktree. No se modificó ningún archivo fuera de los paths permitidos.
diff --git a/frontend/src/pages/HotelProfile.test.tsx b/frontend/src/pages/HotelProfile.test.tsx
index 8f3067ec1c70eff9b6f1094109c65ba6d6d603a5..ab45244a3943ebc5e58c72ac72d6b19c64008994
--- a/frontend/src/pages/HotelProfile.test.tsx
+++ b/frontend/src/pages/HotelProfile.test.tsx
@@ -33,7 +33,7 @@
   alloggiatiAutoSend: false,
   hotelName: 'Hotel Test',
   address: 'Via Roma 1',
-  vatNumber: '12345678901',
+  vatNumber: 'ABC123456EF7',
   fiscalCode: 'ABCDEF12G34H567I',
   logoUrl: '',
   alloggiatiUsername: null,
diff --git a/frontend/src/pages/HotelProfile.tsx b/frontend/src/pages/HotelProfile.tsx
index 3925d01ae2b5cb382849544eaf7b0c1f968e9baf..291edc329b13fa50b0667df4a95d7cb3fc656aa7
--- a/frontend/src/pages/HotelProfile.tsx
+++ b/frontend/src/pages/HotelProfile.tsx
@@ -86,7 +86,7 @@
   const imgRef = useRef<HTMLImageElement>(null);
 
   const profileSchema = useMemo(() => z.object({
-    vatNumber: z.union([z.string().regex(RFC_REGEX, t('common:err_invalid_fiscal_code')), z.literal('')]),
+    vatNumber: z.union([z.string().regex(RFC_REGEX, t('common:err_invalid_vat')), z.literal('')]),
     logoUrl: z.union([z.string().url(t('common:err_invalid_url')), z.literal('')]),
   }), [t]);
   const handleLogoError = useCallback(() => {

tokens used
18,539
Implementado el A.SPEC:

- VAT inválido ahora muestra `common:err_invalid_vat`.
- Fixture de `HotelProfile` usa un RFC válido para aislar la prueba del toast backend.
- Se preservó la propagación literal de `response.data.detail`.
- `git diff --check` pasó.

La verificación Vitest quedó bloqueada por `EPERM` al escribir en `frontend/node_modules/.vite-temp`, enlazado fuera del worktree. No se modificó ningún archivo fuera de los paths permitidos.

$ npm --prefix frontend exec -- vitest run --root frontend src/pages/HotelProfile.test.tsx --pool=threads --maxWorkers=1 --reporter=dot
exit=1
brary/dom/dist/query-helpers.js:[2m95:19[22m[39m
[36m [2m❯[22m src/pages/HotelProfile.test.tsx:[2m203:34[22m[39m
    [90m201|[39m     await waitFor(() => expect(screen.getByText('hotel_profile_title')…
    [90m202|[39m
    [90m203|[39m     const passwordInput = screen.getByLabelText(/label_alloggiati_pass…
    [90m   |[39m                                  [31m^[39m
    [90m204|[39m     const wsKeyInput = screen.getByLabelText(/label_alloggiati_ws_key/…
    [90m205|[39m     const [showPasswordToggle, showWsKeyToggle] = screen.getAllByLabel…

[31m[2m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[9/10]⎯[22m[39m

[41m[1m FAIL [22m[49m src/pages/HotelProfile.test.tsx[2m > [22mHotelProfile[2m > [22msaves the entered username/password/WsKey and clears the secret fields afterwards
[31m[1mTestingLibraryElementError[22m[39m: Unable to find a label with the text of: /label_alloggiati_username/i

Ignored nodes: comments, script, style
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
              [33maria-invalid[39m=[32m"false"[39m
              [33mclass[39m=[32m"w-full rounded-md border border-outline bg-surface px-3 py-2 text-sm text-on-surface
            focus:outline-none focus:ring-2 focus:ring-primary"[39m
              [33mid[39m=[32m"profile-rfc"[39m
              [33mplaceholder[39m=[32m"placeholder_vat_number"[39m
              [33mtype[39m=[32m"text"[39m
              [33mvalue[39m=[32m"ABC123456EF7"[39m
            [36m/>[39m
          [36m</div>[39m
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
[90m [2m❯[22m Object.getElementError ../../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/dom/dist/config.js:[2m37:19[22m[39m
[90m [2m❯[22m getAllByLabelText ../../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/dom/dist/queries/label-text.js:[2m111:38[22m[39m
[90m [2m❯[22m ../../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/dom/dist/query-helpers.js:[2m52:17[22m[39m
[90m [2m❯[22m ../../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/dom/dist/query-helpers.js:[2m95:19[22m[39m
[36m [2m❯[22m src/pages/HotelProfile.test.tsx:[2m225:28[22m[39m
    [90m223|[39m
    [90m224|[39m     [35mconst[39m user [33m=[39m userEvent[33m.[39m[34msetup[39m()[33m;[39m
    [90m225|[39m     await user.type(screen.getByLabelText(/label_alloggiati_username/i…
    [90m   |[39m                            [31m^[39m
    [90m226|[39m     await user.type(screen.getByLabelText(/label_alloggiati_password/i…
    [90m227|[39m     await user.type(screen.getByLabelText(/label_alloggiati_ws_key/i),…

[31m[2m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[10/10]⎯[22m[39m


[2m Test Files [22m [1m[31m1 failed[39m[22m[90m (1)[39m
[2m      Tests [22m [1m[31m10 failed[39m[22m[2m | [22m[1m[32m8 passed[39m[22m[90m (18)[39m
[2m   Start at [22m 08:18:07
[2m   Duration [22m 2.57s[2m (transform 169ms, setup 103ms, import 680ms, tests 1.06s, environment 607ms)[22m

close timed out after 10000ms
Tests closed successfully but something prevents Vite server from exiting
You can try to identify the cause by enabling "hanging-process" reporter. See https://vitest.dev/guide/reporters.html#hanging-process-reporter


```
