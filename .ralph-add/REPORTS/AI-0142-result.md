# AI-0142 Hypervelocity Result

- Result: VERIFY_FAIL
- Worker: isolated Codex worktree
- Integration applier: single serialized real-worktree applier
- Sandbox pretest: FAIL
- Integration: applied 1 paths; backup=/Users/usuario/.ralph-hotel/backups/AI-0142-20260822-062940
- Changed paths:
  - frontend/src/pages/HotelProfile.tsx

## Worker log tail
```text
profile_title')).toBeInTheDocument());

    fireEvent.click(screen.getByRole('button', { name: /btn_save_profile/i }));
    await waitFor(() => expect(mockAddToast).toHaveBeenCalledWith('INVALID_VAT_NUMBER', 'error'));
  });

  it('should have no accessibility violations', async () => {
    const { container } = renderComponent();
    await waitFor(() => expect(screen.getByText('hotel_profile_title')).toBeInTheDocument());
    const results = await axe(container);
    expect(results).toHaveNoViolations();
  }, 30000);
});

codex
El código actual delega en `getErrorMessage`, pero ese helper exige que el objeto pase `axios.isAxiosError`; el contrato de esta pantalla depende del detalle estructural `response.data.detail`. Haré el ajuste quirúrgico en el `catch` de `HotelProfile` para conservar ese código cuando exista y el fallback actual en los demás casos; no tocaré el test preexistente salvo que la verificación lo requiera.
apply patch
patch: completed
/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0142-_0zoddib/frontend/src/pages/HotelProfile.tsx
diff --git a/frontend/src/pages/HotelProfile.tsx b/frontend/src/pages/HotelProfile.tsx
index 537763dfbc80d67ee0d17c5266d7478250e3e361..ffd71e9f960533612f339639cb2d9cc86d2fd900
--- a/frontend/src/pages/HotelProfile.tsx
+++ b/frontend/src/pages/HotelProfile.tsx
@@ -146,7 +146,13 @@
       await stayService.updateHotelSettings({ ...form, ...result.data });
       addToast(t('toast_profile_saved'), 'success');
     } catch (err: unknown) {
-      addToast(getErrorMessage(err, t('err_profile_save')), 'error');
+      const detail = (err as { response?: { data?: { detail?: unknown } } })?.response?.data?.detail;
+      addToast(
+        typeof detail === 'string' && detail.trim() !== ''
+          ? detail
+          : getErrorMessage(err, t('err_profile_save')),
+        'error',
+      );
     } finally {
       setSaving(false);
     }

diff --git a/frontend/src/pages/HotelProfile.tsx b/frontend/src/pages/HotelProfile.tsx
index 537763dfbc80d67ee0d17c5266d7478250e3e361..ffd71e9f960533612f339639cb2d9cc86d2fd900
--- a/frontend/src/pages/HotelProfile.tsx
+++ b/frontend/src/pages/HotelProfile.tsx
@@ -146,7 +146,13 @@
       await stayService.updateHotelSettings({ ...form, ...result.data });
       addToast(t('toast_profile_saved'), 'success');
     } catch (err: unknown) {
-      addToast(getErrorMessage(err, t('err_profile_save')), 'error');
+      const detail = (err as { response?: { data?: { detail?: unknown } } })?.response?.data?.detail;
+      addToast(
+        typeof detail === 'string' && detail.trim() !== ''
+          ? detail
+          : getErrorMessage(err, t('err_profile_save')),
+        'error',
+      );
     } finally {
       setSaving(false);
     }

exec
/bin/zsh -lc 'git diff --check && git diff -- frontend/src/pages/HotelProfile.tsx && git status --short -- frontend/src/pages/HotelProfile.tsx frontend/src/pages/HotelProfile.test.tsx && npm --prefix frontend exec vitest run src/pages/HotelProfile.test.tsx --reporter=dot' in /Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0142-_0zoddib
diff --git a/frontend/src/pages/HotelProfile.tsx b/frontend/src/pages/HotelProfile.tsx
index 537763dfbc80d67ee0d17c5266d7478250e3e361..ffd71e9f960533612f339639cb2d9cc86d2fd900
--- a/frontend/src/pages/HotelProfile.tsx
+++ b/frontend/src/pages/HotelProfile.tsx
@@ -146,7 +146,13 @@
       await stayService.updateHotelSettings({ ...form, ...result.data });
       addToast(t('toast_profile_saved'), 'success');
     } catch (err: unknown) {
-      addToast(getErrorMessage(err, t('err_profile_save')), 'error');
+      const detail = (err as { response?: { data?: { detail?: unknown } } })?.response?.data?.detail;
+      addToast(
+        typeof detail === 'string' && detail.trim() !== ''
+          ? detail
+          : getErrorMessage(err, t('err_profile_save')),
+        'error',
+      );
     } finally {
       setSaving(false);
     }

 exited 1 in 2169ms:
diff --git a/frontend/src/pages/HotelProfile.tsx b/frontend/src/pages/HotelProfile.tsx
index 537763d..ffd71e9 100644
--- a/frontend/src/pages/HotelProfile.tsx
+++ b/frontend/src/pages/HotelProfile.tsx
@@ -146,7 +146,13 @@ export function HotelProfile() {
       await stayService.updateHotelSettings({ ...form, ...result.data });
       addToast(t('toast_profile_saved'), 'success');
     } catch (err: unknown) {
-      addToast(getErrorMessage(err, t('err_profile_save')), 'error');
+      const detail = (err as { response?: { data?: { detail?: unknown } } })?.response?.data?.detail;
+      addToast(
+        typeof detail === 'string' && detail.trim() !== ''
+          ? detail
+          : getErrorMessage(err, t('err_profile_save')),
+        'error',
+      );
     } finally {
       setSaving(false);
     }
 M frontend/src/pages/HotelProfile.test.tsx
 M frontend/src/pages/HotelProfile.tsx
npm warn Unknown cli config "--reporter". This will stop working in the next major version of npm.

 RUN  v4.1.5 /Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0142-_0zoddib

stdout | frontend/src/pages/HotelProfile.test.tsx
🌐 i18next is maintained with support from Locize — consider powering your project with managed localization (AI, CDN, integrations): https://locize.com 💙

 ❯ frontend/src/pages/HotelProfile.test.tsx (18 tests | 18 failed) 9ms
     × renders the profile form with all fields including the alloggiati toggle 4ms
     × loads alloggiatiAutoSend=false and renders checkbox unchecked 0ms
     × loads alloggiatiAutoSend=true and renders checkbox checked 0ms
     × shows the toggle hint text below the checkbox 0ms
     × when backend returns alloggiatiAutoSend=true, save preserves the value 0ms
     × saving with toggle unchecked sends alloggiatiAutoSend: false 0ms
     × toggles alloggiatiAutoSend via the checkbox 0ms
     × hides the logo preview image on load error 0ms
     × shows an error toast when loading settings fails 0ms
     × shows an error toast when saving settings fails 0ms
     × renders Alloggiati credential fields, all blank, when none are configured 0ms
     × pre-fills the username but never the password/WsKey when credentials are configured 0ms
     × uses password-type inputs for the secret fields 0ms
     × toggles visibility independently for the password and WS key fields 0ms
     × saves the entered username/password/WsKey and clears the secret fields afterwards 0ms
     × blocks save and shows an error when VAT number is malformed 0ms
     × shows the backend detail instead of the generic fallback when saving fails 0ms
     × should have no accessibility violations 0ms

⎯⎯⎯⎯⎯⎯ Failed Tests 18 ⎯⎯⎯⎯⎯⎯⎯

 FAIL  frontend/src/pages/HotelProfile.test.tsx > HotelProfile > renders the profile form with all fields including the alloggiati toggle
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ renderComponent frontend/src/pages/HotelProfile.test.tsx:49:3
     47|
     48| const renderComponent = () =>
     49|   render(
       |   ^
     50|     <MemoryRouter>
     51|       <HotelProfile />
 ❯ frontend/src/pages/HotelProfile.test.tsx:63:5

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[1/18]⎯

 FAIL  frontend/src/pages/HotelProfile.test.tsx > HotelProfile > loads alloggiatiAutoSend=false and renders checkbox unchecked
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ renderComponent frontend/src/pages/HotelProfile.test.tsx:49:3
     47|
     48| const renderComponent = () =>
     49|   render(
       |   ^
     50|     <MemoryRouter>
     51|       <HotelProfile />
 ❯ frontend/src/pages/HotelProfile.test.tsx:75:5

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[2/18]⎯

 FAIL  frontend/src/pages/HotelProfile.test.tsx > HotelProfile > loads alloggiatiAutoSend=true and renders checkbox checked
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ renderComponent frontend/src/pages/HotelProfile.test.tsx:49:3
     47|
     48| const renderComponent = () =>
     49|   render(
       |   ^
     50|     <MemoryRouter>
     51|       <HotelProfile />
 ❯ frontend/src/pages/HotelProfile.test.tsx:87:5

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[3/18]⎯

 FAIL  frontend/src/pages/HotelProfile.test.tsx > HotelProfile > shows the toggle hint text below the checkbox
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ renderComponent frontend/src/pages/HotelProfile.test.tsx:49:3
     47|
     48| const renderComponent = () =>
     49|   render(
       |   ^
     50|     <MemoryRouter>
     51|       <HotelProfile />
 ❯ frontend/src/pages/HotelProfile.test.tsx:95:5

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[4/18]⎯

 FAIL  frontend/src/pages/HotelProfile.test.tsx > HotelProfile > when backend returns alloggiatiAutoSend=true, save preserves the value
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ renderComponent frontend/src/pages/HotelProfile.test.tsx:49:3
     47|
     48| const renderComponent = () =>
     49|   render(
       |   ^
     50|     <MemoryRouter>
     51|       <HotelProfile />
 ❯ frontend/src/pages/HotelProfile.test.tsx:105:5

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[5/18]⎯

 FAIL  frontend/src/pages/HotelProfile.test.tsx > HotelProfile > saving with toggle unchecked sends alloggiatiAutoSend: false
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ renderComponent frontend/src/pages/HotelProfile.test.tsx:49:3
     47|
     48| const renderComponent = () =>
     49|   render(
       |   ^
     50|     <MemoryRouter>
     51|       <HotelProfile />
 ❯ frontend/src/pages/HotelProfile.test.tsx:118:5

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[6/18]⎯

 FAIL  frontend/src/pages/HotelProfile.test.tsx > HotelProfile > toggles alloggiatiAutoSend via the checkbox
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ renderComponent frontend/src/pages/HotelProfile.test.tsx:49:3
     47|
     48| const renderComponent = () =>
     49|   render(
       |   ^
     50|     <MemoryRouter>
     51|       <HotelProfile />
 ❯ frontend/src/pages/HotelProfile.test.tsx:132:5

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[7/18]⎯

 FAIL  frontend/src/pages/HotelProfile.test.tsx > HotelProfile > hides the logo preview image on load error
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ renderComponent frontend/src/pages/HotelProfile.test.tsx:49:3
     47|
     48| const renderComponent = () =>
     49|   render(
       |   ^
     50|     <MemoryRouter>
     51|       <HotelProfile />
 ❯ frontend/src/pages/HotelProfile.test.tsx:143:5

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[8/18]⎯

 FAIL  frontend/src/pages/HotelProfile.test.tsx > HotelProfile > shows an error toast when loading settings fails
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ renderComponent frontend/src/pages/HotelProfile.test.tsx:49:3
     47|
     48| const renderComponent = () =>
     49|   render(
       |   ^
     50|     <MemoryRouter>
     51|       <HotelProfile />
 ❯ frontend/src/pages/HotelProfile.test.tsx:153:5

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[9/18]⎯

 FAIL  frontend/src/pages/HotelProfile.test.tsx > HotelProfile > shows an error toast when saving settings fails
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ renderComponent frontend/src/pages/HotelProfile.test.tsx:49:3
     47|
     48| const renderComponent = () =>
     49|   render(
       |   ^
     50|     <MemoryRouter>
     51|       <HotelProfile />
 ❯ frontend/src/pages/HotelProfile.test.tsx:159:5

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[10/18]⎯

 FAIL  frontend/src/pages/HotelProfile.test.tsx > HotelProfile > renders Alloggiati credential fields, all blank, when none are configured
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ renderComponent frontend/src/pages/HotelProfile.test.tsx:49:3
     47|
     48| const renderComponent = () =>
     49|   render(
       |   ^
     50|     <MemoryRouter>
     51|       <HotelProfile />
 ❯ frontend/src/pages/HotelProfile.test.tsx:167:5

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[11/18]⎯

 FAIL  frontend/src/pages/HotelProfile.test.tsx > HotelProfile > pre-fills the username but never the password/WsKey when credentials are configured
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ renderComponent frontend/src/pages/HotelProfile.test.tsx:49:3
     47|
     48| const renderComponent = () =>
     49|   render(
       |   ^
     50|     <MemoryRouter>
     51|       <HotelProfile />
 ❯ frontend/src/pages/HotelProfile.test.tsx:182:5

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[12/18]⎯

 FAIL  frontend/src/pages/HotelProfile.test.tsx > HotelProfile > uses password-type inputs for the secret fields
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ renderComponent frontend/src/pages/HotelProfile.test.tsx:49:3
     47|
     48| const renderComponent = () =>
     49|   render(
       |   ^
     50|     <MemoryRouter>
     51|       <HotelProfile />
 ❯ frontend/src/pages/HotelProfile.test.tsx:192:5

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[13/18]⎯

 FAIL  frontend/src/pages/HotelProfile.test.tsx > HotelProfile > toggles visibility independently for the password and WS key fields
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ renderComponent frontend/src/pages/HotelProfile.test.tsx:49:3
     47|
     48| const renderComponent = () =>
     49|   render(
       |   ^
     50|     <MemoryRouter>
     51|       <HotelProfile />
 ❯ frontend/src/pages/HotelProfile.test.tsx:200:5

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[14/18]⎯

 FAIL  frontend/src/pages/HotelProfile.test.tsx > HotelProfile > saves the entered username/password/WsKey and clears the secret fields afterwards
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ renderComponent frontend/src/pages/HotelProfile.test.tsx:49:3
     47|
     48| const renderComponent = () =>
     49|   render(
       |   ^
     50|     <MemoryRouter>
     51|       <HotelProfile />
 ❯ frontend/src/pages/HotelProfile.test.tsx:221:5

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[15/18]⎯

 FAIL  frontend/src/pages/HotelProfile.test.tsx > HotelProfile > blocks save and shows an error when VAT number is malformed
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ renderComponent frontend/src/pages/HotelProfile.test.tsx:49:3
     47|
     48| const renderComponent = () =>
     49|   render(
       |   ^
     50|     <MemoryRouter>
     51|       <HotelProfile />
 ❯ frontend/src/pages/HotelProfile.test.tsx:246:5

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[16/18]⎯

 FAIL  frontend/src/pages/HotelProfile.test.tsx > HotelProfile > shows the backend detail instead of the generic fallback when saving fails
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ renderComponent frontend/src/pages/HotelProfile.test.tsx:49:3
     47|
     48| const renderComponent = () =>
     49|   render(
       |   ^
     50|     <MemoryRouter>
     51|       <HotelProfile />
 ❯ frontend/src/pages/HotelProfile.test.tsx:260:5

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[17/18]⎯

 FAIL  frontend/src/pages/HotelProfile.test.tsx > HotelProfile > should have no accessibility violations
ReferenceError: document is not defined
 ❯ Proxy.render ../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/react/dist/pure.js:256:5
 ❯ renderComponent frontend/src/pages/HotelProfile.test.tsx:49:3
     47|
     48| const renderComponent = () =>
     49|   render(
       |   ^
     50|     <MemoryRouter>
     51|       <HotelProfile />
 ❯ frontend/src/pages/HotelProfile.test.tsx:268:27

⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[18/18]⎯


 Test Files  1 failed (1)
      Tests  18 failed (18)
   Start at  06:29:32
   Duration  1.19s (transform 184ms, setup 0ms, import 758ms, tests 9ms, environment 0ms)


diff --git a/frontend/src/pages/HotelProfile.tsx b/frontend/src/pages/HotelProfile.tsx
index 537763dfbc80d67ee0d17c5266d7478250e3e361..ffd71e9f960533612f339639cb2d9cc86d2fd900
--- a/frontend/src/pages/HotelProfile.tsx
+++ b/frontend/src/pages/HotelProfile.tsx
@@ -146,7 +146,13 @@
       await stayService.updateHotelSettings({ ...form, ...result.data });
       addToast(t('toast_profile_saved'), 'success');
     } catch (err: unknown) {
-      addToast(getErrorMessage(err, t('err_profile_save')), 'error');
+      const detail = (err as { response?: { data?: { detail?: unknown } } })?.response?.data?.detail;
+      addToast(
+        typeof detail === 'string' && detail.trim() !== ''
+          ? detail
+          : getErrorMessage(err, t('err_profile_save')),
+        'error',
+      );
     } finally {
       setSaving(false);
     }

codex
Implementé el ajuste mínimo en [HotelProfile.tsx](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0142-_0zoddib/frontend/src/pages/HotelProfile.tsx:149):

- Usa `response.data.detail` como código del toast, incluido `INVALID_VAT_NUMBER`.
- Conserva el fallback genérico y el flujo exitoso.
- No modifiqué migraciones, secretos, backend ni datos.

Verificación:

- `git diff --check`: correcto.
- La prueba focalizada no pudo ejecutarse por configuración del entorno: Vitest falla con `document is not defined` porque no carga el entorno `jsdom`. El test ya estaba modificado previamente en el worktree y fue preservado.
diff --git a/frontend/src/pages/HotelProfile.tsx b/frontend/src/pages/HotelProfile.tsx
index 537763dfbc80d67ee0d17c5266d7478250e3e361..ffd71e9f960533612f339639cb2d9cc86d2fd900
--- a/frontend/src/pages/HotelProfile.tsx
+++ b/frontend/src/pages/HotelProfile.tsx
@@ -146,7 +146,13 @@
       await stayService.updateHotelSettings({ ...form, ...result.data });
       addToast(t('toast_profile_saved'), 'success');
     } catch (err: unknown) {
-      addToast(getErrorMessage(err, t('err_profile_save')), 'error');
+      const detail = (err as { response?: { data?: { detail?: unknown } } })?.response?.data?.detail;
+      addToast(
+        typeof detail === 'string' && detail.trim() !== ''
+          ? detail
+          : getErrorMessage(err, t('err_profile_save')),
+        'error',
+      );
     } finally {
       setSaving(false);
     }

tokens used
37,649
Implementé el ajuste mínimo en [HotelProfile.tsx](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0142-_0zoddib/frontend/src/pages/HotelProfile.tsx:149):

- Usa `response.data.detail` como código del toast, incluido `INVALID_VAT_NUMBER`.
- Conserva el fallback genérico y el flujo exitoso.
- No modifiqué migraciones, secretos, backend ni datos.

Verificación:

- `git diff --check`: correcto.
- La prueba focalizada no pudo ejecutarse por configuración del entorno: Vitest falla con `document is not defined` porque no carga el entorno `jsdom`. El test ya estaba modificado previamente en el worktree y fue preservado.

$ npm --prefix frontend exec vitest run src/pages/HotelProfile.test.tsx --reporter=dot
BLOCKED_BY_SUPERVISOR

```
