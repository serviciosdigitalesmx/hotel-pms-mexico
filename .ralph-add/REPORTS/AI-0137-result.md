# AI-0137 Hypervelocity Result

- Result: VERIFY_FAIL
- Worker: isolated Codex worktree
- Integration applier: single serialized real-worktree applier
- Sandbox pretest: FAIL
- Integration: no bounded delta
- Changed paths:

## Worker log tail
```text
de_modules/vite/dist/node/chunks/node.js:37064:3)
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
  path: '/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0137-ylfbn788/frontend/node_modules/.vite-temp/vite.config.ts.timestamp-1787401255780-233fe7506505e.mjs'
}




exec
/bin/zsh -lc 'ls -ld frontend/node_modules frontend/node_modules/.vite-temp 2>&1; git diff --name-only -- frontend/src/pages/HotelProfile.test.tsx frontend/src/pages/Stays.tsx frontend/src/pages/Stays.test.tsx; git diff --stat -- frontend/src/pages/HotelProfile.test.tsx frontend/src/pages/Stays.tsx frontend/src/pages/Stays.test.tsx' in /Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0137-ylfbn788
 succeeded in 479ms:
lrwxr-xr-x  1 usuario  staff  54 Aug 22 06:20 frontend/node_modules -> /Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules
drwxr-xr-x@ 2 usuario  staff  64 Aug 22 06:19 frontend/node_modules/.vite-temp
frontend/src/pages/HotelProfile.test.tsx
frontend/src/pages/Stays.tsx
 frontend/src/pages/HotelProfile.test.tsx | 2 +-
 frontend/src/pages/Stays.tsx             | 8 +++++++-
 2 files changed, 8 insertions(+), 2 deletions(-)

codex
La primera ejecución no llegó a las aserciones: `npm exec` no cargó la configuración Vite y dejó `document` indefinido. Al forzar el entorno jsdom, la carga de configuración intenta escribir en `frontend/node_modules`, que en este checkout es un symlink externo y el runner devuelve EPERM; continuaré con una verificación equivalente sin generar esos temporales y luego revisaré el diff autorizado.
exec
/bin/zsh -lc 'npm --prefix frontend exec -- vitest run --environment jsdom --setupFiles frontend/src/setupTests.ts src/pages/HotelProfile.test.tsx src/pages/Stays.test.tsx --reporter=dot' in /Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0137-ylfbn788
 exited 1 in 342ms:
file:///Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/vitest/dist/chunks/cac.DJJmV0dT.js:406
          throw new CACError(`Unknown option \`${name.length > 1 ? `--${name}` : `-${name}`}\``);
                ^

CACError: Unknown option `--setupFiles`
    at Command.checkUnknownOptions (file:///Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/vitest/dist/chunks/cac.DJJmV0dT.js:406:17)
    at CAC.runMatchedCommand (file:///Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/vitest/dist/chunks/cac.DJJmV0dT.js:606:13)
    at CAC.parse (file:///Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/vitest/dist/chunks/cac.DJJmV0dT.js:547:12)
    at file:///Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/vitest/dist/cli.js:11:13
    at ModuleJob.run (node:internal/modules/esm/module_job:569:25)
    at async node:internal/modules/esm/loader:650:26
    at async asyncRunEntryPointWithESMLoader (node:internal/modules/run_main:101:5)

Node.js v26.7.0

exec
/bin/zsh -lc 'git diff --check -- frontend/src/pages/HotelProfile.test.tsx frontend/src/pages/Stays.tsx frontend/src/pages/Stays.test.tsx; git diff -- frontend/src/pages/HotelProfile.test.tsx frontend/src/pages/Stays.tsx frontend/src/pages/Stays.test.tsx; git status --short -- frontend/src/pages/HotelProfile.test.tsx frontend/src/pages/Stays.tsx frontend/src/pages/Stays.test.tsx' in /Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0137-ylfbn788
 succeeded in 64ms:
diff --git a/frontend/src/pages/HotelProfile.test.tsx b/frontend/src/pages/HotelProfile.test.tsx
index 9499985..8f3067e 100644
--- a/frontend/src/pages/HotelProfile.test.tsx
+++ b/frontend/src/pages/HotelProfile.test.tsx
@@ -41,7 +41,7 @@ const baseSettings: HotelSettingsResponse = {
   sendReservationConfirmedEmail: true,
   sendCheckoutEmail: true,
   aiEnabled: false,
-  aiModel: 'openai/gpt-oss-20b',
+  aiModel: 'qwen3:4b-instruct-2507-q4_K_M',
   aiApiKeyConfigured: false,
 };
 
diff --git a/frontend/src/pages/Stays.tsx b/frontend/src/pages/Stays.tsx
index 401df3a..5f697a1 100644
--- a/frontend/src/pages/Stays.tsx
+++ b/frontend/src/pages/Stays.tsx
@@ -2,6 +2,7 @@ import { useState, useEffect, useCallback, useMemo, memo } from 'react';
 import { useNavigate, useLocation } from 'react-router-dom';
 import { stayService } from '../services/stayService';
 import { useToastStore } from '../store/toastStore';
+import { useAuthStore } from '../store/authStore';
 import type { StayResponse, StayStatus } from '../types/stay.types';
 import { MaterialIcon } from '../components/MaterialIcon';
 import { M3Button } from '../components/m3/M3Button';
@@ -10,6 +11,7 @@ import { useTranslation } from 'react-i18next';
 
 import { StayRow } from './Stays/StayRow';
 import { StayStatusChip } from './Stays/StayStatusChip';
+import { AlloggiatiReportSection } from './Stays/AlloggiatiReportSection';
 import { getStatusTone } from './Stays/stayStatusTone';
 import { getErrorMessage } from '../utils/errorMessage';
 
@@ -41,6 +43,8 @@ export const Stays = memo(() => {
   const [sortField, setSortField] = useState<StaySortField>(() => navState?.sortField ?? 'actualCheckInTime');
   const [sortDir, setSortDir] = useState<SortDir>(() => navState?.sortDir ?? 'desc');
   const addToast = useToastStore((s) => s.addToast);
+  const role = useAuthStore((s) => s.user?.role);
+  const isAdminOrOwner = role === 'ADMIN' || role === 'OWNER';
 
   useEffect(() => {
     const id = setTimeout(() => setDebouncedSearch(searchQuery), 300);
@@ -92,7 +96,7 @@ export const Stays = memo(() => {
       setTotalPages(data.totalPages);
     } catch (err: unknown) {
       const message = getErrorMessage(err, t('failed_load_stays'));
-      setError(message);
+      setError(message === 'alloggiati_failed' ? t('alloggiati_failed') : message);
     } finally {
       setLoading(false);
     }
@@ -235,6 +239,8 @@ export const Stays = memo(() => {
         </div>
       </div>
 
+      <AlloggiatiReportSection isAdminOrOwner={isAdminOrOwner} />
+
       {loading ? (
         <div className="flex justify-center items-center h-64 bg-surface rounded-shape-md shadow-elevation-1">
           <MaterialIcon name="progress_activity" size={32} className="text-primary animate-spin" />
 M frontend/src/pages/HotelProfile.test.tsx
 M frontend/src/pages/Stays.tsx

codex
Implementación acotada confirmada en los archivos permitidos:

- `HotelProfile.test.tsx`: actualizado `aiModel` al valor vigente.
- `Stays.tsx`: mantiene visible/traducido `alloggiati_failed` y respeta roles autorizados.
- `Stays.test.tsx`: sin cambios necesarios.

`git diff --check` pasa. Las pruebas no pudieron ejecutarse por EPERM del `node_modules` externo/symlink y la configuración Vite; no se modificaron artefactos fuera de alcance.
tokens used
51,117
Implementación acotada confirmada en los archivos permitidos:

- `HotelProfile.test.tsx`: actualizado `aiModel` al valor vigente.
- `Stays.tsx`: mantiene visible/traducido `alloggiati_failed` y respeta roles autorizados.
- `Stays.test.tsx`: sin cambios necesarios.

`git diff --check` pasa. Las pruebas no pudieron ejecutarse por EPERM del `node_modules` externo/symlink y la configuración Vite; no se modificaron artefactos fuera de alcance.

$ npm --prefix frontend exec vitest run src/pages/HotelProfile.test.tsx src/pages/Stays.test.tsx --reporter=dot
BLOCKED_BY_SUPERVISOR

$ npm --prefix frontend run build
exit=0

> frontend@0.0.0 build
> tsc -b && vite build

vite v8.2.1 building client environment for production...
[2K
transforming...Browserslist: browsers data (caniuse-lite) is 6 months old. Please run:
  npx update-browserslist-db@latest
  Why you should do it regularly: https://github.com/browserslist/update-db#readme
✓ 1516 modules transformed.
rendering chunks...
computing gzip size...
dist/index.html                                               0.73 kB │ gzip:  0.41 kB
dist/assets/inter-vietnamese-400-normal-DMkecbls.woff2        4.97 kB
dist/assets/inter-vietnamese-300-normal-Bdr24Bqb.woff2        5.06 kB
dist/assets/inter-vietnamese-600-normal-Cc8MFFhd.woff2        5.10 kB
dist/assets/inter-vietnamese-700-normal-DlLaEgI2.woff2        5.10 kB
dist/assets/inter-vietnamese-500-normal-DOriooB6.woff2        5.11 kB
dist/assets/inter-greek-ext-400-normal-DGGRlc-M.woff2         5.26 kB
dist/assets/inter-greek-ext-300-normal-l2DDyC6M.woff2         5.34 kB
dist/assets/inter-greek-ext-500-normal-C4iEst2y.woff2         5.42 kB
dist/assets/inter-greek-ext-600-normal-DRtmH8MT.woff2         5.43 kB
dist/assets/inter-greek-ext-700-normal-qfdV9bQt.woff2         5.44 kB
dist/assets/outfit-latin-ext-500-normal-zeox_O30.woff2        6.24 kB
dist/assets/outfit-latin-ext-400-normal-5tcqmc2S.woff2        6.38 kB
dist/assets/outfit-latin-ext-800-normal-DRv2ic_2.woff2        6.47 kB
dist/assets/outfit-latin-ext-700-normal-CI4iH74K.woff2        6.48 kB
dist/assets/inter-vietnamese-400-normal-Bbgyi5SW.woff         6.50 kB
dist/assets/outfit-latin-ext-600-normal-B85nYjL1.woff2        6.52 kB
dist/assets/inter-vietnamese-300-normal-DDGmYYdT.woff         6.58 kB
dist/assets/inter-vietnamese-500-normal-mJboJaSs.woff         6.59 kB
dist/assets/inter-vietnamese-700-normal-BZaoP0fm.woff         6.63 kB
dist/assets/inter-vietnamese-600-normal-BuLX-rYi.woff         6.64 kB
dist/assets/inter-greek-ext-400-normal-KugGGMne.woff          7.06 kB
dist/assets/inter-greek-ext-300-normal-DLbbeei1.woff          7.12 kB
dist/assets/inter-greek-ext-500-normal-2j5mBUwD.woff          7.19 kB
dist/assets/inter-greek-ext-600-normal-B8X0CLgF.woff          7.21 kB
dist/assets/inter-greek-ext-700-normal-BoQ6DsYi.woff          7.21 kB
dist/assets/inter-cyrillic-400-normal-obahsSVq.woff2          7.71 kB
dist/assets/inter-greek-400-normal-B4URO6DV.woff2             7.77 kB
dist/assets/inter-cyrillic-300-normal-BnqRxXuy.woff2          7.81 kB
dist/assets/inter-cyrillic-500-normal-BasfLYem.woff2          7.90 kB
dist/assets/inter-cyrillic-700-normal-CjBOestx.woff2          7.90 kB
dist/assets/inter-greek-500-normal-BIZE56-Y.woff2             7.92 kB
dist/assets/inter-greek-700-normal-C3JjAnD8.woff2             7.92 kB
dist/assets/inter-greek-600-normal-plRanbMR.woff2             7.94 kB
dist/assets/inter-cyrillic-600-normal-CWCymEST.woff2          7.97 kB
dist/assets/inter-greek-300-normal-DmGD3g_f.woff2             8.00 kB
dist/assets/outfit-latin-ext-500-normal-DrCvqoFD.woff         8.49 kB
dist/assets/outfit-latin-ext-400-normal-DHm7mdGe.woff         8.64 kB
dist/assets/outfit-latin-ext-600-normal-CWJcPgd7.woff         8.75 kB
dist/assets/outfit-latin-ext-800-normal-DyhPHUt-.woff         8.76 kB
dist/assets/outfit-latin-ext-700-normal-fjS8-Gm7.woff         8.78 kB
dist/assets/inter-cyrillic-400-normal-HOLc17fK.woff           9.78 kB
dist/assets/inter-cyrillic-300-normal-LR1W_oT8.woff           9.81 kB
dist/assets/inter-cyrillic-700-normal-DrXBdSj3.woff           9.91 kB
dist/assets/inter-greek-400-normal-q2sYcFCs.woff              9.92 kB
dist/assets/inter-cyrillic-600-normal-4D_pXhcN.woff           9.93 kB
dist/assets/inter-cyrillic-500-normal-CxZf_p3X.woff           9.94 kB
dist/assets/inter-greek-500-normal-Xzm54t5V.woff              9.98 kB
dist/assets/inter-greek-700-normal-BUv2fZ6O.woff              9.98 kB
dist/assets/inter-greek-300-normal-BrhSP0vQ.woff              9.99 kB
dist/assets/inter-greek-600-normal-BZpKdvQh.woff             10.03 kB
dist/assets/inter-cyrillic-ext-400-normal-BQZuk6qB.woff2     10.23 kB
dist/assets/inter-cyrillic-ext-300-normal-CgCALhwJ.woff2     10.35 kB
dist/assets/inter-cyrillic-ext-500-normal-B0yAr1jD.woff2     10.43 kB
dist/assets/inter-cyrillic-ext-600-normal-Dfes3d0z.woff2     10.48 kB
dist/assets/inter-cyrillic-ext-700-normal-BjwYoWNd.woff2     10.49 kB
dist/assets/inter-cyrillic-ext-400-normal-DQukG94-.woff      13.33 kB
dist/assets/inter-cyrillic-ext-300-normal-RId2JxDB.woff      13.40 kB
dist/assets/inter-cyrillic-ext-700-normal-LO58E6JB.woff      13.40 kB
dist/assets/inter-cyrillic-ext-500-normal-BmqWE9Dz.woff      13.45 kB
dist/assets/inter-cyrillic-ext-600-normal-Bcila6Z-.woff      13.46 kB
dist/assets/outfit-latin-500-normal-DKnIMDSk.woff2           13.52 kB
dist/assets/outfit-latin-400-normal-BGsTXAXT.woff2           14.03 kB
dist/assets/outfit-latin-800-normal-CQna6-G7.woff2           14.04 kB
dist/assets/outfit-latin-700-normal-Cu9v6i1X.woff2           14.06 kB
dist/assets/outfit-latin-600-normal-B7SfZ07L.woff2           14.14 kB
dist/assets/outfit-latin-500-normal-ClnHRwRh.woff            17.68 kB
dist/assets/outfit-latin-400-normal-DMwTpYkH.woff            18.23 kB
dist/assets/outfit-latin-700-normal-D4itBLBr.woff            18.34 kB
dist/assets/outfit-latin-800-normal-BRHLSPcU.woff            18.39 kB
dist/assets/outfit-latin-600-normal-BEfTtDA7.woff            18.41 kB
dist/assets/inter-latin-400-normal-C38fXH4l.woff2            23.66 kB
dist/assets/inter-latin-300-normal-BVlfKGgI.woff2            23.91 kB
dist/assets/inter-latin-500-normal-Cerq10X2.woff2            24.27 kB
dist/assets/inter-latin-700-normal-Yt3aPRUw.woff2            24.35 kB
dist/assets/inter-latin-600-normal-LgqL8muc.woff2            24.45 kB
dist/assets/inter-latin-400-normal-CyCys3Eg.woff             30.69 kB
dist/assets/inter-latin-300-normal-i8F0SvXL.woff             31.01 kB
dist/assets/inter-latin-600-normal-CiBQ2DWP.woff             31.26 kB
dist/assets/inter-latin-500-normal-BL9OpVg8.woff             31.28 kB
dist/assets/inter-latin-700-normal-BLAVimhd.woff             31.32 kB
dist/assets/inter-latin-ext-400-normal-C1nco2VV.woff2        35.00 kB
dist/assets/inter-latin-ext-300-normal-CPgO9Ksf.woff2        35.88 kB
dist/assets/inter-latin-ext-500-normal-CV4jyFjo.woff2        36.02 kB
dist/assets/inter-latin-ext-700-normal-Ca8adRJv.woff2        36.24 kB
dist/assets/inter-latin-ext-600-normal-D2bJ5OIk.woff2        36.26 kB
dist/assets/inter-latin-ext-400-normal-77YHD8bZ.woff         47.56 kB
dist/assets/inter-latin-ext-500-normal-BxGbmqWO.woff         48.49 kB
dist/assets/inter-latin-ext-300-normal-Dp1L8vcn.woff         48.60 kB
dist/assets/inter-latin-ext-700-normal-TidjK2hL.woff         48.63 kB
dist/assets/inter-latin-ext-600-normal-CIVaiw4L.woff         48.66 kB
dist/assets/material-symbols-outlined-DTCSuhiZ.woff2      3,915.91 kB
dist/assets/CalendarPlanning-COks4oDc.css                    10.62 kB │ gzip:  2.42 kB
dist/assets/index-Dh8JP8NF.css                               51.10 kB │ gzip:  9.38 kB
dist/assets/errorMessage-eCQNYHno.js                          0.17 kB │ gzip:  0.16 kB
dist/assets/passwordPolicy-B-6Lzf6f.js                        0.21 kB │ gzip:  0.15 kB
dist/assets/M3Card-CGtAqvSy.js                                0.43 kB │ gzip:  0.26 kB
dist/assets/userService-DCStX85W.js                           0.44 kB │ gzip:  0.23 kB
dist/assets/rateSeasonService-C5nQSYDe.js                     0.48 kB │ gzip:  0.27 kB
dist/assets/guestService-798lX1i0.js                          0.60 kB │ gzip:  0.34 kB
dist/assets/M3StatusChip-Crni-VQL.js                          0.60 kB │ gzip:  0.35 kB
dist/assets/fbService-CxqzHT0e.js                             0.61 kB │ gzip:  0.28 kB
dist/assets/PasswordVisibilityToggle-DTob0Akx.js              0.62 kB │ gzip:  0.39 kB
dist/assets/M3TableActionLink-n7xQBw27.js                     0.64 kB │ gzip:  0.36 kB
dist/assets/billingReportService-D-RR_Y9Z.js                  0.73 kB │ gzip:  0.50 kB
dist/assets/reservationService-Cg5V356C.js                    0.76 kB │ gzip:  0.39 kB
dist/assets/SettingsPageHeader-BmDX0CL8.js                    0.77 kB │ gzip:  0.42 kB
dist/assets/inventoryService-CdDdeaIp.js                      0.86 kB │ gzip:  0.37 kB
dist/assets/quotationService-DeAV7uzD.js                      0.97 kB │ gzip:  0.44 kB
dist/assets/M3Table-BclZLXJu.js                               1.01 kB │ gzip:  0.47 kB
dist/assets/M3SegmentedRow-BKz3L0t6.js                        1.31 kB │ gzip:  0.71 kB
dist/assets/billingService-CNiCAPFQ.js                        1.33 kB │ gzip:  0.56 kB
dist/assets/SettingsProfile-CEsWWYiL.js                       1.33 kB │ gzip:  0.64 kB
dist/assets/dashboardService-4FPQUo0J.js                      1.34 kB │ gzip:  0.68 kB
dist/assets/Login-BPgeXS5p.js                                 1.58 kB │ gzip:  0.82 kB
dist/assets/M3Dialog-DR7CIi1g.js                              1.71 kB │ gzip:  0.81 kB
dist/assets/M3TextField-BpUK5o1r.js                           1.84 kB │ gzip:  0.91 kB
dist/assets/SettingsAccessibility-CFYKDYxk.js                 2.46 kB │ gzip:  1.05 kB
dist/assets/Settings-BJcp3KAl.js                              2.47 kB │ gzip:  1.00 kB
dist/assets/SettingsPassword-BjMnq7Em.js                      3.02 kB │ gzip:  1.32 kB
dist/assets/SettingsAppearance-Ty4xkgqh.js                    3.07 kB │ gzip:  1.36 kB
dist/assets/RoomSelection-BRZKijAv.js                         3.62 kB │ gzip:  1.50 kB
dist/assets/CheckInForm-BU4xu-zj.js                           5.30 kB │ gzip:  2.18 kB
dist/assets/HotelProfile-DIK-5rpz.js                          5.33 kB │ gzip:  2.02 kB
dist/assets/Dashboard-CVV4FLYG.js                             5.91 kB │ gzip:  2.10 kB
dist/assets/Housekeeping-a2VRsLF1.js                          6.04 kB │ gzip:  2.11 kB
dist/assets/OwnerDashboard-CwYBtA5w.js                        6.81 kB │ gzip:  2.09 kB
dist/assets/WalkInCheckInForm-C430HnW7.js                     7.35 kB │ gzip:  2.47 kB
dist/assets/Quotations-C4PJzhjF.js                            7.72 kB │ gzip:  2.56 kB
dist/assets/SettingsSystem-g2vpf1DB.js                        7.88 kB │ gzip:  2.49 kB
dist/assets/ReservationForm-BUpdi2lM.js                       9.85 kB │ gzip:  3.36 kB
dist/assets/Reservations-Chqblm4Y.js                         10.43 kB │ gzip:  3.46 kB
dist/assets/authStore-D_EzjumG.js                            10.53 kB │ gzip:  4.24 kB
dist/assets/GuestFieldSection-CRxWBRvR.js                    10.76 kB │ gzip:  3.07 kB
dist/assets/QuotationForm-C8JM5nbk.js                        11.28 kB │ gzip:  3.81 kB
dist/assets/QuotationDetail-DKw552f9.js                      11.86 kB │ gzip:  3.17 kB
dist/assets/AdminUsers-B6EKXRsj.js                           13.15 kB │ gzip:  3.29 kB
dist/assets/RateCalendar-CXyOUX4H.js                         13.50 kB │ gzip:  4.54 kB
dist/assets/Stays-D3wbv5MT.js                                14.00 kB │ gzip:  4.11 kB
dist/assets/Assistant-NeiKhCjh.js                            15.87 kB │ gzip:  5.18 kB
dist/assets/Guests-i3fnGpFO.js                               16.07 kB │ gzip:  4.53 kB
dist/assets/Billing-Qz_CxV9K.js                              17.74 kB │ gzip:  4.89 kB
dist/assets/Restaurant-j578vpm6.js                           21.71 kB │ gzip:  5.61 kB
dist/assets/Rooms-DxXTFfGz.js                                25.46 kB │ gzip:  5.51 kB
dist/assets/it-ChNJib2b.js                                   32.13 kB │ gzip:  8.15 kB
dist/assets/schemas-Bo3zeGyq.js                              69.31 kB │ gzip: 18.48 kB
dist/assets/CalendarPlanning-Drztg7aU.js                    195.75 kB │ gzip: 57.97 kB
dist/assets/api-C5oH9PS7.js                                 206.61 kB │ gzip: 66.32 kB
dist/assets/index-BSrhyi3h.js                               284.35 kB │ gzip: 90.50 kB

✓ built in 1.16s


```
