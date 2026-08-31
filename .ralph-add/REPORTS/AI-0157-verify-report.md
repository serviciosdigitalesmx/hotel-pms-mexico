# AI-0157 Verification Result

- Result: FAIL
- Hypervelocity lane: isolated verification worktree

## Verification log
```text
$ npm --prefix frontend exec -- vitest run --root frontend src --pool=threads --maxWorkers=1 --reporter=dot
exit=1
7|[39m
    [90m128|[39m       fireEvent[33m.[39m[34mclick[39m(toggle)[33m;[39m
    [90m129|[39m       expect(screen.getByLabelText(/label_fiscal_code/i)).toBeInTheDoc…
    [90m   |[39m                     [31m^[39m
    [90m130|[39m
    [90m131|[39m       fireEvent[33m.[39m[34mclick[39m(toggle)[33m;[39m

[31m[2m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[2/3]⎯[22m[39m

[41m[1m FAIL [22m[49m src/pages/GuestFormModal.test.tsx[2m > [22mGuestFormModal[2m > [22mfiscal section[2m > [22mis expanded by default when guest has fiscal data
[31m[1mTestingLibraryElementError[22m[39m: Unable to find a label with the text of: /label_fiscal_code/i

Ignored nodes: comments, script, style
[36m<body>[39m
  [36m<div>[39m
    [36m<div[39m
      [33maria-labelledby[39m=[32m"guest-modal-title"[39m
      [33maria-modal[39m=[32m"true"[39m
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

[31m[2m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[3/3]⎯[22m[39m


[2m Test Files [22m [1m[31m2 failed[39m[22m[2m | [22m[1m[32m87 passed[39m[22m[90m (89)[39m
[2m      Tests [22m [1m[31m3 failed[39m[22m[2m | [22m[1m[32m894 passed[39m[22m[90m (897)[39m
[2m   Start at [22m 08:49:13
[2m   Duration [22m 141.68s[2m (transform 2.85s, setup 4.44s, import 36.29s, tests 54.42s, environment 37.38s)[22m

close timed out after 10000ms
Tests closed successfully but something prevents Vite server from exiting
You can try to identify the cause by enabling "hanging-process" reporter. See https://vitest.dev/guide/reporters.html#hanging-process-reporter


```
