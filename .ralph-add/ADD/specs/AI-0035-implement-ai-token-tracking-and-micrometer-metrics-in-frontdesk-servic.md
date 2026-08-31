# A.SPEC AI-0035 — Implement AI Token Tracking and Micrometer Metrics in frontdesk-service

ID: AI-0035
Mode: WRITE
RISK: MEDIUM

## WHY
The AI-0034 inspection revealed that token usage fields (prompt_tokens, completion_tokens, total_tokens) from AI provider responses are ignored, preventing token cost/volume tracking and AI-specific operational observability.

## WHAT
Extract usage metrics from chat completion JSON responses in AssistantService, instrument AI request execution with Micrometer counters/timers tagged by provider and model, and add unit tests covering token extraction and metrics recording.

## SCOPE
- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java
- frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/AssistantServiceTest.java

## OUT OF SCOPE
- config-service/src
- guest-service/src
- common-web-lib/src
- Database schema or Flyway migrations
- Modifications to security/RBAC filters

## CONTRACT
- AssistantService parses prompt_tokens, completion_tokens, and total_tokens when present in AI responses.
- Micrometer MeterRegistry records counters for pms.ai.tokens.prompt, pms.ai.tokens.completion, pms.ai.tokens.total, and timer pms.ai.requests.duration.
- Response structures returned to AssistantController and frontend remain backward-compatible.

## INVARIANTS
- No secret keys or full prompts logged in cleartext.
- Existing fallback behavior via ResilientIntentFallbackHandler is preserved when response parsing fails or provider is unreachable.
- Tenant context (hotelId, userId) is maintained.

## VERIFICATION
- ./gradlew :frontdesk-service:test --no-daemon

## ROLLBACK
git checkout -- frontdesk-service/src/

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/
- frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test --no-daemon
END_VERIFY_COMMANDS
