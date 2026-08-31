# AI-0074 Verification Result

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
3mclass[39m=[32m"text-on-surface-variant text-xs"[39m
            [36m>[39m
              [0minvoice_number[0m
            [36m</dt>[39m
            [36m<dd[39m
              [33mclass[39m=[32m"font-medium text-on-surface"[39m
            [36m>[39m
              [0mINV-001[0m
            [36m</dd>[39m
          [36m</div>[39m
          [36m<div>[39m
            [36m<dt[39m
              [33mclass[39m=[32m"text-on-surface-variant text-xs"[39m
            [36m>[39m
              [0missue_date[0m
            [36m</dt>[39m
            [36m<dd[39m
              [33mclass[39m=[32m"text-on-surface"[39m
            [36m>[39m
              [0m1/1/2026, 10:00:00 AM[0m
            [36m</dd>[39m
          [36m</div>[39m
          [36m<div>[39m
            [36m<dt[39m
              [33mclass[39m=[32m"text-on-surface-variant text-xs"[39m
            [36m>[39m
              [0mtotal_amount[0m
            [36m</dt>[39m
            [36m<dd[39m
              [33mclass[39m=[32m"font-semibold text-on-surface text-base"[39m
            [36m>[39m
              [0mMX$250.00[0m
            [36m</dd>[39m
          [36m</div>[39m
          [36m<div>[39m
            [36m<dt[39m
              [33mclass[39m=[32m"text-on-surface-variant text-xs"[39m
            [36m>[39m
              [0mstatus[0m
            [36m</dt>[39m
            [36m<dd[39m
              [33mclass[39m=[32m"mt-0.5"[39m
            [36m>[39m
              [36m<span>[39m
                [0minvoice_status_ISSUED[0m
              [36m</span>[39m
            [36m</dd>[39m
          [36m</div>[39m
        [36m</dl>[39m
        [36m<section[39m
          [33maria-labelledby[39m=[32m"payments-heading"[39m
        [36m>[39m
          [36m<h3[39m
            [33mclass[39m=[32m"text-xs font-medium text-on-surface-variant uppercase tracking-wide mb-2"[39m
            [33mid[39m=[32m"payments-heading"[39m
          [36m>[39m
            [0mpayments_history[0m
          [36m</h3>[39m
          [36m<p[39m
            [33mclass[39m=[32m"text-on-surface-variant italic"[39m
          [36m>[39m
            [0mno_payments_yet[0m
          [36m</p>[39m
        [36m</section>[39m
      [36m</div>[39m
      [36m<div[39m
        [33mclass[39m=[32m"flex justify-end pt-2 border-t border-outline-variant mt-4"[39m
      [36m>[39m
        [36m<button[39m
          [33mclass[39m=[32m"flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-on-primary text-sm font-medium hover:opacity-90 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary min-h-[40px]"[39m
          [33mtype[39m=[32m"button"[39m
        [36m>[39m
          [36m<span[39m
            [33maria-hidden[39m=[32m"true"[39m
            [33mclass[39m=[32m"material-symbols-outlined"[39m
            [33mstyle[39m=[32m"font-size: 18px;"[39m
          [36m>[39m
            [0mdownload[0m
          [36m</span>[39m
          [0mdownload_pdf[0m
        [36m</button>[39m
      [36m</div>[39m
    [36m</div>[39m
  [36m</div>[39m
[36m</body>[39m
[90m [2m❯[22m Object.getElementError node_modules/@testing-library/dom/dist/config.js:[2m37:19[22m[39m
[90m [2m❯[22m node_modules/@testing-library/dom/dist/query-helpers.js:[2m76:38[22m[39m
[90m [2m❯[22m node_modules/@testing-library/dom/dist/query-helpers.js:[2m52:17[22m[39m
[90m [2m❯[22m node_modules/@testing-library/dom/dist/query-helpers.js:[2m95:19[22m[39m
[36m [2m❯[22m src/pages/Billing/InvoiceDetailModal.test.tsx:[2m158:28[22m[39m
    [90m156|[39m     vi.mocked(billingService.validateFatturaPAXml).mockResolvedValueOn…
    [90m157|[39m     render(<InvoiceDetailModal invoice={BASE_INVOICE} onClose={onClose…
    [90m158|[39m     fireEvent.click(screen.getByRole('button', { name: /download_fattu…
    [90m   |[39m                            [31m^[39m
    [90m159|[39m
    [90m160|[39m     await waitFor(() => expect(billingService.validateFatturaPAXml).to…

[31m[2m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[27/28]⎯[22m[39m

[41m[1m FAIL [22m[49m src/pages/Billing/InvoiceDetailModal.test.tsx[2m > [22mInvoiceDetailModal[2m > [22mshows error toast and does not download when FatturaPA validation fails
[31m[1mTestingLibraryElementError[22m[39m: Unable to find an accessible element with the role "button" and name `/download_fattura_pa/i`

Here are the accessible roles:

  dialog:

  Name "invoice_detail_title":
  [36m<div[39m
    [33maria-label[39m=[32m"invoice_detail_title"[39m
    [33mrole[39m=[32m"dialog"[39m
  [36m/>[39m

  --------------------------------------------------
  term:

  Name "":
  [36m<dt[39m
    [33mclass[39m=[32m"text-on-surface-variant text-xs"[39m
  [36m/>[39m

  Name "":
  [36m<dt[39m
    [33mclass[39m=[32m"text-on-surface-variant text-xs"[39m
  [36m/>[39m

  Name "":
  [36m<dt[39m
    [33mclass[39m=[32m"text-on-surface-variant text-xs"[39m
  [36m/>[39m

  Name "":
  [36m<dt[39m
    [33mclass[39m=[32m"text-on-surface-variant text-xs"[39m
  [36m/>[39m

  --------------------------------------------------
  definition:

  Name "":
  [36m<dd[39m
    [33mclass[39m=[32m"font-medium text-on-surface"[39m
  [36m/>[39m

  Name "":
  [36m<dd[39m
    [33mclass[39m=[32m"text-on-surface"[39m
  [36m/>[39m

  Name "":
  [36m<dd[39m
    [33mclass[39m=[32m"font-semibold text-on-surface text-base"[39m
  [36m/>[39m

  Name "":
  [36m<dd[39m
    [33mclass[39m=[32m"mt-0.5"[39m
  [36m/>[39m

  --------------------------------------------------
  region:

  Name "payments_history":
  [36m<section[39m
    [33maria-labelledby[39m=[32m"payments-heading"[39m
  [36m/>[39m

  --------------------------------------------------
  heading:

  Name "payments_history":
  [36m<h3[39m
    [33mclass[39m=[32m"text-xs font-medium text-on-surface-variant uppercase tracking-wide mb-2"[39m
    [33mid[39m=[32m"payments-heading"[39m
  [36m/>[39m

  --------------------------------------------------
  paragraph:

  Name "":
  [36m<p[39m
    [33mclass[39m=[32m"text-on-surface-variant italic"[39m
  [36m/>[39m

  --------------------------------------------------
  button:

  Name "download_pdf":
  [36m<button[39m
    [33mclass[39m=[32m"flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-on-primary text-sm font-medium hover:opacity-90 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary min-h-[40px]"[39m
    [33mtype[39m=[32m"button"[39m
  [36m/>[39m

  --------------------------------------------------

Ignored nodes: comments, script, style
[36m<body>[39m
  [36m<div>[39m
    [36m<div[39m
      [33maria-label[39m=[32m"invoice_detail_title"[39m
      [33mrole[39m=[32m"dialog"[39m
    [36m>[39m
      [36m<div[39m
        [33mclass[39m=[32m"space-y-6 text-sm font-body"[39m
      [36m>[39m
        [36m<dl[39m
          [33mclass[39m=[32m"grid grid-cols-2 gap-x-6 gap-y-3"[39m
        [36m>[39m
          [36m<div>[39m
            [36m<dt[39m
              [33mclass[39m=[32m"text-on-surface-variant text-xs"[39m
            [36m>[39m
              [0minvoice_number[0m
            [36m</dt>[39m
            [36m<dd[39m
              [33mclass[39m=[32m"font-medium text-on-surface"[39m
            [36m>[39m
              [0mINV-001[0m
            [36m</dd>[39m
          [36m</div>[39m
          [36m<div>[39m
            [36m<dt[39m
              [33mclass[39m=[32m"text-on-surface-variant text-xs"[39m
            [36m>[39m
              [0missue_date[0m
            [36m</dt>[39m
            [36m<dd[39m
              [33mclass[39m=[32m"text-on-surface"[39m
            [36m>[39m
              [0m1/1/2026, 10:00:00 AM[0m
            [36m</dd>[39m
          [36m</div>[39m
          [36m<div>[39m
            [36m<dt[39m
              [33mclass[39m=[32m"text-on-surface-variant text-xs"[39m
            [36m>[39m
              [0mtotal_amount[0m
            [36m</dt>[39m
            [36m<dd[39m
              [33mclass[39m=[32m"font-semibold text-on-surface text-base"[39m
            [36m>[39m
              [0mMX$250.00[0m
            [36m</dd>[39m
          [36m</div>[39m
          [36m<div>[39m
            [36m<dt[39m
              [33mclass[39m=[32m"text-on-surface-variant text-xs"[39m
            [36m>[39m
              [0mstatus[0m
            [36m</dt>[39m
            [36m<dd[39m
              [33mclass[39m=[32m"mt-0.5"[39m
            [36m>[39m
              [36m<span>[39m
                [0minvoice_status_ISSUED[0m
              [36m</span>[39m
            [36m</dd>[39m
          [36m</div>[39m
        [36m</dl>[39m
        [36m<section[39m
          [33maria-labelledby[39m=[32m"payments-heading"[39m
        [36m>[39m
          [36m<h3[39m
            [33mclass[39m=[32m"text-xs font-medium text-on-surface-variant uppercase tracking-wide mb-2"[39m
            [33mid[39m=[32m"payments-heading"[39m
          [36m>[39m
            [0mpayments_history[0m
          [36m</h3>[39m
          [36m<p[39m
            [33mclass[39m=[32m"text-on-surface-variant italic"[39m
          [36m>[39m
            [0mno_payments_yet[0m
          [36m</p>[39m
        [36m</section>[39m
      [36m</div>[39m
      [36m<div[39m
        [33mclass[39m=[32m"flex justify-end pt-2 border-t border-outline-variant mt-4"[39m
      [36m>[39m
        [36m<button[39m
          [33mclass[39m=[32m"flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-on-primary text-sm font-medium hover:opacity-90 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary min-h-[40px]"[39m
          [33mtype[39m=[32m"button"[39m
        [36m>[39m
          [36m<span[39m
            [33maria-hidden[39m=[32m"true"[39m
            [33mclass[39m=[32m"material-symbols-outlined"[39m
            [33mstyle[39m=[32m"font-size: 18px;"[39m
          [36m>[39m
            [0mdownload[0m
          [36m</span>[39m
          [0mdownload_pdf[0m
        [36m</button>[39m
      [36m</div>[39m
    [36m</div>[39m
  [36m</div>[39m
[36m</body>[39m
[90m [2m❯[22m Object.getElementError node_modules/@testing-library/dom/dist/config.js:[2m37:19[22m[39m
[90m [2m❯[22m node_modules/@testing-library/dom/dist/query-helpers.js:[2m76:38[22m[39m
[90m [2m❯[22m node_modules/@testing-library/dom/dist/query-helpers.js:[2m52:17[22m[39m
[90m [2m❯[22m node_modules/@testing-library/dom/dist/query-helpers.js:[2m95:19[22m[39m
[36m [2m❯[22m src/pages/Billing/InvoiceDetailModal.test.tsx:[2m169:28[22m[39m
    [90m167|[39m     })[33m;[39m
    [90m168|[39m     render(<InvoiceDetailModal invoice={BASE_INVOICE} onClose={onClose…
    [90m169|[39m     fireEvent.click(screen.getByRole('button', { name: /download_fattu…
    [90m   |[39m                            [31m^[39m
    [90m170|[39m
    [90m171|[39m     [35mawait[39m [34mwaitFor[39m(() [33m=>[39m

[31m[2m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[28/28]⎯[22m[39m


[2m Test Files [22m [1m[31m5 failed[39m[22m[2m | [22m[1m[32m84 passed[39m[22m[90m (89)[39m
[2m      Tests [22m [1m[31m28 failed[39m[22m[2m | [22m[1m[32m869 passed[39m[22m[90m (897)[39m
[2m   Start at [22m 03:59:50
[2m   Duration [22m 50.48s[2m (transform 10.80s, setup 14.13s, import 96.71s, tests 113.26s, environment 100.86s)[22m

close timed out after 10000ms
Tests closed successfully but something prevents Vite server from exiting
You can try to identify the cause by enabling "hanging-process" reporter. See https://vitest.dev/guide/reporters.html#hanging-process-reporter


```
