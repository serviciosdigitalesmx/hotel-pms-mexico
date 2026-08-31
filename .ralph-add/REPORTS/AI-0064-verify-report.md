# AI-0064 Verification Result

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

BUILD SUCCESSFUL in 4s
75 actionable tasks: 75 up-to-date
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.3.1/userguide/configuration_cache_enabling.html


$ npm --prefix frontend test
exit=1
marks a room occupied when it overlaps another active reservation, and blocks the click
[41m[1m FAIL [22m[49m src/pages/Reservations/RoomSelection.test.tsx[2m > [22mRoomSelection[2m > [22mdoes not treat the current reservation itself as an occupying conflict
[41m[1m FAIL [22m[49m src/pages/Reservations/RoomSelection.test.tsx[2m > [22mRoomSelection[2m > [22mmarks the room as selected when its id is in selectedRoomIds
[41m[1m FAIL [22m[49m src/pages/Reservations/RoomSelection.test.tsx[2m > [22mRoomSelection[2m > [22mdoes not call onToggleRoom for a free room when readOnly
[41m[1m FAIL [22m[49m src/pages/Reservations/RoomSelection.test.tsx[2m > [22mRoomSelection[2m > [22mpropagates date and guest count changes
[41m[1m FAIL [22m[49m src/pages/Reservations/RoomSelection.test.tsx[2m > [22mRoomSelection[2m > [22mshows the flat basePrice when no resolved price is available for the room
[41m[1m FAIL [22m[49m src/pages/Reservations/RoomSelection.test.tsx[2m > [22mRoomSelection[2m > [22mshows the date-aware resolved total price when available for the room
[41m[1m FAIL [22m[49m src/pages/Reservations/RoomSelection.test.tsx[2m > [22mRoomSelection[2m > [22mhas no accessibility violations
[31m[1mTypeError[22m: Cannot read properties of undefined (reading 'language')[39m
[36m [2m❯[22m RoomButton src/pages/Reservations/RoomSelection.tsx:[2m56:40[22m[39m
    [90m 54|[39m         [33m{[39mdisplayPrice [33m===[39m undefined
    [90m 55|[39m           [33m?[39m [32m'—'[39m
    [90m 56|[39m           : new Intl.NumberFormat(i18n.language, { style: 'currency', …
    [90m   |[39m                                        [31m^[39m
    [90m 57|[39m       [33m<[39m[33m/[39m[33mspan[39m[33m>[39m
    [90m 58|[39m       [33m{[39misOccupied [33m&&[39m (
[90m [2m❯[22m Object.react_stack_bottom_frame node_modules/react-dom/cjs/react-dom-client.development.js:[2m25904:20[22m[39m
[90m [2m❯[22m renderWithHooks node_modules/react-dom/cjs/react-dom-client.development.js:[2m7662:22[22m[39m
[90m [2m❯[22m updateFunctionComponent node_modules/react-dom/cjs/react-dom-client.development.js:[2m10166:19[22m[39m
[90m [2m❯[22m updateSimpleMemoComponent node_modules/react-dom/cjs/react-dom-client.development.js:[2m9830:14[22m[39m
[90m [2m❯[22m updateMemoComponent node_modules/react-dom/cjs/react-dom-client.development.js:[2m9763:13[22m[39m
[90m [2m❯[22m beginWork node_modules/react-dom/cjs/react-dom-client.development.js:[2m12204:18[22m[39m
[90m [2m❯[22m runWithFiberInDEV node_modules/react-dom/cjs/react-dom-client.development.js:[2m874:13[22m[39m
[90m [2m❯[22m performUnitOfWork node_modules/react-dom/cjs/react-dom-client.development.js:[2m17641:22[22m[39m
[90m [2m❯[22m workLoopSync node_modules/react-dom/cjs/react-dom-client.development.js:[2m17469:41[22m[39m

[31m[2m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[38/48]⎯[22m[39m

[41m[1m FAIL [22m[49m src/pages/Rates/RateCalendar.test.tsx[2m > [22mRateCalendar[2m > [22mshows a loading spinner then renders the grid
[31m[1mTestingLibraryElementError[22m[39m: Unable to find an element with the text: € 90.00. This could be because the text is broken up by multiple elements. In this case, you can provide a function for your text matcher to make your matcher more flexible.

Ignored nodes: comments, script, style
[36m<body>[39m
  [36m<div>[39m
    [36m<div[39m
      [33mclass[39m=[32m"space-y-4"[39m
    [36m>[39m
      [36m<div[39m
        [33mclass[39m=[32m"flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4"[39m
      [36m>[39m
        [36m<div>[39m
          [36m<h1[39m
            [33mclass[39m=[32m"text-2xl font-display font-bold tracking-tight text-on-surface flex items-center"[39m
          [36m>[39m
            [36m<span[39m
              [33maria-hidden[39m=[32m"true"[39m
              [33mclass[39m=[32m"material-symbols-outlined mr-2 text-primary"[39m
            [36m>[39m
              [0mpayments[0m
            [36m</span>[39m
            [0mnav_rates[0m
          [36m</h1>[39m
          [36m<p[39m
            [33mclass[39m=[32m"text-sm font-body text-on-surface-variant mt-1"[39m
          [36m>[39m
            [0mrate_calendar_subtitle[0m
          [36m</p>[39m
        [36m</div>[39m
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
          [0mbtn_apply_price[0m
        [36m</button>[39m
      [36m</div>[39m
      [36m<div[39m
        [33mclass[39m=[32m"flex items-center justify-between"[39m
      [36m>[39m
        [36m<div[39m
          [33mclass[39m=[32m"flex items-center gap-2"[39m
        [36m>[39m
          [36m<button[39m
            [33maria-label[39m=[32m"prev_month"[39m
            [33mclass[39m=[32m"inline-flex items-center justify-center gap-2 px-6 h-10 rounded-shape-full text-sm font-medium font-body transition-all
        border border-outline text-primary bg-transparent hover:bg-primary/[0.08] focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2
        
        "[39m
          [36m>[39m
            [36m<span[39m
              [33maria-hidden[39m=[32m"true"[39m
              [33mclass[39m=[32m"material-symbols-outlined"[39m
              [33mstyle[39m=[32m"font-size: 18px; width: 18px; height: 18px;"[39m
            [36m>[39m
              [0mchevron_left[0m
            [36m</span>[39m
          [36m</button>[39m
          [36m<span[39m
            [33mclass[39m=[32m"text-sm font-medium font-body text-on-surface capitalize min-w-32 text-center"[39m
          [36m>[39m
            [0mAugust 2026[0m
          [36m</span>[39m
          [36m<button[39m
            [33maria-label[39m=[32m"next_month"[39m
            [33mclass[39m=[32m"inline-flex items-center justify-center gap-2 px-6 h-10 rounded-shape-full text-sm font-medium font-body transition-all
        border border-outline text-primary bg-transparent hover:bg-primary/[0.08] focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2
        
        "[39m
          [36m>[39m
            [36m<span[39m
              [33maria-hidden[39m=[32m"true"[39m
              [33mclass[39m=[32m"material-symbols-outlined"[39m
              [33mstyle[39m=[32m"font-size: 18px; width: 18px; height: 18px;"[39m
            [36m>[39m
              [0mchevron_right[0m
            [36m</span>[39m
          [36m</button>[39m
        [36m</div>[39m
      [36m</div>[39m
      [36m<div[39m
        [33mclass[39m=[32m"flex flex-wrap items-center gap-4 text-xs font-body text-on-surface-variant"[39m
      [36m>[39m
        [36m<span[39m
          [33mclass[39m=[32m"flex items-center gap-1.5"[39m
        [36m>[39m
          [36m<span[39m
            [33maria-hidden[39m=[32m"true"[39m
            [33mclass[39m=[32m"w-2.5 h-2.5 rounded-full"[39m
            [33mstyle[39m=[32m"background-color: rgb(103, 80, 164);"[39m
          [36m/>[39m
          [0mHigh season[0m
        [36m</span>[39m
        [36m<span[39m
          [33mclass[39m=[32m"flex items-center gap-1.5"[39m
        [36m>[39m
          [36m<span[39m
            [33maria-hidden[39m=[32m"true"[39m
            [33mclass[39m=[32m"w-2.5 h-2.5 rounded-full border border-outline-variant"[39m
          [36m/>[39m
          [0mrate_calendar_legend_base_price[0m
        [36m</span>[39m
      [36m</div>[39m
      [36m<div[39m
        [33mclass[39m=[32m"bg-surface border border-outline-variant rounded-shape-md overflow-hidden"[39m
      [36m>[39m
        [36m<div[39m
          [33mclass[39m=[32m"overflow-auto"[39m
        [36m>[39m
          [36m<div[39m
            [33mstyle[39m=[32m"width: 3292px;"[39m
          [36m>[39m
            [36m<div[39m
              [33mclass[39m=[32m"sticky top-0 z-40 flex bg-surface-container-low border-b border-outline-variant"[39m
              [33mstyle[39m=[32m"height: 64px;"[39m
            [36m>[39m
              [36m<div[39m
                [33mclass[39m=[32m"sticky left-0 z-50 h-full bg-surface-container-low border-r border-outline-variant flex flex-col justify-center px-4 font-display font-bold text-sm text-primary shadow-elevation-1"[39m
                [33mstyle[39m=[32m"width: 192px;"[39m
              [36m>[39m
                [0mlabel_room_types[0m
              [36m</div>[39m
              [36m<div[39m
                [33mclass[39m=[32m"flex flex-1"[39m
              [36m>[39m
                [36m<div[39m
                  [33mclass[39m=[32m"flex-shrink-0 border-r border-outline-variant flex flex-col items-center justify-center "[39m
                  [33mstyle[39m=[32m"width: 100px;"[39m
                [36m>[39m
                  [36m<span[39m
                    [33mclass[39m=[32m"text-[10px] uppercase font-bold tracking-wider opacity-60"[39m
                  [36m>[39m
                    [0mSat[0m
                  [36m</span>[39m
                  [36m<span[39m
                    [33mclass[39m=[32m"text-lg font-display font-medium leading-none"[39m
                  [36m>[39m
                    [0m1[0m
                  [36m</span>[39m
                [36m</div>[39m
                [36m<div[39m
                  [33mclass[39m=[32m"flex-shrink-0 border-r border-outline-variant flex flex-col items-center justify-center "[39m
                  [33mstyle[39m=[32m"width: 100px;"[39m
                [36m>[39m
                  [36m<span[39m
                    [33mclass[39m=[32m"text-[10px] uppercase font-...
[90m [2m❯[22m Object.getElementError node_modules/@testing-library/dom/dist/config.js:[2m37:19[22m[39m
[90m [2m❯[22m node_modules/@testing-library/dom/dist/query-helpers.js:[2m76:38[22m[39m
[90m [2m❯[22m node_modules/@testing-library/dom/dist/query-helpers.js:[2m52:17[22m[39m
[90m [2m❯[22m node_modules/@testing-library/dom/dist/query-helpers.js:[2m95:19[22m[39m
[36m [2m❯[22m src/pages/Rates/RateCalendar.test.tsx:[2m68:19[22m[39m
    [90m 66|[39m     [34mrender[39m([33m<[39m[33mRateCalendar[39m [33m/[39m[33m>[39m)[33m;[39m
    [90m 67|[39m     await waitFor(() => expect(screen.getByText('Double')).toBeInTheDo…
    [90m 68|[39m     [34mexpect[39m(screen[33m.[39m[34mgetByText[39m([32m'€ 90.00'[39m))[33m.[39m[34mtoBeInTheDocument[39m()[33m;[39m
    [90m   |[39m                   [31m^[39m
    [90m 69|[39m   })[33m;[39m
    [90m 70|[39m

[31m[2m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[39/48]⎯[22m[39m


[2m Test Files [22m [1m[31m9 failed[39m[22m[2m | [22m[1m[32m80 passed[39m[22m[90m (89)[39m
[2m      Tests [22m [1m[31m48 failed[39m[22m[2m | [22m[1m[32m849 passed[39m[22m[90m (897)[39m
[2m   Start at [22m 03:12:28
[2m   Duration [22m 71.64s[2m (transform 9.92s, setup 20.82s, import 130.67s, tests 166.37s, environment 143.28s)[22m

close timed out after 10000ms
Tests closed successfully but something prevents Vite server from exiting
You can try to identify the cause by enabling "hanging-process" reporter. See https://vitest.dev/guide/reporters.html#hanging-process-reporter


```
