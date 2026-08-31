# FRONTEND-0002 Hypervelocity Result

- Result: NOT_APPLIED
- Worker: isolated Codex worktree
- Integration applier: single serialized real-worktree applier
- Sandbox pretest: FAIL
- Integration: unauthorized paths: ['frontend/tsconfig.app.tsbuildinfo', 'frontend/tsconfig.vitest.tsbuildinfo']
- Changed paths:
  - frontend/src/pages/HotelProfile.tsx
  - frontend/tsconfig.app.tsbuildinfo
  - frontend/tsconfig.vitest.tsbuildinfo

## Worker log tail
```text
m
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
[2m   Start at [22m 08:20:14
[2m   Duration [22m 2.60s[2m (transform 173ms, setup 102ms, import 677ms, tests 1.08s, environment 616ms)[22m

close timed out after 10000ms
Tests closed successfully but something prevents Vite server from exiting
You can try to identify the cause by enabling "hanging-process" reporter. See https://vitest.dev/guide/reporters.html#hanging-process-reporter


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
dist/assets/index-BATNp0Yo.css                               51.19 kB │ gzip:  9.40 kB
dist/assets/errorMessage-eCQNYHno.js                          0.17 kB │ gzip:  0.16 kB
dist/assets/passwordPolicy-B-6Lzf6f.js                        0.21 kB │ gzip:  0.15 kB
dist/assets/M3Card-CGtAqvSy.js                                0.43 kB │ gzip:  0.26 kB
dist/assets/userService-DCStX85W.js                           0.44 kB │ gzip:  0.23 kB
dist/assets/rateSeasonService-C5nQSYDe.js                     0.48 kB │ gzip:  0.27 kB
dist/assets/guestService-798lX1i0.js                          0.60 kB │ gzip:  0.34 kB
dist/assets/M3StatusChip-Crni-VQL.js                          0.60 kB │ gzip:  0.35 kB
dist/assets/fbService-CxqzHT0e.js                             0.61 kB │ gzip:  0.28 kB
dist/assets/PasswordVisibilityToggle-CI2mifOJ.js              0.62 kB │ gzip:  0.40 kB
dist/assets/M3TableActionLink-n7xQBw27.js                     0.64 kB │ gzip:  0.36 kB
dist/assets/billingReportService-D-RR_Y9Z.js                  0.73 kB │ gzip:  0.50 kB
dist/assets/reservationService-Cg5V356C.js                    0.76 kB │ gzip:  0.39 kB
dist/assets/SettingsPageHeader-Cdkl3dG8.js                    0.77 kB │ gzip:  0.42 kB
dist/assets/inventoryService-CdDdeaIp.js                      0.86 kB │ gzip:  0.37 kB
dist/assets/quotationService-DeAV7uzD.js                      0.97 kB │ gzip:  0.44 kB
dist/assets/M3Table-BclZLXJu.js                               1.01 kB │ gzip:  0.47 kB
dist/assets/M3SegmentedRow-DCpm4MMs.js                        1.31 kB │ gzip:  0.71 kB
dist/assets/billingService-CNiCAPFQ.js                        1.33 kB │ gzip:  0.56 kB
dist/assets/SettingsProfile-cc-Bkc8S.js                       1.33 kB │ gzip:  0.64 kB
dist/assets/dashboardService-CYQiOZhz.js                      1.34 kB │ gzip:  0.68 kB
dist/assets/Login-Bjhc-7A_.js                                 1.58 kB │ gzip:  0.82 kB
dist/assets/M3Dialog-CGezvdxc.js                              1.71 kB │ gzip:  0.81 kB
dist/assets/M3TextField-Cr9AS0t9.js                           1.84 kB │ gzip:  0.91 kB
dist/assets/SettingsAccessibility-BHmeLbnr.js                 2.46 kB │ gzip:  1.05 kB
dist/assets/Settings-Ch9G8uyZ.js                              2.47 kB │ gzip:  1.00 kB
dist/assets/SettingsPassword-BD3D3Urb.js                      3.02 kB │ gzip:  1.32 kB
dist/assets/SettingsAppearance-D59TxOym.js                    3.07 kB │ gzip:  1.36 kB
dist/assets/RoomSelection-CYSUHtqM.js                         3.62 kB │ gzip:  1.50 kB
dist/assets/CheckInForm-B6ajBYAq.js                           5.30 kB │ gzip:  2.18 kB
dist/assets/Dashboard-CEMbLzEA.js                             5.91 kB │ gzip:  2.10 kB
dist/assets/Housekeeping-BRlcJNQk.js                          6.04 kB │ gzip:  2.11 kB
dist/assets/OwnerDashboard-BdfBfDlL.js                        6.81 kB │ gzip:  2.09 kB
dist/assets/WalkInCheckInForm-DEZakJHY.js                     7.35 kB │ gzip:  2.47 kB
dist/assets/Quotations-db73qSW3.js                            7.72 kB │ gzip:  2.56 kB
dist/assets/SettingsSystem-CcYnpNKX.js                        7.88 kB │ gzip:  2.49 kB
dist/assets/HotelProfile-CiHujbYy.js                          8.11 kB │ gzip:  2.54 kB
dist/assets/ReservationForm-BBkv5i6B.js                       9.85 kB │ gzip:  3.36 kB
dist/assets/Reservations-B9FV0D4y.js                         10.43 kB │ gzip:  3.46 kB
dist/assets/authStore-D_EzjumG.js                            10.53 kB │ gzip:  4.24 kB
dist/assets/GuestFieldSection-BjWAHWmn.js                    10.76 kB │ gzip:  3.08 kB
dist/assets/QuotationForm-BFczth03.js                        11.28 kB │ gzip:  3.81 kB
dist/assets/QuotationDetail-CWFQmedA.js                      11.86 kB │ gzip:  3.17 kB
dist/assets/AdminUsers-DOtf6tiS.js                           13.15 kB │ gzip:  3.29 kB
dist/assets/RateCalendar-CyCbCU8D.js                         13.50 kB │ gzip:  4.54 kB
dist/assets/Stays-SJYERUc6.js                                14.00 kB │ gzip:  4.11 kB
dist/assets/Assistant-BucjOHfM.js                            15.87 kB │ gzip:  5.18 kB
dist/assets/Guests-Bz9KSILw.js                               16.07 kB │ gzip:  4.53 kB
dist/assets/Billing-BxSIxIv1.js                              17.74 kB │ gzip:  4.89 kB
dist/assets/Restaurant-BN2gtV6k.js                           21.71 kB │ gzip:  5.61 kB
dist/assets/Rooms-DOeQJdas.js                                25.46 kB │ gzip:  5.51 kB
dist/assets/it-ChNJib2b.js                                   32.13 kB │ gzip:  8.15 kB
dist/assets/schemas-Bo3zeGyq.js                              69.31 kB │ gzip: 18.48 kB
dist/assets/CalendarPlanning-qhJyw9Qo.js                    195.75 kB │ gzip: 57.97 kB
dist/assets/api-C5oH9PS7.js                                 206.61 kB │ gzip: 66.32 kB
dist/assets/index-CA4heHGp.js                               284.35 kB │ gzip: 90.49 kB

✓ built in 1.29s


```
