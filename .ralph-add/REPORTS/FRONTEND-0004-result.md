# FRONTEND-0004 Hypervelocity Result

- Result: VERIFY_FAIL
- Worker: isolated Codex worktree
- Integration applier: single serialized real-worktree applier
- Sandbox pretest: FAIL
- Integration: applied 1 paths; backup=/Users/usuario/.ralph-hotel/backups/FRONTEND-0004-20260822-082353
- Changed paths:
  - frontend/src/pages/HotelProfile.tsx

## Worker log tail
```text
="alloggiati-credentials-title" className="text-lg font-medium text-on-surface">
+            {t('section_title_alloggiati_credentials')}
+          </h2>
+          <p className="text-sm text-on-surface-variant">{t('hint_alloggiati_credentials')}</p>
+          <label className="flex items-start gap-3 text-sm text-on-surface">
+            <input
+              type="checkbox"
+              checked={Boolean(form.alloggiatiAutoSend)}
+              onChange={handleCheckboxChange('alloggiatiAutoSend')}
+              aria-label={t('label_alloggiati_auto_send')}
+              className="mt-1"
+            />
+            <span>
+              <span className="block font-medium">{t('label_alloggiati_auto_send')}</span>
+              <span className="block text-on-surface-variant">{t('hint_alloggiati_auto_send')}</span>
+            </span>
+          </label>
+          <ProfileField
+            id="profile-alloggiati-username"
+            label={t('label_alloggiati_username')}
+            value={form.alloggiatiUsername ?? ''}
+            placeholder={t('placeholder_alloggiati_username')}
+            onChange={handleChange('alloggiatiUsername')}
+            autoComplete="username"
+          />
+          <div className="relative">
+            <ProfileField
+              id="profile-alloggiati-password"
+              label={t('label_alloggiati_password')}
+              value={form.alloggiatiPassword ?? ''}
+              placeholder={t(alloggiatiCredentialsConfigured ? 'placeholder_alloggiati_credential_configured' : 'placeholder_alloggiati_credential_unconfigured')}
+              onChange={handleChange('alloggiatiPassword')}
+              type={showAlloggiatiPassword ? 'text' : 'password'}
+              autoComplete="new-password"
+            />
+            <button type="button" aria-label="show_password" onClick={() => setShowAlloggiatiPassword((visible) => !visible)} className="absolute right-2 top-7">
+              <MaterialIcon name={showAlloggiatiPassword ? 'visibility_off' : 'visibility'} size={20} />
+            </button>
+          </div>
+          <div className="relative">
+            <ProfileField
+              id="profile-alloggiati-ws-key"
+              label={t('label_alloggiati_ws_key')}
+              value={form.alloggiatiWsKey ?? ''}
+              placeholder={t(alloggiatiCredentialsConfigured ? 'placeholder_alloggiati_credential_configured' : 'placeholder_alloggiati_credential_unconfigured')}
+              onChange={handleChange('alloggiatiWsKey')}
+              type={showAlloggiatiWsKey ? 'text' : 'password'}
+              autoComplete="new-password"
+            />
+            <button type="button" aria-label="show_password" onClick={() => setShowAlloggiatiWsKey((visible) => !visible)} className="absolute right-2 top-7">
+              <MaterialIcon name={showAlloggiatiWsKey ? 'visibility_off' : 'visibility'} size={20} />
+            </button>
+          </div>
+          <p className="text-sm text-on-surface-variant">
+            {t(alloggiatiCredentialsConfigured ? 'status_alloggiati_credentials_configured' : 'status_alloggiati_credentials_not_configured')}
+          </p>
+        </section>
       </M3Card>
 
       <div className="flex justify-end">

tokens used
31,598
Implementado únicamente en [HotelProfile.tsx](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/FRONTEND-0004-swyyskj6/frontend/src/pages/HotelProfile.tsx):

- Campos Alloggiati controlados.
- Password/WS key vacíos y con visibilidad independiente.
- Estado de credenciales configuradas.
- Carga y persistencia mediante `updateHotelSettings`.
- Limpieza de secretos tras guardar.
- Preservación de `alloggiatiAutoSend`.

`git diff --check` pasó. Vitest no pudo iniciar por permisos EPERM en `frontend/node_modules/.vite-temp`; no se modificaron tests ni otros archivos.

$ npm --prefix frontend exec -- vitest run --root frontend src/pages/HotelProfile.test.tsx --pool=threads --maxWorkers=1 --reporter=dot
exit=1

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
 ...
[90m [2m❯[22m Object.getElementError ../../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/dom/dist/config.js:[2m37:19[22m[39m
[90m [2m❯[22m getAllByLabelText ../../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/dom/dist/queries/label-text.js:[2m111:38[22m[39m
[90m [2m❯[22m ../../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/dom/dist/query-helpers.js:[2m52:17[22m[39m
[90m [2m❯[22m ../../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/dom/dist/query-helpers.js:[2m95:19[22m[39m
[36m [2m❯[22m src/pages/HotelProfile.test.tsx:[2m69:19[22m[39m
    [90m 67|[39m     expect(screen.getByLabelText(/label_hotel_address/i)).toBeInTheDoc…
    [90m 68|[39m     expect(screen.getByLabelText(/label_vat_number/i)).toBeInTheDocume…
    [90m 69|[39m     expect(screen.getByLabelText(/label_fiscal_code/i)).toBeInTheDocum…
    [90m   |[39m                   [31m^[39m
    [90m 70|[39m     expect(screen.getByLabelText(/label_logo_url/i)).toBeInTheDocument…
    [90m 71|[39m     expect(screen.getByLabelText(/label_alloggiati_auto_send/i)).toBeI…

[31m[2m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[1/1]⎯[22m[39m


[2m Test Files [22m [1m[31m1 failed[39m[22m[90m (1)[39m
[2m      Tests [22m [1m[31m1 failed[39m[22m[2m | [22m[1m[32m17 passed[39m[22m[90m (18)[39m
[2m   Start at [22m 08:23:41
[2m   Duration [22m 2.58s[2m (transform 167ms, setup 95ms, import 661ms, tests 1.06s, environment 639ms)[22m

Error: Could not find config file.
    at assertConfigurationExists (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/config/config-loader.js:80:17)
    at LegacyConfigLoader.loadConfigArrayForFile (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/config/config-loader.js:414:3)
    at async ESLint.calculateConfigForFile (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/eslint/eslint.js:1299:4)
    at async ESLint.isPathIgnored (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/eslint/eslint.js:1339:18) {
  messageTemplate: 'config-file-missing'
}
Error: Could not find config file.
    at assertConfigurationExists (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/config/config-loader.js:80:17)
    at LegacyConfigLoader.loadConfigArrayForFile (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/config/config-loader.js:414:3)
    at async ESLint.calculateConfigForFile (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/eslint/eslint.js:1299:4)
    at async ESLint.isPathIgnored (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/eslint/eslint.js:1339:18) {
  messageTemplate: 'config-file-missing'
}
Error: Could not find config file.
    at assertConfigurationExists (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/config/config-loader.js:80:17)
    at LegacyConfigLoader.loadConfigArrayForFile (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/config/config-loader.js:414:3)
    at async ESLint.calculateConfigForFile (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/eslint/eslint.js:1299:4)
    at async ESLint.isPathIgnored (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/eslint/eslint.js:1339:18) {
  messageTemplate: 'config-file-missing'
}
Error: Could not find config file.
    at assertConfigurationExists (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/config/config-loader.js:80:17)
    at LegacyConfigLoader.loadConfigArrayForFile (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/config/config-loader.js:414:3)
    at async ESLint.calculateConfigForFile (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/eslint/eslint.js:1299:4)
    at async ESLint.isPathIgnored (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/eslint/eslint.js:1339:18) {
  messageTemplate: 'config-file-missing'
}
Error: Could not find config file.
    at assertConfigurationExists (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/config/config-loader.js:80:17)
    at LegacyConfigLoader.loadConfigArrayForFile (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/config/config-loader.js:414:3)
    at async ESLint.calculateConfigForFile (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/eslint/eslint.js:1299:4)
    at async ESLint.isPathIgnored (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/eslint/eslint.js:1339:18) {
  messageTemplate: 'config-file-missing'
}
Error: Could not find config file.
    at assertConfigurationExists (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/config/config-loader.js:80:17)
    at LegacyConfigLoader.loadConfigArrayForFile (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/config/config-loader.js:414:3)
    at async ESLint.calculateConfigForFile (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/eslint/eslint.js:1299:4)
    at async ESLint.isPathIgnored (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/eslint/eslint.js:1339:18) {
  messageTemplate: 'config-file-missing'
}
Error: Could not find config file.
    at assertConfigurationExists (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/config/config-loader.js:80:17)
    at LegacyConfigLoader.loadConfigArrayForFile (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/config/config-loader.js:414:3)
    at async ESLint.calculateConfigForFile (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/eslint/eslint.js:1299:4)
    at async ESLint.isPathIgnored (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/eslint/eslint.js:1339:18) {
  messageTemplate: 'config-file-missing'
}
Error: Could not find config file.
    at assertConfigurationExists (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/config/config-loader.js:80:17)
    at LegacyConfigLoader.loadConfigArrayForFile (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/config/config-loader.js:414:3)
    at async ESLint.calculateConfigForFile (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/eslint/eslint.js:1299:4)
    at async ESLint.isPathIgnored (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/eslint/eslint.js:1339:18) {
  messageTemplate: 'config-file-missing'
}
Error: Could not find config file.
    at assertConfigurationExists (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/config/config-loader.js:80:17)
    at LegacyConfigLoader.loadConfigArrayForFile (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/config/config-loader.js:414:3)
    at async ESLint.calculateConfigForFile (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/eslint/eslint.js:1299:4)
    at async ESLint.isPathIgnored (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/eslint/eslint.js:1339:18) {
  messageTemplate: 'config-file-missing'
}
Error: Could not find config file.
    at assertConfigurationExists (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/config/config-loader.js:80:17)
    at LegacyConfigLoader.loadConfigArrayForFile (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/config/config-loader.js:414:3)
    at async ESLint.calculateConfigForFile (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/eslint/eslint.js:1299:4)
    at async ESLint.isPathIgnored (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/eslint/eslint.js:1339:18) {
  messageTemplate: 'config-file-missing'
}
Error: Could not find config file.
    at assertConfigurationExists (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/config/config-loader.js:80:17)
    at LegacyConfigLoader.loadConfigArrayForFile (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/config/config-loader.js:414:3)
    at async ESLint.calculateConfigForFile (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/eslint/eslint.js:1299:4)
    at async ESLint.isPathIgnored (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/eslint/eslint.js:1339:18) {
  messageTemplate: 'config-file-missing'
}
Error: Could not find config file.
    at assertConfigurationExists (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/config/config-loader.js:80:17)
    at LegacyConfigLoader.loadConfigArrayForFile (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/config/config-loader.js:414:3)
    at async ESLint.calculateConfigForFile (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/eslint/eslint.js:1299:4)
    at async ESLint.isPathIgnored (/Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/eslint/lib/eslint/eslint.js:1339:18) {
  messageTemplate: 'config-file-missing'
}
close timed out after 10000ms
Tests closed successfully but something prevents Vite server from exiting
You can try to identify the cause by enabling "hanging-process" reporter. See https://vitest.dev/guide/reporters.html#hanging-process-reporter


```
