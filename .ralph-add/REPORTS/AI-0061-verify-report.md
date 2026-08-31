# AI-0061 Verification Result

- Result: FAIL
- Source/workspace mutation: none detected
- Commands:
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
$ npm --prefix frontend test
exit=1
39m=[32m"button"[39m
                        [36m>[39m
                          [0mdelete[0m
                        [36m</button>[39m
                      [36m</td>[39m
                    [36m</tr>[39m
                  [36m</tbody>[39m
                [36m</table>[39m
              [36m</div>[39m
            [36m</div>[39m
          [36m</div>[39m
        [36m</div>[39m
        [36m<div[39m
          [33mclass[39m=[32m"h-px bg-outline-variant mx-6"[39m
        [36m/>[39m
        [36m<div[39m
          [33mclass[39m=[32m"px-6 py-4"[39m
        [36m>[39m
          [36m<div[39m
            [33mclass[39m=[32m"flex justify-end"[39m
          [36m>[39m
            [36m<button[39m
              [33mclass[39m=[32m"inline-flex items-center justify-center gap-2 px-6 h-10 rounded-shape-full text-sm font-medium font-body transition-all
        text-primary bg-transparent hover:bg-primary/[0.08] focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2
        
        "[39m
            [36m>[39m
              [0mclose[0m
            [36m</button>[39m
          [36m</div>[39m
        [36m</div>[39m
      [36m</div>[39m
    [36m</div>[39m
  [36m</div>[39m
[36m</body>[39m
[90m [2m❯[22m Object.getElementError node_modules/@testing-library/dom/dist/config.js:[2m37:19[22m[39m
[90m [2m❯[22m node_modules/@testing-library/dom/dist/query-helpers.js:[2m76:38[22m[39m
[90m [2m❯[22m node_modules/@testing-library/dom/dist/query-helpers.js:[2m52:17[22m[39m
[90m [2m❯[22m node_modules/@testing-library/dom/dist/query-helpers.js:[2m95:19[22m[39m
[36m [2m❯[22m src/pages/Rooms/RateSeasonManagerModal.test.tsx:[2m62:19[22m[39m
    [90m 60|[39m     render(<RateSeasonManagerModal roomType={ROOM_TYPE} onClose={onClo…
    [90m 61|[39m     await waitFor(() => expect(screen.getByText('High season')).toBeIn…
    [90m 62|[39m     [34mexpect[39m(screen[33m.[39m[34mgetByText[39m([32m'€ 150.00'[39m))[33m.[39m[34mtoBeInTheDocument[39m()[33m;[39m
    [90m   |[39m                   [31m^[39m
    [90m 63|[39m   })[33m;[39m
    [90m 64|[39m

[31m[2m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[40/51]⎯[22m[39m

[41m[1m FAIL [22m[49m src/pages/Rooms/RoomTypeList.test.tsx[2m > [22mRoomTypeList[2m > [22mrenders room type row after data loads
[31m[1mTestingLibraryElementError[22m[39m: Unable to find an element with the text: € 50.00. This could be because the text is broken up by multiple elements. In this case, you can provide a function for your text matcher to make your matcher more flexible.

Ignored nodes: comments, script, style
[36m<body>[39m
  [36m<div>[39m
    [36m<div[39m
      [33mclass[39m=[32m"space-y-4"[39m
    [36m>[39m
      [36m<div[39m
        [33mclass[39m=[32m"flex justify-between items-center"[39m
      [36m>[39m
        [36m<h2[39m
          [33mclass[39m=[32m"text-xl font-display font-medium text-on-surface"[39m
        [36m>[39m
          [0mtab_room_types[0m
        [36m</h2>[39m
        [36m<button[39m
          [33mclass[39m=[32m"inline-flex items-center justify-center gap-2 px-6 h-10 rounded-shape-full text-sm font-medium font-body transition-all
        bg-primary text-on-primary hover:shadow-elevation-1 active:shadow-elevation-0 focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2
        
        "[39m
        [36m>[39m
          [36m<span[39m
            [33maria-hidden[39m=[32m"true"[39m
            [33mclass[39m=[32m"material-symbols-outlined"[39m
            [33mstyle[39m=[32m"font-size: 18px; width: 18px; height: 18px;"[39m
          [36m>[39m
            [0madd[0m
          [36m</span>[39m
          [0madd_room_type[0m
        [36m</button>[39m
      [36m</div>[39m
      [36m<div[39m
        [33mclass[39m=[32m"bg-surface shadow-elevation-1 rounded-shape-md overflow-hidden "[39m
      [36m>[39m
        [36m<div[39m
          [33mclass[39m=[32m"overflow-x-auto"[39m
        [36m>[39m
          [36m<table[39m
            [33mclass[39m=[32m"min-w-full"[39m
          [36m>[39m
            [36m<thead>[39m
              [36m<tr[39m
                [33mclass[39m=[32m"bg-surface-container-highest"[39m
              [36m>[39m
                [36m<th[39m
                  [33mclass[39m=[32m"py-3.5 px-4 text-left text-xs font-medium font-body text-on-surface-variant uppercase tracking-wider first:pl-6 last:pr-6"[39m
                  [33mscope[39m=[32m"col"[39m
                [36m>[39m
                  [0mname[0m
                [36m</th>[39m
                [36m<th[39m
                  [33mclass[39m=[32m"py-3.5 px-4 text-left text-xs font-medium font-body text-on-surface-variant uppercase tracking-wider first:pl-6 last:pr-6"[39m
                  [33mscope[39m=[32m"col"[39m
                [36m>[39m
                  [0mmax_occupancy[0m
                [36m</th>[39m
                [36m<th[39m
                  [33mclass[39m=[32m"py-3.5 px-4 text-left text-xs font-medium font-body text-on-surface-variant uppercase tracking-wider first:pl-6 last:pr-6"[39m
                  [33mscope[39m=[32m"col"[39m
                [36m>[39m
                  [0mbase_price[0m
                [36m</th>[39m
                [36m<th[39m
                  [33mclass[39m=[32m"py-3.5 px-4 text-left text-xs font-medium font-body text-on-surface-variant uppercase tracking-wider first:pl-6 last:pr-6"[39m
                  [33mscope[39m=[32m"col"[39m
                [36m>[39m
                  [0mdescription[0m
                [36m</th>[39m
                [36m<th[39m
                  [33mclass[39m=[32m"py-3.5 px-4 text-left text-xs font-medium font-body text-on-surface-variant uppercase tracking-wider first:pl-6 last:pr-6"[39m
                  [33mscope[39m=[32m"col"[39m
                [36m>[39m
                  [0mactions[0m
                [36m</th>[39m
              [36m</tr>[39m
            [36m</thead>[39m
            [36m<tbody[39m
              [33mclass[39m=[32m"divide-y divide-outline-variant/50"[39m
            [36m>[39m
              [36m<tr[39m
                [33mclass[39m=[32m"hover:bg-surface-container-low transition-colors "[39m
              [36m>[39m
                [36m<td[39m
                  [33mclass[39m=[32m"whitespace-nowrap py-4 px-4 text-sm font-body text-on-surface first:pl-6 last:pr-6 font-medium"[39m
                [36m>[39m
                  [0mSingle[0m
                [36m</td>[39m
                [36m<td[39m
                  [33mclass[39m=[32m"whitespace-nowrap py-4 px-4 text-sm font-body text-on-surface first:pl-6 last:pr-6 text-on-surface-variant font-medium"[39m
                [36m>[39m
                  [0m1[0m
                [36m</td>[39m
                [36m<td[39m
                  [33mclass[39m=[32m"whitespace-nowrap py-4 px-4 text-sm font-body text-on-surface first:pl-6 last:pr-6 text-on-surface-variant font-medium"[39m
                [36m>[39m
                  [0mMX$50.00[0m
                [36m</td>[39m
                [36m<td[39m
                  [33mclass[39m=[32m"whitespace-nowrap py-4 px-4 text-sm font-body text-on-surface first:pl-6 last:pr-6 text-on-surface-variant max-w-xs truncate"[39m
                  [33mtitle[39m=[32m"A single room"[39m
                [36m>[39m
                  [0mA single room[0m
                [36m</td>[39m
                [36m<td[39m
                  [33mclass[39m=[32m"whitespace-nowrap py-4 px-4 text-sm font-body text-on-surface first:pl-6 last:pr-6 text-right"[39m
                [36m>[39m
                  [36m<button[39m
                    [33mclass[39m=[32m"inline-flex items-center justify-center min-h-[40px] min-w-[40px] px-2
        font-medium text-sm rounded transition-colors
        focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2
        disabled:opacity-50 disabled:cursor-not-allowed
        text-primary hover:text-primary/80 focus-visible:ring-primary
        lg:mr-4"[39m
                    [33mtype[39m=[32m"button"[39m
                  [36m>[39m
                    [0mrate_seasons[0m
                  [36m</button>[39m
                  [36m<button[39m
                    [33mclass[39m=[32m"inline-flex items-center justify-center min-h-[40px] min-w-[40px] px-2
        font-medium text-sm rounded transition-colors
        focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2
        disabled:opacity-50 disabled:cursor-not-allowed
        text-primary hover:text-primary/80 focus-visible:ring-primary
        lg:mr-4"[39m
                    [33mtype[39m=[32m"button"[39m
                  [36m>[39m
                    [0medit[0m
                  [36m</button>[39m
                [36m</td>[39m
              [36m</tr>[39m
            [36m</tbody>[39m
          [36m</table>[39m
        [36m</div>[39m
      [36m</div>[39m
    [36m</div>[39m
  [36m</div>[39m
[36m</body>[39m
[90m [2m❯[22m Object.getElementError node_modules/@testing-library/dom/dist/config.js:[2m37:19[22m[39m
[90m [2m❯[22m node_modules/@testing-library/dom/dist/query-helpers.js:[2m76:38[22m[39m
[90m [2m❯[22m node_modules/@testing-library/dom/dist/query-helpers.js:[2m52:17[22m[39m
[90m [2m❯[22m node_modules/@testing-library/dom/dist/query-helpers.js:[2m95:19[22m[39m
[36m [2m❯[22m src/pages/Rooms/RoomTypeList.test.tsx:[2m50:19[22m[39m
    [90m 48|[39m     [34mrender[39m([33m<[39m[33mRoomTypeList[39m [33m/[39m[33m>[39m)[33m;[39m
    [90m 49|[39m     await waitFor(() => expect(screen.getByText('Single')).toBeInTheDo…
    [90m 50|[39m     [34mexpect[39m(screen[33m.[39m[34mgetByText[39m([32m'€ 50.00'[39m))[33m.[39m[34mtoBeInTheDocument[39m()[33m;[39m
    [90m   |[39m                   [31m^[39m
    [90m 51|[39m   })[33m;[39m
    [90m 52|[39m

[31m[2m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[41/51]⎯[22m[39m

[41m[1m FAIL [22m[49m src/pages/Settings/SettingsAppearance.test.tsx[2m > [22mSettingsAppearance[2m > [22mmarks the active language as checked and calls setLanguage on selection
[31m[1mError[22m: [2mexpect([22m[31melement[31m[2m).toHaveAttribute([22m[32m[32m"aria-checked"[32m[31m[2m, [22m[32m[32m"true"[32m[31m[2m) // element.getAttribute("aria-checked") === "true"[22m

Expected the element to have attribute:
[32m  aria-checked="true"[31m
Received:
[31m  aria-checked="false"[31m[39m
[36m [2m❯[22m src/pages/Settings/SettingsAppearance.test.tsx:[2m53:65[22m[39m
    [90m 51|[39m   it('marks the active language as checked and calls setLanguage on se…
    [90m 52|[39m     [34mrenderPage[39m()[33m;[39m
    [90m 53|[39m     expect(screen.getByRole('radio', { name: /lang_spanish/ })).toHave…
    [90m   |[39m                                                                 [31m^[39m
    [90m 54|[39m     fireEvent.click(screen.getByRole('radio', { name: /lang_english/ }…
    [90m 55|[39m     [34mexpect[39m(setLanguage)[33m.[39m[34mtoHaveBeenCalledWith[39m([32m'en'[39m)[33m;[39m

[31m[2m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[42/51]⎯[22m[39m


[2m Test Files [22m [1m[31m12 failed[39m[22m[2m | [22m[1m[32m77 passed[39m[22m[90m (89)[39m
[2m      Tests [22m [1m[31m51 failed[39m[22m[2m | [22m[1m[32m846 passed[39m[22m[90m (897)[39m
[2m   Start at [22m 03:03:13
[2m   Duration [22m 58.31s[2m (transform 9.91s, setup 16.01s, import 109.71s, tests 137.85s, environment 111.96s)[22m

close timed out after 10000ms
Tests closed successfully but something prevents Vite server from exiting
You can try to identify the cause by enabling "hanging-process" reporter. See https://vitest.dev/guide/reporters.html#hanging-process-reporter


```
