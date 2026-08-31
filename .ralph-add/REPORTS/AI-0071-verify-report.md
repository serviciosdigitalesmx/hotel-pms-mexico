# AI-0071 Verification Result

- Result: FAIL
- Source/workspace mutation: none detected
- Commands:
  - `./gradlew test`
  - `npm --prefix frontend test`

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
ling-service:processResources UP-TO-DATE
> Task :billing-service:classes UP-TO-DATE
> Task :billing-service:compileTestJava UP-TO-DATE
> Task :billing-service:processTestResources UP-TO-DATE
> Task :billing-service:testClasses UP-TO-DATE
> Task :pdf-template-engine:processResources UP-TO-DATE
> Task :pdf-template-engine:classes UP-TO-DATE
> Task :pdf-template-engine:jar UP-TO-DATE
> Task :billing-service:test UP-TO-DATE
> Task :billing-service:jacocoTestReport UP-TO-DATE
> Task :billing-service:jacocoTestCoverageVerification UP-TO-DATE
> Task :common-web-lib:compileTestJava UP-TO-DATE
> Task :common-web-lib:processTestResources NO-SOURCE
> Task :common-web-lib:testClasses UP-TO-DATE
> Task :common-web-lib:test UP-TO-DATE
> Task :common-web-lib:jacocoTestReport UP-TO-DATE
> Task :common-web-lib:jacocoTestCoverageVerification UP-TO-DATE
> Task :config-service:compileJava UP-TO-DATE
> Task :config-service:processResources UP-TO-DATE
> Task :config-service:classes UP-TO-DATE
> Task :config-service:compileTestJava UP-TO-DATE
> Task :config-service:processTestResources NO-SOURCE
> Task :config-service:testClasses UP-TO-DATE
> Task :config-service:test UP-TO-DATE
> Task :config-service:jacocoTestReport UP-TO-DATE
> Task :config-service:jacocoTestCoverageVerification UP-TO-DATE
> Task :fb-service:compileJava UP-TO-DATE
> Task :fb-service:processResources UP-TO-DATE
> Task :fb-service:classes UP-TO-DATE
> Task :fb-service:compileTestJava UP-TO-DATE
> Task :fb-service:processTestResources UP-TO-DATE
> Task :fb-service:testClasses UP-TO-DATE
> Task :fb-service:test UP-TO-DATE
> Task :fb-service:jacocoTestReport UP-TO-DATE
> Task :fb-service:jacocoTestCoverageVerification UP-TO-DATE
> Task :frontdesk-service:compileJava UP-TO-DATE
> Task :frontdesk-service:processResources UP-TO-DATE
> Task :frontdesk-service:classes UP-TO-DATE
> Task :frontdesk-service:compileTestJava UP-TO-DATE
> Task :frontdesk-service:processTestResources UP-TO-DATE
> Task :frontdesk-service:testClasses UP-TO-DATE
> Task :frontdesk-service:test UP-TO-DATE
> Task :frontdesk-service:jacocoTestReport UP-TO-DATE
> Task :frontdesk-service:jacocoTestCoverageVerification UP-TO-DATE
> Task :guest-service:compileJava UP-TO-DATE
> Task :guest-service:processResources UP-TO-DATE
> Task :guest-service:classes UP-TO-DATE
> Task :guest-service:compileTestJava UP-TO-DATE
> Task :guest-service:processTestResources UP-TO-DATE
> Task :guest-service:testClasses UP-TO-DATE
> Task :guest-service:test UP-TO-DATE
> Task :guest-service:jacocoTestReport UP-TO-DATE
> Task :guest-service:jacocoTestCoverageVerification UP-TO-DATE
> Task :internal-auth-lib:compileTestJava UP-TO-DATE
> Task :internal-auth-lib:processTestResources NO-SOURCE
> Task :internal-auth-lib:testClasses UP-TO-DATE
> Task :internal-auth-lib:test UP-TO-DATE
> Task :internal-auth-lib:jacocoTestReport UP-TO-DATE
> Task :internal-auth-lib:jacocoTestCoverageVerification UP-TO-DATE
> Task :notification-service:compileJava UP-TO-DATE
> Task :notification-service:processResources UP-TO-DATE
> Task :notification-service:classes UP-TO-DATE
> Task :notification-service:compileTestJava UP-TO-DATE
> Task :notification-service:processTestResources UP-TO-DATE
> Task :notification-service:testClasses UP-TO-DATE
> Task :notification-service:test UP-TO-DATE
> Task :notification-service:jacocoTestReport UP-TO-DATE
> Task :notification-service:jacocoTestCoverageVerification UP-TO-DATE
> Task :pdf-template-engine:compileTestJava UP-TO-DATE
> Task :pdf-template-engine:processTestResources UP-TO-DATE
> Task :pdf-template-engine:testClasses UP-TO-DATE
> Task :pdf-template-engine:test UP-TO-DATE
> Task :pdf-template-engine:jacocoTestReport UP-TO-DATE
> Task :pdf-template-engine:jacocoTestCoverageVerification UP-TO-DATE

BUILD SUCCESSFUL in 3s
75 actionable tasks: 75 up-to-date
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.3.1/userguide/configuration_cache_enabling.html


$ npm --prefix frontend test
exit=1
mclass[39m=[32m"text-lg font-medium text-on-surface"[39m
            [36m>[39m
              [0mstep_recipient[0m
            [36m</h2>[39m
          [36m</div>[39m
          [36m<div[39m
            [33maria-label[39m=[32m"heading_recipient"[39m
            [33mclass[39m=[32m"flex gap-2"[39m
            [33mrole[39m=[32m"group"[39m
          [36m>[39m
            [36m<button[39m
              [33mclass[39m=[32m"inline-flex items-center justify-center gap-2 px-6 h-10 rounded-shape-full text-sm font-medium font-body transition-all
        bg-primary text-on-primary hover:shadow-elevation-1 active:shadow-elevation-0 focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2
        
        "[39m
              [33mtype[39m=[32m"button"[39m
            [36m>[39m
              [0mtoggle_existing_guest[0m
            [36m</button>[39m
            [36m<button[39m
              [33mclass[39m=[32m"inline-flex items-center justify-center gap-2 px-6 h-10 rounded-shape-full text-sm font-medium font-body transition-all
        border border-outline text-primary bg-transparent hover:bg-primary/[0.08] focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2
        
        "[39m
              [33mtype[39m=[32m"button"[39m
            [36m>[39m
              [0mtoggle_new_prospect[0m
            [36m</button>[39m
          [36m</div>[39m
          [36m<div[39m
            [33mclass[39m=[32m"space-y-2"[39m
          [36m>[39m
            [36m<div[39m
              [33mclass[39m=[32m"relative "[39m
            [36m>[39m
              [36m<div[39m
                [33mclass[39m=[32m"relative flex items-center rounded-shape-xs border transition-all
          border-outline hover:border-on-surface
        "[39m
              [36m>[39m
                [36m<span[39m
                  [33mclass[39m=[32m"material-symbols-outlined pl-3 text-on-surface-variant"[39m
                  [33mstyle[39m=[32m"font-size: 20px;"[39m
                [36m>[39m
                  [0msearch[0m
                [36m</span>[39m
                [36m<input[39m
                  [33maria-invalid[39m=[32m"false"[39m
                  [33mclass[39m=[32m"peer w-full bg-transparent px-4 pt-5 pb-1.5 text-sm font-body text-on-surface placeholder-transparent
            focus:outline-none pl-2"[39m
                  [33mid[39m=[32m"_r_4g_"[39m
                  [33mplaceholder[39m=[32m"guests:search_guest_placeholder"[39m
                  [33mvalue[39m=[32m""[39m
                [36m/>[39m
                [36m<label[39m
                  [33mclass[39m=[32m"absolute transition-all duration-150 pointer-events-none font-body
            left-10
            top-1/2 -translate-y-1/2 text-sm
            text-on-surface-variant
          "[39m
                  [33mfor[39m=[32m"_r_4g_"[39m
                [36m>[39m
                  [0mguests:search_guest_placeholder[0m
                [36m</label>[39m
              [36m</div>[39m
            [36m</div>[39m
          [36m</div>[39m
        [36m</div>[39m
        [36m<div[39m
          [33mclass[39m=[32m"bg-surface shadow-elevation-1 rounded-shape-md p-6 space-y-4"[39m
        [36m>[39m
          [36m<div[39m
            [33mclass[39m=[32m"flex items-center justify-between gap-2 mb-2"[39m
          [36m>[39m
            [36m<div[39m
              [33mclass[39m=[32m"flex items-center gap-2"[39m
            [36m>[39m
              [36m<span[39m
                [33maria-hidden[39m=[32m"true"[39m
                [33mclass[39m=[32m"material-symbols-outlined text-primary"[39m
              [36m>[39m
                [0mevent_seat[0m
              [36m</span>[39m
              [36m<h2[39m
                [33mclass[39m=[32m"text-lg font-medium text-on-surface"[39m
              [36m>[39m
                [0mstep_stay_details[0m
              [36m</h2>[39m
            [36m</div>[39m
          [36m</div>[39m
          [36m<div[39m
            [33maria-label[39m=[32m"label_options"[39m
            [33mclass[39m=[32m"flex flex-wrap items-center gap-2"[39m
            [33mrole[39m=[32m"group"[39m
          [36m>[39m
            [36m<div[39m
              [33mclass[39m=[32m"flex items-center rounded-shape-full border border-primary bg-primary-container"[39m
            [36m>[39m
              [36m<button[39m
                [33maria-pressed[39m=[32m"true"[39m
                [33mclass[39m=[32m"px-4 py-2 text-sm font-medium font-body rounded-shape-full focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary text-on-primary-container"[39m
                [33mtype[39m=[32m"button"[39m
              [36m>[39m
                [0mOpción 1[0m
                [0m · [0m
                [0mMX$300.00[0m
              [36m</button>[39m
            [36m</div>[39m
            [36m<button[39m
              [33mclass[39m=[32m"inline-flex items-center justify-center gap-2 px-6 h-10 rounded-shape-full text-sm font-medium font-body transition-all
        text-primary bg-transparent hover:bg-primary/[0.08] focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2
        
        "[39m
              [33mtype[39m=[32m"button"[39m
            [36m>[39m
              [36m<span[39m
                [33maria-hidden[39m=[32m"true"[39m
                [33mclass[39m=[32m"material-symbols-outlined"[39m
                [33mstyle[39m=[32m"font-size: 18px; width: 18px; height: 18px;"[39m
              [36m>[39m
                [0madd[0m
              [36m</span>[39m
              [0maction_add_option[0m
            [36m</button>[39m
          [36m</div>[39m
          [36m<div[39m
            [33mclass[39m=[32m"relative max-w-xs"[39m
          [36m>[39m
            [36m<div[39m
              [33mclass[39m=[32m"relative flex items-center rounded-shape-xs border transition-all
          border-outline hover:border-on-surface
        "[39m
            [36m>[39m
              [36m<input[39m
                [33maria-invalid[39m=[32m"false"[39m
                [33mclass[39m=[32m"peer w-full bg-transparent px-4 pt-5 pb-1.5 text-sm font-body text-on-surface placeholder-transparent
            focus:outline-none "[39m
                [33mid[39m=[32m"_r_4h_"[39m
                [33mplaceholder[39m=[32m"label_option_name"[39m
                [33mvalue[39m=[32m"Opción 1"[39m
              [36m/>[39m
              [36m<label[39m
                [33mclass[39m=[32m"absolute transition-all duration-150 pointer-events-none font-body
            left-4
            top-1 text-xs
            text-on-surface-variant
          "[39m
                [33mfor[39m=[32m"_r_4h_"[39m
              [36m>[39m
                [0mlabel_option_name[0m
              [36m</label>[39m
            [36m</div>[39m
          [36m</div>[39m
          [36m<div[39m
            [33mdata-testid[39m=[32m"room-mock"[39m
          [36m>[39m
            [36m<label[39m
              [33mfor[39m=[32m"mock-checkin"[39m
            [36m>[39m
              [0mMock Check-in[0m
            [36m</label>[39m
            [36m<input[39m
              [33mid[39m=[32m"mock-checkin"[39m
            [36m/>[39m
            [36m<label[39m
              [33mfor[39m=[32m"mock-checkout"[39m
            [36m>[39m
              [0mMock Check-out[0m
            [36m</label>[39m
            [36m<input[39m
              [33mid[39m=[32m"mock-checkout"[39m
            [36m/>[39m
            [36m<button[39m
              [33mtype[39m=[32m"button"[39m
            [36m>[39m
              [0mToggle Room r1[0m
            [36m</button>[39m
            [36m<span>[39m
              [0mSelected: [0m
              [0mr1[0m
            [36m</span>[39m
          [36m</div>[39m
          [36m<div[39m
            [33mclass[39m=[32m"relative max-w-xs"[39m
          [36m>[39m
            [36m<div[39m
              [33mclass[39m=[32m"relative flex items-center rounded-shape-xs border transition-all
          border-outline hover:border-on-surface
        "[39m
            [36m>[39m
              [36m<input[39m
                [33maria-invalid[39m=[32m"false"[39m
                [33mclass[39m=[32m"peer w-full bg-transparent px-4 pt-5 pb-1.5 text-sm font-body text-on-surface placeholder-transparent
            focus:outline-none "[39m
                [33mid[39m=[32m"_r_4i_"[39m
                [33mplaceholder[39m=[32m"label_valid_until"[39m
                [33mrequired[39m=[32m""[39m
                [33mtype[39m=[32m"date"[39m
                [33mvalue[39m=[32m"2026-08-29"[39m
              [36m/>[39m
              [36m<label[39m
                [33mclass[39m=[32m"absolute transition-all duration-150 pointer-events-none font-body
            left-4
            top-1 text-xs
            text-on-surface-variant
          "[39m
                [33mfor[39m=[32m"_r_4i_"[39m
              [36m>[39m
                [0mlabel_valid_until[0m
              [36m</label>[39m
            [36m</div>[39m
          [36m</div>[39m
          [36m<p[39m
            [33mclass[39m=[32m"text-sm font-medium text-on-surface"[39m
          [36m>[39m
            [0mquotation_total:MX$300.00[0m
          [36m</p>[39m
        [36m</div>[39m
        [36m<div[39m
          [33mclass[39m=[32m"flex justify-end pt-4 gap-3"[39m
        [36m>[39m
          [36m<button[39m
            [33mclass[39m=[32m"inline-flex items-center justify-center gap-2 px-6 h-10 rounded-shape-full text-sm font-medium font-body transition-all
        text-primary bg-transparent hover:bg-primary/[0.08] focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2
        
        "[39m
            [33mtype[39m=[32m"button"[39m
          [36m>[39m
            [0mcommon:cancel[0m
          [36m</button>[39m
          [36m<button[39m
            [33mclass[39m=[32m"inline-flex items-center justify-center gap-2 px-6 h-10 rounded-shape-full text-sm font-medium font-body transition-all
        bg-primary text-on-primary hover:shadow-elevation-1 active:shadow-elevation-0 focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2
        
        "[39m
            [33mtype[39m=[32m"submit"[39m
          [36m>[39m
            [0mcommon:save[0m
          [36m</button>[39m
        [36m</div>[39m
      [36m</form>[39m
    [36m</div>[39m
  [36m</body>[39m
[36m</html>[39m
[90m [2m❯[22m Proxy.waitForWrapper node_modules/@testing-library/dom/dist/wait-for.js:[2m163:27[22m[39m
[36m [2m❯[22m src/pages/Quotations/QuotationForm.test.tsx:[2m388:11[22m[39m
    [90m386|[39m     fireEvent[33m.[39m[34mclick[39m(screen[33m.[39m[34mgetByText[39m([32m'Toggle Room r1'[39m))[33m;[39m
    [90m387|[39m
    [90m388|[39m     await waitFor(() => expect(screen.getByText('quotation_total:€ 300…
    [90m   |[39m           [31m^[39m
    [90m389|[39m   })[33m;[39m
    [90m390|[39m

[31m[2m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[37/37]⎯[22m[39m


[2m Test Files [22m [1m[31m7 failed[39m[22m[2m | [22m[1m[32m82 passed[39m[22m[90m (89)[39m
[2m      Tests [22m [1m[31m37 failed[39m[22m[2m | [22m[1m[32m860 passed[39m[22m[90m (897)[39m
[2m   Start at [22m 03:40:36
[2m   Duration [22m 64.99s[2m (transform 9.72s, setup 16.52s, import 118.63s, tests 161.41s, environment 122.65s)[22m

close timed out after 10000ms
Tests closed successfully but something prevents Vite server from exiting
You can try to identify the cause by enabling "hanging-process" reporter. See https://vitest.dev/guide/reporters.html#hanging-process-reporter


```
