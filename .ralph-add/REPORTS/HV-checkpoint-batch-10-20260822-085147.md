# Hypervelocity checkpoint batch-10

- Result: FAIL
- Workspace: isolated worktree

## Commands
```text
   ] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-08-22T08:54:29.355-06:00  INFO 29331 --- [guest-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-08-22T08:54:29.375-06:00  INFO 29331 --- [guest-service] [ionShutdownHook] [                                                 ] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
> Task :guest-service:test
> Task :internal-auth-lib:compileTestJava
> Task :internal-auth-lib:processTestResources NO-SOURCE
> Task :internal-auth-lib:testClasses
> Task :guest-service:jacocoTestReport
> Task :internal-auth-lib:test
> Task :guest-service:jacocoTestCoverageVerification
> Task :internal-auth-lib:jacocoTestReport
> Task :notification-service:compileJava
> Task :notification-service:processResources
> Task :notification-service:classes
> Task :internal-auth-lib:jacocoTestCoverageVerification
> Task :notification-service:compileTestJava
> Task :notification-service:processTestResources
> Task :notification-service:testClasses
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
> Task :notification-service:test
> Task :pdf-template-engine:compileTestJava
> Task :pdf-template-engine:processTestResources
> Task :pdf-template-engine:testClasses
> Task :notification-service:jacocoTestReport
> Task :pdf-template-engine:test
> Task :notification-service:jacocoTestCoverageVerification
> Task :pdf-template-engine:jacocoTestReport
> Task :pdf-template-engine:jacocoTestCoverageVerification

[Incubating] Problems report is available at: file:///Users/usuario/.ralph-hotel/hypervelocity-worktrees/checkpoint-batch-10-imjqmldn/build/reports/problems/problems-report.html

BUILD SUCCESSFUL in 3m 9s
75 actionable tasks: 75 executed
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.3.1/userguide/configuration_cache_enabling.html


$ npm --prefix frontend test
exit=1

      [33mclass[39m=[32m"fixed inset-0 z-50 flex items-center justify-center p-4"[39m
      [33mrole[39m=[32m"dialog"[39m
    [36m>[39m
      [36m<div[39m
        [33maria-hidden[39m=[32m"true"[39m
        [33mclass[39m=[32m"absolute inset-0 bg-scrim/40"[39m
      [36m/>[39m
      [36m<div[39m
        [33mclass[39m=[32m"relative w-full max-w-lg max-h-[90dvh] overflow-hidden flex flex-col bg-surface-container-high rounded-[28px] shadow-elevation-3"[39m
      [36m>[39m
        [36m<div[39m
          [33mclass[39m=[32m"flex items-center justify-between px-6 pt-6 pb-4"[39m
        [36m>[39m
          [36m<h2[39m
            [33mclass[39m=[32m"text-xl font-semibold font-display text-on-surface leading-tight"[39m
            [33mid[39m=[32m"guest-modal-title"[39m
          [36m>[39m
            [0medit_guest[0m
          [36m</h2>[39m
          [36m<button[39m
            [33maria-label[39m=[32m"close"[39m
            [33mclass[39m=[32m"flex items-center justify-center w-10 h-10 rounded-shape-full text-on-surface-variant hover:bg-surface-container-highest focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 transition-colors"[39m
            [33mtype[39m=[32m"button"[39m
          [36m>[39m
            [36m<span[39m
              [33maria-hidden[39m=[32m"true"[39m
              [33mclass[39m=[32m"material-symbols-outlined"[39m
              [33mstyle[39m=[32m"font-size: 20px; width: 20px; height: 20px;"[39m
            [36m>[39m
              [0mclose[0m
            [36m</span>[39m
          [36m</button>[39m
        [36m</div>[39m
        [36m<div[39m
          [33mclass[39m=[32m"h-px bg-outline-variant mx-6"[39m
        [36m/>[39m
        [36m<div[39m
          [33mclass[39m=[32m"flex-1 overflow-y-auto px-6 py-5"[39m
        [36m>[39m
          [36m<form[39m
            [33mclass[39m=[32m"space-y-4"[39m
            [33mid[39m=[32m"guest-form"[39m
            [33mnovalidate[39m=[32m""[39m
          [36m>[39m
            [36m<div[39m
              [33mclass[39m=[32m"grid grid-cols-2 gap-4"[39m
            [36m>[39m
              [36m<div>[39m
                [36m<label[39m
                  [33mclass[39m=[32m"block text-sm font-medium font-body text-on-surface-variant mb-1"[39m
                  [33mfor[39m=[32m"firstName"[39m
                [36m>[39m
                  [0mlabel_first_name[0m
                  [0m *[0m
                [36m</label>[39m
                [36m<input[39m
                  [33maria-invalid[39m=[32m"false"[39m
                  [33mclass[39m=[32m"block w-full rounded-shape-xs border border-outline px-3 py-2 text-sm font-body bg-transparent text-on-surface focus:border-primary focus:ring-1 focus:ring-primary focus:outline-none"[39m
                  [33mid[39m=[32m"firstName"[39m
                  [33mname[39m=[32m"firstName"[39m
                  [33mtype[39m=[32m"text"[39m
                  [33mvalue[39m=[32m"Mario"[39m
                [36m/>[39m
              [36m</div>[39m
              [36m<div>[39m
                [36m<label[39m
                  [33mclass[39m=[32m"block text-sm font-medium font-body text-on-surface-variant mb-1"[39m
                  [33mfor[39m=[32m"lastName"[39m
                [36m>[39m
                  [0mlabel_last_name[0m
                  [0m *[0m
                [36m</label>[39m
                [36m<input[39m
                  [33maria-invalid[39m=[32m"false"[39m
                  [33mclass[39m=[32m"block w-full rounded-shape-xs border border-outline px-3 py-2 text-sm font-body bg-transparent text-on-surface focus:border-primary focus:ring-1 focus:ring-primary focus:outline-none"[39m
                  [33mid[39m=[32m"lastName"[39m
                  [33mname[39m=[32m"lastName"[39m
                  [33mtype[39m=[32m"text"[39m
                  [33mvalue[39m=[32m"Rossi"[39m
                [36m/>[39m
              [36m</div>[39m
            [36m</div>[39m
            [36m<div>[39m
              [36m<label[39m
                [33mclass[39m=[32m"block text-sm font-medium font-body text-on-surface-variant mb-1"[39m
                [33mfor[39m=[32m"email"[39m
              [36m>[39m
                [0mlabel_email_hint[0m
                [0m *[0m
              [36m</label>[39m
              [36m<input[39m
                [33maria-invalid[39m=[32m"false"[39m
                [33mclass[39m=[32m"block w-full rounded-shape-xs border border-outline px-3 py-2 text-sm font-body bg-transparent text-on-surface focus:border-primary focus:ring-1 focus:ring-primary focus:outline-none"[39m
                [33mid[39m=[32m"email"[39m
                [33mname[39m=[32m"email"[39m
                [33mtype[39m=[32m"email"[39m
                [33mvalue[39m=[32m"mario@test.com"[39m
              [36m/>[39m
            [36m</div>[39m
            [36m<div>[39m
              [36m<label[39m
                [33mclass[39m=[32m"block text-sm font-medium font-body text-on-surface-variant mb-1"[39m
                [33mfor[39m=[32m"phonePrefix"[39m
              [36m>[39m
                [0mlabel_phone_hint[0m
                [0m *[0m
              [36m</label>[39m
              [36m<div[39m
                [33mclass[39m=[32m"flex gap-2"[39m
              [36m>[39m
                [36m<select[39m
                  [33mclass[39m=[32m"shrink-0 w-24 rounded-shape-xs border border-outline px-2 py-2 text-sm font-body bg-transparent text-on-surface focus:border-primary focus:ring-1 focus:ring-primary focus:outline-none"[39m
                  [33mid[39m=[32m"phonePrefix"[39m
                [36m>[39m
                  [36m<option[39m
                    [33mvalue[39m=[32m"+52"[39m
                  [36m>[39m
                    [0m🇲🇽 +52[0m
                  [36m</option>[39m
                  [36m<option[39m
                    [33mvalue[39m=[32m"+39"[39m
                  [36m>[39m
                    [0m🇮🇹 +39[0m
                  [36m</option>[39m
                  [36m<option[39m
                    [33mvalue[39m=[32m"+44"[39m
                  [36m>[39m
                    [0m🇬🇧 +44[0m
                  [36m</option>[39m
                  [36m<option[39m
                    [33mvalue[39m=[32m"+1"[39m
                  [36m>[39m
                    [0m🇺🇸 +1[0m
                  [36m</option>[39m
                  [36m<option[39m
                    [33mvalue[39m=[32m"+49"[39m
                  [36m>[39m
                    [0m🇩🇪 +49[0m
                  [36m</option>[39m
                  [36m<option[39m
                    [33mvalue[39m=[32m"+33"[39m
                  [36m>[39m
                    [0m🇫🇷 +33[0m
                  [36m</option>[39m
                  [36m<option[39m
                    [33mvalue[39m=[32m"+34"[39m
                  [36m>[39m
                    [0m🇪🇸 +34[0m
                  [36m</option>[39m
                  [36m<option[39m
                    [33mvalue[39m=[32m"+41"[39m
                  [36m>[39m
                    [0m🇨🇭 +41[0m
                  [36m</option>[39m
                [36m</select>[39m
                [36m<input[39m
                  [33maria-label[39m=[32m"label_phone_number"[39m
                  [33mclass[39m=[32m"block w-full rounded-shape-xs border border-outline px-3 py-2 text-sm font-body bg-transparent text-on-surface focus:border-primary focus:ring-1 focus:ring-primary focus:outline-none flex-1"[39m
                  [33mid[39m=[32m"phoneNumber"[39m
                  [33mplaceholder[39m=[32m"Ej. 81 1234 5678"[39m
                  [33mtype[39m=[32m"text"[39m
                  [33mvalue[39m=[32m""[39m
                [36m/>[39m
              [36m</div>[39m
            [36m</div>[39m
            [36m<div[39m
              [33mclass[39m=[32m"grid grid-cols-2 gap-4"[39m
            [36m>[39m
              [36m<div>[39m
                [36m<label[39m
                  [33mclass[39m=[32m"block text-sm font-medium font-body text-on-surface-variant mb-1"[39m
                  [33mfor[39m=[32m"city"[39m
                [36m>[39m
                  [0mcity[0m
                [36m</label>[39m
                [36m<input[39m
                  [33mclass[39m=[32m"block w-full rounded-shape-xs border border-outline px-3 py-2 text-sm font-body bg-transparent text-on-surface focus:border-primary focus:ring-1 focus:ring-primary focus:outline-none"[39m
                  [33mid[39m=[32m"city"[39m
                  [33mname[39m=[32m"city"[39m
                  [33mtype[39m=[32m"text"[39m
                  [33mvalue[39m=[32m""[39m
                [36m/>[39m
              [36m</div>[39m
              [36m<div>[39m
                [36m<label[39m
                  [33mclass[39m=[32m"block text-sm font-medium font-body text-on-surface-variant mb-1"[39m
                  [33mfor[39m=[32m"country"[39m
                [36m>[39m
                  [0mlabel_country[0m
                [36m</label>[39m
                [36m<input[39m
                  [33mclass[39m=[32m"block w-full rounded-shape-xs border border-outline px-3 py-2 text-sm font-body bg-transparent text-on-surface focus:border-primary focus:ring-1 focus:ring-primary focus:outline-none"[39m
                  [33mid[39m=[32m"country"[39m
                  [33mname[39m=[32m"country"[39m
                  [33mtype[39m=[32m"text"[39m
                  [33mvalue[39m=[32m"MX"[39m
                [36m/>[39m
              [36m</div>[39m
            [36m</div>[39m
            [36m<div[39m
              [33mclass[39m=[32m"border border-outline-variant/40 rounded-shape-xs"[39m
            [36m>[39m
              [36m<button[39m
                [33maria-controls[39m=[32m"fiscal-section"[39m
                [33maria-expanded[39m=[32m"false"[39m
                [33mclass[39m=[32m"w-full flex items-center justify-between px-3 py-2 text-sm font-medium font-body text-on-surface-variant hover:bg-surface-container-highest/50 rounded-shape-xs transition-colors focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-1 focus-visible:outline-none"[39m
                [33mtype[39m=[32m"button"[39m
              [36m>[39m
                [36m<span>[39m
                  [0msection_fiscal_data[0m
                [36m</span>[39m
                [36m<span[39m
                  [33maria-hidden[39m=[32m"true"[39m
                  [33mclass[39m=[32m"material-symbols-outlined"[39m
                  [33mstyle[39m=[32m"font-size: 18px; width: 18px; height: 18px;"[39m
                [36m>[39m
                  [0mexpand_more[0m
                [36m</span>[39m
              [36m</button>[39m
            [36m</div>[39m
          [36m</form>[39m
        [36m</div>[39m
        [36m<div[39m
          [33mclass[39m=[32m"h-px bg-outline-variant mx-6"[39m
        [36m/>[39m
        [36m<div[39m
          [33mclass[39m=[32m"px-6 py-4"[39m
        [36m>[39m
          [36m<div[39m
            [33mclass[39m=[32m"flex justify-between items-center"[39m
          [36m>[39m
            [36m<div>[39m
              [36m<button[39m
                [33mclass[39m=[32m"inline-flex items-center justify-center gap-2 px-6 h-10 rounded-shape-full text-sm font-medium font-body transition-all
        text-primary bg-transparent hover:bg-primary/[0.08] focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2
        
        text-error hover:bg-error-container/20"[39m
              [36m>[39m
                [0mdelete[0m
              [36m</button>[39m
            [36m</div>[39m
            [36m<div[39m
              [33mclass[39m=[32m"flex gap-2"[39m
            [36m>[39m
              [36m<button[39m
                [33mclass[39m=[32m"inline-flex items-center justify-center gap-2 px-6 h-10 rounded-shape-full text-sm font-medium font-body transition-all
        text-primary bg-transparent hover:bg-primary/[0.08] focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2
        
        "[39m
              [36m>[39m
                [0mcancel[0m
              [36m</button>[39m
              [36m<button[39m
                [33mclass[39m=[32m"inline-flex items-center justify-center gap-2 px-6 h-10 rounded-shape-full text-sm font-medium font-body transition-all
        bg-primary text-on-primary hover:shadow-elevation-1 active:shadow-elevation-0 focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2
        
        "[39m
                [33mform[39m=[32m"guest-form"[39m
                [33mtype[39m=[32m"submit"[39m
              [36m>[39m
                [0msave[0m
              [36m</button>[39m
            [36m</div>[39m
          [36m</div>[39m
        [36m</div>[39m
      [36m</div>[39m
    [36m</div>[39m
  [36m</div>[39m
[36m</body>[39m
[90m [2m❯[22m Object.getElementError ../../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/dom/dist/config.js:[2m37:19[22m[39m
[90m [2m❯[22m getAllByLabelText ../../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/dom/dist/queries/label-text.js:[2m111:38[22m[39m
[90m [2m❯[22m ../../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/dom/dist/query-helpers.js:[2m52:17[22m[39m
[90m [2m❯[22m ../../../../Desktop/HOTEL-PMS/frontend/node_modules/@testing-library/dom/dist/query-helpers.js:[2m95:19[22m[39m
[36m [2m❯[22m src/pages/GuestFormModal.test.tsx:[2m137:21[22m[39m
    [90m135|[39m     [34mit[39m([32m'is expanded by default when guest has fiscal data'[39m[33m,[39m () [33m=>[39m {
    [90m136|[39m       render(<GuestFormModal guest={GUEST_WITH_FISCAL} onClose={vi.fn(…
    [90m137|[39m       expect(screen.getByLabelText(/label_fiscal_code/i)).toBeInTheDoc…
    [90m   |[39m                     [31m^[39m
    [90m138|[39m       expect((screen.getByLabelText(/label_fiscal_code/i) as HTMLInput…
    [90m139|[39m     })[33m;[39m

[31m[2m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[4/5]⎯[22m[39m

[41m[1m FAIL [22m[49m src/pages/Stays/CheckInForm.test.tsx[2m > [22mCheckInForm[2m > [22mshould have no accessibility violations
[31m[1mError[22m: Test timed out in 5000ms.
If this is a long-running test, pass a timeout value as the last argument or configure it globally with "testTimeout".[39m
[36m [2m❯[22m src/pages/Stays/CheckInForm.test.tsx:[2m95:3[22m[39m
    [90m 93|[39m   })[33m;[39m
    [90m 94|[39m
    [90m 95|[39m   [34mit[39m([32m'should have no accessibility violations'[39m[33m,[39m [35masync[39m () [33m=>[39m {
    [90m   |[39m   [31m^[39m
    [90m 96|[39m     [35mconst[39m { container } [33m=[39m [34mrenderComponent[39m([34m1[39m)[33m;[39m
    [90m 97|[39m     await waitFor(() => expect(screen.getByText('checkin_title')).toBe…

[31m[2m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[5/5]⎯[22m[39m


[2m Test Files [22m [1m[31m4 failed[39m[22m[2m | [22m[1m[32m85 passed[39m[22m[90m (89)[39m
[2m      Tests [22m [1m[31m5 failed[39m[22m[2m | [22m[1m[32m892 passed[39m[22m[90m (897)[39m
[2m   Start at [22m 08:55:06
[2m   Duration [22m 100.63s[2m (transform 19.16s, setup 25.87s, import 185.79s, tests 239.03s, environment 198.43s)[22m

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
dist/assets/index-BXlIQohX.css                               51.18 kB │ gzip:  9.40 kB
dist/assets/errorMessage-eCQNYHno.js                          0.17 kB │ gzip:  0.16 kB
dist/assets/passwordPolicy-B-6Lzf6f.js                        0.21 kB │ gzip:  0.15 kB
dist/assets/M3Card-CGtAqvSy.js                                0.43 kB │ gzip:  0.26 kB
dist/assets/userService-DCStX85W.js                           0.44 kB │ gzip:  0.23 kB
dist/assets/rateSeasonService-C5nQSYDe.js                     0.48 kB │ gzip:  0.27 kB
dist/assets/guestService-798lX1i0.js                          0.60 kB │ gzip:  0.34 kB
dist/assets/M3StatusChip-Crni-VQL.js                          0.60 kB │ gzip:  0.35 kB
dist/assets/fbService-CxqzHT0e.js                             0.61 kB │ gzip:  0.28 kB
dist/assets/PasswordVisibilityToggle-AVlljMw9.js              0.62 kB │ gzip:  0.39 kB
dist/assets/M3TableActionLink-n7xQBw27.js                     0.64 kB │ gzip:  0.36 kB
dist/assets/billingReportService-D-RR_Y9Z.js                  0.73 kB │ gzip:  0.50 kB
dist/assets/reservationService-Cg5V356C.js                    0.76 kB │ gzip:  0.39 kB
dist/assets/SettingsPageHeader-BUZSRpsa.js                    0.77 kB │ gzip:  0.42 kB
dist/assets/inventoryService-CdDdeaIp.js                      0.86 kB │ gzip:  0.37 kB
dist/assets/quotationService-DeAV7uzD.js                      0.97 kB │ gzip:  0.44 kB
dist/assets/M3Table-BclZLXJu.js                               1.01 kB │ gzip:  0.47 kB
dist/assets/M3SegmentedRow-UW5PeTtF.js                        1.31 kB │ gzip:  0.71 kB
dist/assets/billingService-CNiCAPFQ.js                        1.33 kB │ gzip:  0.56 kB
dist/assets/SettingsProfile-Bxig_gZY.js                       1.33 kB │ gzip:  0.64 kB
dist/assets/dashboardService-CMssjC3Z.js                      1.34 kB │ gzip:  0.68 kB
dist/assets/Login-dibxfxfL.js                                 1.58 kB │ gzip:  0.82 kB
dist/assets/M3Dialog-BVC3Epat.js                              1.71 kB │ gzip:  0.81 kB
dist/assets/M3TextField-CvsZYmie.js                           1.84 kB │ gzip:  0.91 kB
dist/assets/SettingsAccessibility-3gb5fzid.js                 2.46 kB │ gzip:  1.05 kB
dist/assets/Settings-DxTouz6x.js                              2.47 kB │ gzip:  1.00 kB
dist/assets/SettingsPassword-CXrrePpO.js                      3.02 kB │ gzip:  1.32 kB
dist/assets/SettingsAppearance-VIUMRyOp.js                    3.07 kB │ gzip:  1.36 kB
dist/assets/RoomSelection-C-mi2QC9.js                         3.62 kB │ gzip:  1.50 kB
dist/assets/CheckInForm-D5LTG8aR.js                           5.30 kB │ gzip:  2.18 kB
dist/assets/Dashboard-CR5s7k7L.js                             5.91 kB │ gzip:  2.10 kB
dist/assets/Housekeeping-C0UDikut.js                          6.04 kB │ gzip:  2.11 kB
dist/assets/OwnerDashboard-C-pVafAR.js                        6.81 kB │ gzip:  2.09 kB
dist/assets/WalkInCheckInForm-CyPqYifs.js                     7.35 kB │ gzip:  2.47 kB
dist/assets/Quotations-D6YlEKI7.js                            7.72 kB │ gzip:  2.56 kB
dist/assets/SettingsSystem-CP9KrTSY.js                        7.88 kB │ gzip:  2.49 kB
dist/assets/HotelProfile-Dsan0lwy.js                          8.36 kB │ gzip:  2.61 kB
dist/assets/ReservationForm-BHl_Vwc2.js                       9.85 kB │ gzip:  3.35 kB
dist/assets/Reservations---Og5GXN.js                         10.43 kB │ gzip:  3.46 kB
dist/assets/authStore-D_EzjumG.js                            10.53 kB │ gzip:  4.24 kB
dist/assets/GuestFieldSection-BHCf3Xr8.js                    10.76 kB │ gzip:  3.07 kB
dist/assets/QuotationForm-BqsLDu_j.js                        11.28 kB │ gzip:  3.81 kB
dist/assets/QuotationDetail-qlEd_32F.js                      11.86 kB │ gzip:  3.17 kB
dist/assets/AdminUsers-CnvTWbjv.js                           13.15 kB │ gzip:  3.29 kB
dist/assets/RateCalendar-1ayFwJFx.js                         13.50 kB │ gzip:  4.54 kB
dist/assets/Stays-BX0vmp8j.js                                14.00 kB │ gzip:  4.11 kB
dist/assets/Assistant-C4Ve1u0t.js                            15.87 kB │ gzip:  5.18 kB
dist/assets/Guests-Celwachj.js                               16.07 kB │ gzip:  4.53 kB
dist/assets/Billing-Bop-1ll2.js                              17.74 kB │ gzip:  4.89 kB
dist/assets/Restaurant-DFy7gu1u.js                           21.71 kB │ gzip:  5.61 kB
dist/assets/Rooms-DXeRKhkP.js                                25.46 kB │ gzip:  5.51 kB
dist/assets/it-ChNJib2b.js                                   32.13 kB │ gzip:  8.15 kB
dist/assets/schemas-Bo3zeGyq.js                              69.31 kB │ gzip: 18.48 kB
dist/assets/CalendarPlanning-Bi1Qzkzt.js                    195.75 kB │ gzip: 57.97 kB
dist/assets/api-C5oH9PS7.js                                 206.61 kB │ gzip: 66.32 kB
dist/assets/index-Tw1v8bwa.js                               284.35 kB │ gzip: 90.47 kB

✓ built in 1.47s


```
