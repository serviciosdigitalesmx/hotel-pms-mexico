# AI-0058 Verification Result

- Result: FAIL
- Source/workspace mutation: none detected
- Commands:
  - `npm --prefix frontend test -- --watchAll=false`
  - `npm --prefix frontend run build`

## Local build manifest
- api-gateway/build.gradle.kts
- auth-service/build.gradle.kts
- billing-service/build.gradle.kts
- billing-service/gradlew
- billing-service/settings.gradle.kts
- build.gradle.kts
- common-web-lib/build.gradle.kts
- config-service/build.gradle.kts
- fb-service/build.gradle.kts
- fb-service/settings.gradle.kts
- frontdesk-service/build.gradle.kts
- frontend/package-lock.json
- frontend/package.json
- gradlew
- guest-service/build.gradle.kts
- guest-service/gradlew
- guest-service/settings.gradle.kts
- internal-auth-lib/build.gradle.kts
- notification-service/build.gradle.kts
- pdf-template-engine/build.gradle.kts
- settings.gradle.kts

## Verification log tail
```text
$ npm --prefix frontend test -- --watchAll=false
exit=1

> frontend@0.0.0 test
> vitest run --watchAll=false

file:///Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/vitest/dist/chunks/cac.DJJmV0dT.js:406
          throw new CACError(`Unknown option \`${name.length > 1 ? `--${name}` : `-${name}`}\``);
                ^

CACError: Unknown option `--watchAll`
    at Command.checkUnknownOptions (file:///Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/vitest/dist/chunks/cac.DJJmV0dT.js:406:17)
    at CAC.runMatchedCommand (file:///Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/vitest/dist/chunks/cac.DJJmV0dT.js:606:13)
    at CAC.parse (file:///Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/vitest/dist/chunks/cac.DJJmV0dT.js:547:12)
    at file:///Users/usuario/Desktop/HOTEL-PMS/frontend/node_modules/vitest/dist/cli.js:11:13
    at ModuleJob.run (node:internal/modules/esm/module_job:569:25)
    at async node:internal/modules/esm/loader:650:26
    at async asyncRunEntryPointWithESMLoader (node:internal/modules/run_main:101:5)

Node.js v26.7.0


$ npm --prefix frontend run build
exit=0

> frontend@0.0.0 build
> tsc -b && vite build

vite v8.2.1 building client environment for production...
[2K
transforming...Browserslist: browsers data (caniuse-lite) is 6 months old. Please run:
  npx update-browserslist-db@latest
  Why you should do it regularly: https://github.com/browserslist/update-db#readme
✓ 1515 modules transformed.
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
dist/assets/errorMessage-BjTIjlnZ.js                          0.17 kB │ gzip:  0.16 kB
dist/assets/passwordPolicy-B-6Lzf6f.js                        0.21 kB │ gzip:  0.15 kB
dist/assets/M3Card-CGtAqvSy.js                                0.43 kB │ gzip:  0.26 kB
dist/assets/userService-aj5qU_b3.js                           0.44 kB │ gzip:  0.23 kB
dist/assets/rateSeasonService-BJZfp8zp.js                     0.48 kB │ gzip:  0.27 kB
dist/assets/guestService-MTSszPRh.js                          0.60 kB │ gzip:  0.34 kB
dist/assets/M3StatusChip-Crni-VQL.js                          0.60 kB │ gzip:  0.35 kB
dist/assets/fbService-DZJYeLlc.js                             0.61 kB │ gzip:  0.28 kB
dist/assets/PasswordVisibilityToggle-Bs91NOom.js              0.62 kB │ gzip:  0.39 kB
dist/assets/M3TableActionLink-n7xQBw27.js                     0.64 kB │ gzip:  0.36 kB
dist/assets/billingReportService-BHCJ0l-s.js                  0.73 kB │ gzip:  0.50 kB
dist/assets/reservationService-BfRTc-bX.js                    0.76 kB │ gzip:  0.39 kB
dist/assets/SettingsPageHeader-D6CMEb3q.js                    0.77 kB │ gzip:  0.42 kB
dist/assets/inventoryService-GXqkz0wa.js                      0.86 kB │ gzip:  0.37 kB
dist/assets/quotationService-CejWSTPY.js                      0.97 kB │ gzip:  0.44 kB
dist/assets/M3Table-BclZLXJu.js                               1.01 kB │ gzip:  0.47 kB
dist/assets/M3SegmentedRow-BexZ0nw3.js                        1.31 kB │ gzip:  0.70 kB
dist/assets/billingService-D4yVsusa.js                        1.33 kB │ gzip:  0.56 kB
dist/assets/SettingsProfile-CpZjuGwr.js                       1.33 kB │ gzip:  0.64 kB
dist/assets/dashboardService-8DECqf9c.js                      1.34 kB │ gzip:  0.68 kB
dist/assets/Login-EkAvan7R.js                                 1.58 kB │ gzip:  0.82 kB
dist/assets/M3Dialog-C_BW96en.js                              1.71 kB │ gzip:  0.81 kB
dist/assets/M3TextField-DsvFe7H8.js                           1.84 kB │ gzip:  0.91 kB
dist/assets/SettingsAccessibility-B-Dc8gGY.js                 2.46 kB │ gzip:  1.05 kB
dist/assets/Settings-BMajBUD4.js                              2.47 kB │ gzip:  1.00 kB
dist/assets/SettingsPassword-DHsmgEG8.js                      3.02 kB │ gzip:  1.32 kB
dist/assets/SettingsAppearance-BsljWUrZ.js                    3.05 kB │ gzip:  1.35 kB
dist/assets/RoomSelection-ABAR37hG.js                         3.61 kB │ gzip:  1.50 kB
dist/assets/CheckInForm-HRK7GJDV.js                           5.30 kB │ gzip:  2.18 kB
dist/assets/HotelProfile-WOnvxnC0.js                          5.33 kB │ gzip:  2.02 kB
dist/assets/Dashboard-jDDtlZG1.js                             5.91 kB │ gzip:  2.10 kB
dist/assets/Housekeeping-B-4v4exK.js                          6.04 kB │ gzip:  2.12 kB
dist/assets/OwnerDashboard-DFCiH6yI.js                        6.81 kB │ gzip:  2.09 kB
dist/assets/WalkInCheckInForm-BNqyvVjY.js                     7.35 kB │ gzip:  2.47 kB
dist/assets/Quotations-CKIkCyjO.js                            7.72 kB │ gzip:  2.56 kB
dist/assets/SettingsSystem-DgOZ31Jw.js                        7.86 kB │ gzip:  2.48 kB
dist/assets/ReservationForm-jpPsPZbN.js                       9.85 kB │ gzip:  3.36 kB
dist/assets/Reservations-B8z6c56H.js                         10.43 kB │ gzip:  3.46 kB
dist/assets/authStore-D_EzjumG.js                            10.53 kB │ gzip:  4.24 kB
dist/assets/GuestFieldSection-BpYxAXa2.js                    10.76 kB │ gzip:  3.08 kB
dist/assets/Stays-Kfthklb-.js                                11.00 kB │ gzip:  3.42 kB
dist/assets/QuotationForm-Cn-WNTyp.js                        11.28 kB │ gzip:  3.81 kB
dist/assets/QuotationDetail-BMuCR70Q.js                      11.86 kB │ gzip:  3.17 kB
dist/assets/AdminUsers-J3JDdflg.js                           13.15 kB │ gzip:  3.28 kB
dist/assets/RateCalendar-ULq04esE.js                         13.51 kB │ gzip:  4.55 kB
dist/assets/Assistant-Ban4Ak5Z.js                            15.87 kB │ gzip:  5.18 kB
dist/assets/Billing-B1yMZlyM.js                              16.02 kB │ gzip:  4.58 kB
dist/assets/Guests-ClPODlNw.js                               16.07 kB │ gzip:  4.53 kB
dist/assets/Restaurant-D-G3T02u.js                           21.71 kB │ gzip:  5.61 kB
dist/assets/Rooms-Bg19uyg6.js                                25.46 kB │ gzip:  5.52 kB
dist/assets/it-ChNJib2b.js                                   32.13 kB │ gzip:  8.15 kB
dist/assets/schemas-Bo3zeGyq.js                              69.31 kB │ gzip: 18.48 kB
dist/assets/CalendarPlanning-C0NBSYO3.js                    195.75 kB │ gzip: 57.97 kB
dist/assets/api-BB6Ng0p0.js                                 206.60 kB │ gzip: 66.32 kB
dist/assets/index-Bx_Cgl-o.js                               284.34 kB │ gzip: 90.50 kB

✓ built in 1.29s


```
