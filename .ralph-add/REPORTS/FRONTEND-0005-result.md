# FRONTEND-0005 Hypervelocity Result

- Result: PASS
- Worker: isolated Codex worktree
- Integration applier: single serialized real-worktree applier
- Sandbox pretest: PASS
- Integration: applied 1 paths; backup=/Users/usuario/.ralph-hotel/backups/FRONTEND-0005-20260822-082512
- Changed paths:
  - frontend/src/pages/HotelProfile.tsx

## Worker log tail
```text
_configured' : 'placeholder_alloggiati_credential_unconfigured')}
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
-        <M3Button icon="save" onClick={handleSave} disabled={saving}>
+        <M3Button icon="save" onClick={() => void handleSave()} disabled={saving}>
           {saving ? t('btn_saving') : t('btn_save_profile')}
         </M3Button>
       </div>
 M api-gateway/src/main/java/com/hotelpms/gateway/filter/CsrfFilter.java
 M api-gateway/src/main/java/com/hotelpms/gateway/filter/PublicBookingFilter.java
 M auth-service/src/test/java/com/hotelpms/auth/service/UserManagementServiceImplTest.java
 M config-service/src/main/resources/config/api-gateway.yml
 M config-service/src/main/resources/config/frontdesk-service.yml
 M docker-compose.yml
 M docker/prometheus/alert_rules.yml
 M docs/FINAL_AUDIT_ULTRA_SEVERE.md
 M docs/OPERATIONS_RUNBOOK.md
 M docs/ROADMAP.md
 M frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantController.java
 M frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java
 M frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantToolCatalog.java
 M frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/ConversationStep.java
 M frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/DeterministicParser.java
 M frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/LocalIntent.java
 M frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouter.java
 M frontdesk-service/src/main/java/com/hotelpms/frontdesk/client/dto/GuestCreateRequest.java
 M frontdesk-service/src/main/java/com/hotelpms/frontdesk/stays/domain/HotelSettings.java
 M frontdesk-service/src/main/java/com/hotelpms/frontdesk/stays/dto/HotelSettingsResponse.java
 M frontdesk-service/src/main/java/com/hotelpms/frontdesk/stays/repository/HotelSettingsRepository.java
 M frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/AssistantServiceTest.java
 M frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouterTest.java
 M frontend/src/locales/en/quotations.json
 M frontend/src/locales/en/reservations.json
 M frontend/src/locales/en/restaurant.json
 M frontend/src/pages/Billing/InvoiceDetailModal.tsx
 M frontend/src/pages/GuestFormModal.tsx
 M frontend/src/pages/HotelProfile.test.tsx
 M frontend/src/pages/HotelProfile.tsx
 M frontend/src/pages/Quotations.test.tsx
 M frontend/src/pages/Quotations/QuotationForm.test.tsx
 M frontend/src/pages/Rates/RateCalendar.test.tsx
 M frontend/src/pages/Rates/RateCalendar.tsx
 M frontend/src/pages/Reservations/RoomSelection.test.tsx
 M frontend/src/pages/Reservations/RoomSelection.tsx
 M frontend/src/pages/Rooms/RateSeasonManagerModal.test.tsx
 M frontend/src/pages/Rooms/RoomTypeList.test.tsx
 M frontend/src/pages/Settings/SettingsAppearance.test.tsx
 M frontend/src/pages/Settings/SettingsAppearance.tsx
 M frontend/src/pages/Settings/SettingsSystem.test.tsx
 M frontend/src/pages/Settings/SettingsSystem.tsx
 M frontend/src/pages/Stays.tsx
 M frontend/src/pages/Stays/StayRow.tsx
 M frontend/src/setupTests.ts
 M frontend/src/types/guest.types.ts
 M frontend/src/types/stay.types.ts
 M guest-service/src/main/java/com/hotelpms/guest/dto/request/GuestRequest.java
 M guest-service/src/main/java/com/hotelpms/guest/dto/response/GuestResponse.java
 M guest-service/src/main/java/com/hotelpms/guest/model/Guest.java
 M guest-service/src/main/java/com/hotelpms/guest/service/impl/GuestServiceImpl.java
 M guest-service/src/test/java/com/hotelpms/guest/controller/GuestControllerTest.java
 M guest-service/src/test/java/com/hotelpms/guest/service/impl/GuestServiceImplTest.java
?? .add/
?? .ralph_attempts.json
?? RALPH_TASK.md
?? api-gateway/src/test/java/com/hotelpms/gateway/filter/PublicBookingFilterTest.java
?? argos
?? argos-local
?? docker-compose.ollama.yml
?? docker-compose.ollama.yml.backup-network-20260818-010053
?? frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantController.java.backup-20260818-005334
?? frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantController.java.backup-ai-only-20260818-005244
?? frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/PermanentAiProviderException.java
?? frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/RetryableAiProviderException.java
?? frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/ResilientIntentFallbackHandler.java
?? frontdesk-service/src/main/java/com/hotelpms/frontdesk/stays/controller/PublicHotelResolverController.java
?? frontdesk-service/src/main/resources/db/migration/V12__add_version_to_rate_seasons.sql
?? frontdesk-service/src/main/resources/db/migration/V13__add_version_to_quotations.sql
?? frontdesk-service/src/main/resources/db/migration/V14__add_reservation_overlap_exclusion.sql
?? frontdesk-service/src/main/resources/db/migration/V21__migrate_ai_to_deepseek.sql
?? frontdesk-service/src/main/resources/db/migration/V22__use_ollama_by_default_keep_deepseek_option.sql
?? frontdesk-service/src/main/resources/db/migration/V23__use_qwen3_4b_instruct_q4.sql
?? frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/BatchCheckInParserTest.java
?? frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/ResilientIntentFallbackHandlerTest.java
?? guest-service/src/main/resources/db/migration/V10__add_mexico_cfdi_guest_profile.sql
?? node_modules
?? scripts/

diff --git a/frontend/src/pages/HotelProfile.tsx b/frontend/src/pages/HotelProfile.tsx
index a00b2ead55801c0e2c8b4ff6e669d8b3ce258a77..8220b983f0e8850c8ecbc0aaa7bb74459909d753
--- a/frontend/src/pages/HotelProfile.tsx
+++ b/frontend/src/pages/HotelProfile.tsx
@@ -70,6 +70,7 @@
     hotelName: '',
     address: '',
     vatNumber: '',
+    fiscalCode: '',
     logoUrl: '',
     city: '',
     state: '',
@@ -108,6 +109,7 @@
           hotelName: s.hotelName ?? '',
           address: s.address ?? '',
           vatNumber: s.vatNumber ?? '',
+          fiscalCode: s.fiscalCode ?? '',
           logoUrl: s.logoUrl ?? '',
           city: s.city ?? '',
           state: s.state ?? '',
@@ -256,12 +258,19 @@
         </div>
 
         <ProfileField
-            id="profile-rfc"
-            label={t('label_vat_number')}
-            value={form.vatNumber ?? ''}
-            placeholder={t('placeholder_vat_number')}
-            onChange={handleChange('vatNumber')}
-            error={fieldErrors.vatNumber}
+          id="profile-rfc"
+          label={t('label_vat_number')}
+          value={form.vatNumber ?? ''}
+          placeholder={t('placeholder_vat_number')}
+          onChange={handleChange('vatNumber')}
+          error={fieldErrors.vatNumber}
+        />
+
+        <ProfileField
+          id="profile-fiscal-code"
+          label={t('label_fiscal_code')}
+          value={form.fiscalCode ?? ''}
+          onChange={handleChange('fiscalCode')}
         />
 
         <div className="grid grid-cols-2 gap-4">

codex
Implementado en `frontend/src/pages/HotelProfile.tsx`:

- Añadido `fiscalCode` al formulario.
- Cargado desde `HotelSettingsResponse`.
- Renderizado con `label_fiscal_code` y asociación accesible mediante `htmlFor`.

La verificación obligatoria no pudo iniciar por un error de permisos `EPERM` al escribir `frontend/node_modules/.vite-temp`; no fue una falla de la prueba. No se modificaron otros archivos.
diff --git a/frontend/src/pages/HotelProfile.tsx b/frontend/src/pages/HotelProfile.tsx
index a00b2ead55801c0e2c8b4ff6e669d8b3ce258a77..8220b983f0e8850c8ecbc0aaa7bb74459909d753
--- a/frontend/src/pages/HotelProfile.tsx
+++ b/frontend/src/pages/HotelProfile.tsx
@@ -70,6 +70,7 @@
     hotelName: '',
     address: '',
     vatNumber: '',
+    fiscalCode: '',
     logoUrl: '',
     city: '',
     state: '',
@@ -108,6 +109,7 @@
           hotelName: s.hotelName ?? '',
           address: s.address ?? '',
           vatNumber: s.vatNumber ?? '',
+          fiscalCode: s.fiscalCode ?? '',
           logoUrl: s.logoUrl ?? '',
           city: s.city ?? '',
           state: s.state ?? '',
@@ -256,12 +258,19 @@
         </div>
 
         <ProfileField
-            id="profile-rfc"
-            label={t('label_vat_number')}
-            value={form.vatNumber ?? ''}
-            placeholder={t('placeholder_vat_number')}
-            onChange={handleChange('vatNumber')}
-            error={fieldErrors.vatNumber}
+          id="profile-rfc"
+          label={t('label_vat_number')}
+          value={form.vatNumber ?? ''}
+          placeholder={t('placeholder_vat_number')}
+          onChange={handleChange('vatNumber')}
+          error={fieldErrors.vatNumber}
+        />
+
+        <ProfileField
+          id="profile-fiscal-code"
+          label={t('label_fiscal_code')}
+          value={form.fiscalCode ?? ''}
+          onChange={handleChange('fiscalCode')}
         />
 
         <div className="grid grid-cols-2 gap-4">

tokens used
21,472
Implementado en `frontend/src/pages/HotelProfile.tsx`:

- Añadido `fiscalCode` al formulario.
- Cargado desde `HotelSettingsResponse`.
- Renderizado con `label_fiscal_code` y asociación accesible mediante `htmlFor`.

La verificación obligatoria no pudo iniciar por un error de permisos `EPERM` al escribir `frontend/node_modules/.vite-temp`; no fue una falla de la prueba. No se modificaron otros archivos.

$ npm --prefix frontend exec -- vitest run --root frontend src/pages/HotelProfile.test.tsx --pool=threads --maxWorkers=1 --reporter=dot
exit=0

[1m[30m[46m RUN [49m[39m[22m [36mv4.1.5 [39m[90m/Users/usuario/.ralph-hotel/hypervelocity-worktrees/FRONTEND-0005-78tbbdoq/frontend[39m

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
[90mstdout[2m | src/pages/HotelProfile.test.tsx
[22m[39m🌐 i18next is maintained with support from Locize — consider powering your project with managed localization (AI, CDN, integrations): https://locize.com 💙

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
[33m[39m[32m·[39m[33m[39m[32m·[39m[33m[39m[32m·[39m[33m[39m[32m·[39m[33m[39m[32m·[39m[33m[39m[32m·[39m[33m[39m[32m·[39m[33m[39m[32m·[39m[33m[39m[32m·[39m[33m[39m[32m·[39m[33m[39m[32m·[39m[33m[39m[32m·[39m[33m[39m[32m·[39m[33m[39m[32m·[39mNot implemented: HTMLCanvasElement's getContext() method: without installing the canvas npm package
[33m[39m[32m·[39m[33m[39m[32m·[39m[33m[39m[32m·[39m[33m[39m[32m·[39m

[2m Test Files [22m [1m[32m1 passed[39m[22m[90m (1)[39m
[2m      Tests [22m [1m[32m18 passed[39m[22m[90m (18)[39m
[2m   Start at [22m 08:25:00
[2m   Duration [22m 2.70s[2m (transform 204ms, setup 96ms, import 736ms, tests 1.14s, environment 628ms)[22m

close timed out after 10000ms
Tests closed successfully but something prevents Vite server from exiting
You can try to identify the cause by enabling "hanging-process" reporter. See https://vitest.dev/guide/reporters.html#hanging-process-reporter


```
