# A.SPEC AI-0042 — Implement AI provider rate limiting, explicit fallback error boundary, and AI Prometheus alert rules

ID: AI-0042
Mode: WRITE
RISK: MEDIUM

## WHY
AI-0041 identified operational resilience gaps: missing AI-specific Resilience4j rate limiting, broad runtime exception swallowing in fallback boundaries, and lack of dedicated AI provider alerts in Prometheus.

## WHAT
Configure Resilience4j rate limiter for 'aiProvider' in configuration, apply rate limiting to AssistantService provider calls, narrow the fallback exception boundary in ResilientIntentFallbackHandler, add dedicated AI provider alert rules in docker/prometheus/alert_rules.yml, and add unit test coverage.

## SCOPE
- Add resilience4j.ratelimiter configuration for aiProvider in config-service/src/main/resources/config/frontdesk-service.yml
- Annotate AssistantService with @RateLimiter(name = 'aiProvider') and handle rate limit rejections as retryable/fallback events
- Refine ResilientIntentFallbackHandler to specifically intercept provider/resilience exceptions while logging non-provider errors distinctly
- Add Prometheus alerting rules for AI provider high error rate, fallback execution spikes, and rate limiter exhaustion
- Add/update unit tests in frontdesk-service to verify rate limiting and fallback boundaries

## OUT OF SCOPE
- Database migrations or schema changes
- API Gateway global rate limiting modifications
- Modifications to external AI model providers or keys
- Frontdesk auth or RBAC guard changes

## CONTRACT
- Resilience4j rate limiter configuration for 'aiProvider' must be present in config-service/src/main/resources/config/frontdesk-service.yml
- AssistantService provider invocation path must enforce @RateLimiter(name = 'aiProvider')
- ResilientIntentFallbackHandler must handle provider failures and rate-limit rejections smoothly using deterministic fallback
- Prometheus rules in alert_rules.yml must include specific AI provider operational alerts
- Unit tests in frontdesk-service must pass via ./gradlew :frontdesk-service:test

## INVARIANTS
- Existing Resilience4j circuit breaker and retry configuration must remain intact
- Tenant and user security context must continue to pass through AI controller and service paths
- Local deterministic fallback behavior must remain available when AI provider fails or is rate-limited

## VERIFICATION
- ./gradlew :frontdesk-service:test

## ROLLBACK
git checkout -- frontdesk-service/ config-service/ docker/prometheus/alert_rules.yml

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java
- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/ResilientIntentFallbackHandler.java
- frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/
- config-service/src/main/resources/config/frontdesk-service.yml
- docker/prometheus/alert_rules.yml
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test
END_VERIFY_COMMANDS
