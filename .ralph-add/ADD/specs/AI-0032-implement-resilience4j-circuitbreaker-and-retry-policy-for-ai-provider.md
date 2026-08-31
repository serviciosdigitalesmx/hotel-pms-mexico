# A.SPEC AI-0032 — Implement Resilience4j CircuitBreaker and Retry policy for AI Provider in frontdesk-service

ID: AI-0032
Mode: WRITE
RISK: MEDIUM

## WHY
Audit AI-0031 identified that the AI integration lacks a Resilience4j CircuitBreaker, Retry policy, and transient vs. permanent error classification. Adding these guarantees graceful degradation, fast failure when the AI provider is down, and automatic deterministic fallback execution.

## WHAT
1. Add Resilience4j CircuitBreaker and Retry configurations for 'aiProvider' in frontdesk-service configuration.
2. Wrap external HTTP AI provider invocations in AssistantService with Resilience4j CircuitBreaker and Retry policies.
3. Classify transient failures (5xx, timeouts, 429) as retryable while marking client/auth errors (400, 401, 403) as non-retryable.
4. Connect circuit breaker failure states to trigger the existing deterministic local intent router fallback.
5. Write unit tests simulating transient errors, permanent failures, retry limits, and circuit opening without invoking external AI providers.

## SCOPE
- frontdesk-service AI assistant integration code and tests
- frontdesk-service configuration yml in config-service and frontdesk-service

## OUT OF SCOPE
- Database schema changes or Flyway migrations
- External AI API calls during tests
- Gateway rate limiter changes or other microservices

## CONTRACT
- Deterministic local fallback MUST execute when AI circuit is OPEN or retries are exhausted.
- Transient errors (HTTP 5xx, timeouts, HTTP 429) MUST trigger backoff retry up to configured attempts.
- Permanent errors (HTTP 4xx non-429) MUST NOT be retried.
- Security context and hotelId MUST be preserved during retry and fallback flows.

## INVARIANTS
- No external network calls permitted during tests.
- Existing AssistantController security annotations (@PreAuthorize) and parameters remain unaffected.

## VERIFICATION
- ./gradlew :frontdesk-service:test

## ROLLBACK
git checkout -- frontdesk-service config-service

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/
- frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/
- frontdesk-service/src/main/resources/
- config-service/src/main/resources/config/frontdesk-service.yml
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test
END_VERIFY_COMMANDS
