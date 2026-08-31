# A.SPEC AI-0041 — Inspection Report

## Result

**WARNING** — Existing AI integration and resilience components were located. The implementation includes provider selection, timeouts, retry, circuit breaker, deterministic fallback, authorization, tenant scoping, prompt construction, and metrics. Gaps remain in explicit rate limiting, AI-specific alerting, and documented/provider-independent error boundaries.

## Read-only compliance

**PASS**

### OBSERVED

- The requested ADD files were read.
- No repository files, Git state, databases, Redis, Docker runtime, migrations, or secrets were modified.
- No external AI provider was called.
- No `.env` values were read or printed.
- `git status --short` reported pre-existing modified and untracked files; these were not changed.

### INFERENCE

- The working tree was already dirty before this inspection because the status output contains extensive unrelated modifications and untracked artifacts.

## AI integration inventory

### Primary AI service — PASS

**Observed evidence**

- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java`
  - Defines the AI service at lines 39–43.
  - Uses DeepSeek for model names beginning with `deepseek-` and Ollama for other models at lines 91–110 and 329–339.
  - Uses the DeepSeek endpoint at line 45.
  - Resolves the Ollama endpoint from `OLLAMA_BASE_URL`, with a local Docker fallback, at lines 302–325.
  - Loads tenant AI settings from `HotelSettingsRepository` at lines 88–90.
  - Decrypts the tenant API key only for DeepSeek at lines 100–104.
  - Builds provider requests, system prompts, tool definitions, and tool calls at lines 124–152 and 197–210.
  - Parses provider responses and tool calls at lines 230–239 and 275–293.

**Inference**

- `AssistantService` is the central provider adapter and prompt/tool orchestration component for the existing AI path.

### AI controller and authorization — PASS

**Observed evidence**

- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantController.java`
  - Exposes `POST /api/v1/stays/assistant/chat` at lines 18–26.
  - Requires `ADMIN`, `OWNER`, or `RECEPTIONIST` roles at line 26.
  - Passes the authenticated hotel ID and roles into `AssistantService` at lines 30–34.
  - Resolves tenant identity from the authenticated security context at lines 37–44.

**Inference**

- The controller-level AI entry point preserves authenticated role and tenant context.

### Deterministic fallback — PASS

**Observed evidence**

- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouter.java`
  - AI-first routing invokes `AssistantService.chat` and supplies deterministic processing as fallback at lines 128–141.
- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/ResilientIntentFallbackHandler.java`
  - Executes the primary provider and invokes the fallback on runtime provider failure at lines 20–31.
  - The fallback is supplied by the router, preserving its tenant and session context according to the class documentation at lines 15–18.
- `frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/ResilientIntentFallbackHandlerTest.java`
  - Contains tests for successful primary resolution, provider failure fallback, and fallback failure propagation.

**Inference**

- The AI path has a local deterministic fallback and does not require an AI provider for deterministic intent handling.

### Prompt management and tool safety — PASS

**Observed evidence**

- `AssistantService.java:197–210` contains the system prompt.
- The prompt instructs the model to:
  - Respond in Spanish.
  - Use PMS tools for data and actions.
  - Avoid invented IDs and results.
  - Avoid fabricated guest and reservation data.
  - Require authenticated user confirmation before executing actions.
  - Avoid processing credentials and API keys.
- Tool definitions are generated through `AssistantToolCatalog` at `AssistantService.java:150`.
- Tool confirmation metadata is applied while parsing tool calls at `AssistantService.java:285–289`.

**Inference**

- Prompt-level controls and tool confirmation metadata provide safeguards against unsupported operational actions, although prompt instructions alone are not a complete security boundary.

## Resilience coverage

### Request timeout — PASS

**Observed evidence**

- Connection timeout: `AssistantService.java:64–66` sets a 10-second `HttpClient` connection timeout.
- Request timeout: `AssistantService.java:46` defines a 30-second timeout.
- The request applies the timeout at `AssistantService.java:95–98`.

**Inference**

- Both connection establishment and total provider request duration have explicit limits.

### Retry — PASS

**Observed evidence**

- `AssistantService.java:82–83` applies `@Retry(name = "aiProvider")`.
- `config-service/src/main/resources/config/frontdesk-service.yml:124–132` configures:
  - Maximum attempts: 3.
  - Initial wait: 250 ms.
  - Exponential backoff enabled.
  - Multiplier: 2.
  - Retry limited to `RetryableAiProviderException`.
- Provider statuses 429 and 5xx are classified retryable at `AssistantService.java:401–403`.
- IO failures, interruptions, and empty responses are converted to retryable exceptions at `AssistantService.java:236–244`.

**Inference**

- Retry behavior is intentionally restricted to transient provider failures and does not retry permanent provider rejection.

### Circuit breaker — PASS

**Observed evidence**

- `AssistantService.java:82` applies `@CircuitBreaker(name = "aiProvider")`.
- `frontdesk-service.yml:114–123` configures:
  - Health indicator enabled.
  - Count-based sliding window of 5 calls.
  - Minimum 3 calls.
  - 50% failure threshold.
  - 10-second open state.
  - One half-open permitted call.
  - Permanent AI provider errors ignored by the breaker.

**Inference**

- AI provider outages can trip a circuit breaker and fail fast after the configured threshold.

### Fallback error boundary — WARNING

**Observed evidence**

- `ResilientIntentFallbackHandler.java:27–30` catches any `RuntimeException` from the primary AI provider and invokes the deterministic fallback.
- No explicit `fallbackMethod` is present on the `@CircuitBreaker` or `@Retry` annotations.
- The deterministic fallback itself is allowed to propagate failures at line 30.
- `AssistantService.java:221–229` distinguishes retryable and permanent provider errors.

**Inference**

- The router-level fallback is the effective AI fallback boundary. The broad `RuntimeException` catch may classify programming or configuration errors as provider outages, reducing diagnostic precision.

### Rate limiting — WARNING

**Observed evidence**

- No AI-specific `RateLimiter` annotation or `resilience4j.ratelimiter.instances.aiProvider` configuration was found.
- API Gateway rate limiter configuration exists in `api-gateway` and `config-service`, but it is route-level rather than explicitly AI-provider-specific.
- `api-gateway/src/main/resources/application.yml` contains Redis rate-limiter properties.

**Inference**

- AI requests may receive general gateway throttling depending on route configuration, but the inspected AI service has no dedicated provider/tenant/user AI rate limiter.

### Metrics and observability — PASS

**Observed evidence**

- `AssistantService.java:58–61` defines token and request-duration metrics.
- Token usage is recorded at lines 230–231 and 255–271.
- Request duration is timed and tagged by provider and model at lines 217–251.
- `docker/prometheus/alert_rules.yml:81–94` alerts when any Resilience4j circuit breaker remains open.

**Inference**

- Basic AI usage and latency metrics exist, and generic circuit-breaker-open monitoring is present.

### AI-specific alerting — WARNING

**Observed evidence**

- The Prometheus rule inspected is generic: `CircuitBreakerOpen` based on `resilience4j_circuitbreaker_state`.
- No dedicated alert for AI provider error rate, retry exhaustion, request timeout rate, token usage, or AI fallback activation was found in the inspected alert rules.

**Inference**

- Operations can detect an open breaker but may not receive targeted alerts for degraded AI behavior before the breaker opens.

## Provider and configuration history

### WARNING

**Observed evidence**

- `V17__add_tenant_ai_assistant.sql` introduces:
  - `ai_enabled`
  - `ai_model`
  - `ai_instructions`
  - encrypted API-key storage.
- Subsequent migrations contain historical provider transitions:
  - `V19__replace_retired_groq_model.sql`
  - `V20__force_groq_ai_provider.sql`
  - `V21__migrate_ai_to_deepseek.sql`
  - `V22__use_ollama_by_default_keep_deepseek_option.sql`
  - `V23__use_qwen3_4b_instruct_q4.sql`
- `V21` documents disabling migrated legacy AI rows before DeepSeek credentials are installed.
- `V22` and `V23` configure local Ollama/Qwen defaults.

**Inference**

- Provider configuration has evolved through several migrations. The current runtime path supports local Ollama by default and DeepSeek for explicitly prefixed models, but the effective database migration state was not verified because this A.SPEC is read-only and no database inspection was authorized.

## Build/configuration coverage

### PASS

**Observed evidence**

- `frontdesk-service/build.gradle.kts:59` includes `spring-cloud-starter-circuitbreaker-resilience4j`.
- AI-specific Resilience4j configuration exists in `config-service/src/main/resources/config/frontdesk-service.yml`.
- AI-specific exception types exist:
  - `RetryableAiProviderException.java`
  - `PermanentAiProviderException.java`

**Inference**

- The required resilience dependency and named AI resilience configuration are present in source.

## Gaps and classifications

| Finding | Classification | Evidence-based assessment |
|---|---|---|
| Existing AI controller, service, provider selection, prompts, and tool handling | PASS | Located in `frontdesk-service` |
| Authenticated role and tenant propagation | PASS | Controller uses security context and role guard |
| Connection and request timeouts | PASS | 10-second connect and 30-second request timeout |
| Retry with transient-error classification | PASS | Three attempts with exponential backoff |
| AI circuit breaker | PASS | Named `aiProvider` breaker configured |
| Deterministic local fallback | PASS | Router fallback preserves operational context |
| Token, latency, and generic breaker metrics | PASS | Micrometer and Prometheus coverage found |
| Dedicated AI rate limiter | WARNING | No AI-specific Resilience4j rate limiter found |
| AI-specific degradation/fallback alerts | WARNING | Generic breaker alert only was found |
| Broad runtime-exception fallback boundary | WARNING | May mask non-provider programming/configuration failures |
| Current applied migration/provider state | UNKNOWN | Not verified in read-only source inspection |
| Production provider availability or end-to-end AI behavior | UNKNOWN | External providers were not called and runtime behavior was not tested |

## Contract verification

**PASS**

The A.SPEC-required diagnostic search was completed successfully after correcting the local search invocation. Repository evidence was inspected using safe read-only commands only. No implementation or configuration changes were made.
