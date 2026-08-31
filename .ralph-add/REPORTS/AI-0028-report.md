# A.SPEC AI-0028 Report

## Result

**Status: BLOCKER**

One failing test was identified among 408 tests.

## Observed evidence

- Test report directory inspected: `frontdesk-service/build/test-results/test/`
- Total suites: 34
- Total tests: 408
- Skipped: 0
- Failures: 1
- Errors: 0

Failing test:

- Class: `com.hotelpms.frontdesk.assistant.engine.LocalIntentRouterTest`
- Method: `unknownMessageUsesGroqFallbackUnchanged()`
- Report: `TEST-com.hotelpms.frontdesk.assistant.engine.LocalIntentRouterTest.xml`

Failure type:

```text
org.mockito.exceptions.misusing.UnnecessaryStubbingException
```

Failure message:

```text
Unnecessary stubbings detected.
Following stubbings are unnecessary:
1. LocalIntentRouterTest.setUp(LocalIntentRouterTest.java:76)
2. LocalIntentRouterTest.setUp(LocalIntentRouterTest.java:81)
```

Stack trace:

```text
at org.mockito.junit.jupiter.MockitoExtension.afterEach(MockitoExtension.java:197)
at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
```

Corresponding source:

- `LocalIntentRouterTest.java:76`: strict stubbing of `sessionStore.withLock(...)`
- `LocalIntentRouterTest.java:81`: strict stubbing of `sessionStore.load(HOTEL_ID, USER_ID)`
- Failing test source: `LocalIntentRouterTest.java:158`

The failing test configures an `assistantService.chat(...)` return value and calls:

```text
call("resume los ingresos del mes")
```

The test report also records deterministic fallback warnings because the AI intent provider is unavailable. These warnings are logged during the suite but are not the reported test failure.

## Expected vs actual state

- Expected: `unknownMessageUsesGroqFallbackUnchanged()` completes successfully with no unnecessary Mockito stubbings.
- Actual: The test assertion path is reached, but Mockito fails during `afterEach` because the two strict setup stubbings were unused.

## Root-cause assessment

**Observed:** Mockito reports the unused stubbings at `setUp()` lines 76 and 81.

**Inference:** For the unknown-message fallback path, `sessionStore.withLock(...)` and `sessionStore.load(...)` are not invoked. Because those setup stubbings are strict, Mockito raises `UnnecessaryStubbingException` after the test.

## Repository state

**WARNING:** The inspected checkout contains pre-existing modified and untracked files under `frontdesk-service`, including `LocalIntentRouterTest.java`. No files were modified by this inspection.

## Scope and mutation verification

- Source files modified: none by this execution.
- Test files modified: none by this execution.
- Build/test commands executed: none.
- Database, Redis, Docker runtime, migrations, secrets, and Git state were not changed.
