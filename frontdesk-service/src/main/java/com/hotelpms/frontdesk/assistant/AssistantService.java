package com.hotelpms.frontdesk.assistant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hotelpms.frontdesk.assistant.dto.AssistantChatRequest;
import com.hotelpms.frontdesk.assistant.dto.AssistantChatResponse;
import com.hotelpms.frontdesk.assistant.dto.AssistantMessage;
import com.hotelpms.frontdesk.assistant.dto.AssistantToolCall;
import com.hotelpms.frontdesk.exception.BadRequestException;
import com.hotelpms.frontdesk.exception.ExternalServiceException;
import com.hotelpms.frontdesk.stays.domain.HotelSettings;
import com.hotelpms.frontdesk.stays.repository.HotelSettingsRepository;
import com.hotelpms.frontdesk.stays.security.AlloggiatiCredentialEncryptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Calls the tenant-configured DeepSeek API over the allow-listed PMS tool adapter. */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssistantService {

    private static final URI DEEPSEEK_CHAT_URI = URI.create("https://api.deepseek.com/chat/completions");
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final int HTTP_SUCCESS_MIN = 200;
    private static final int HTTP_SUCCESS_MAX = 300;
    private static final int MAX_PROVIDER_LOG_BODY = 1500;
    private static final int MAX_PROVIDER_LOG_FIELD = 500;
    private static final String CONTENT_FIELD = "content";
    private static final String TOOL_CALLS_FIELD = "tool_calls";
    private static final String NAME_FIELD = "name";
    private static final String FUNCTION_FIELD = "function";
    private static final String USAGE_FIELD = "usage";
    private static final String PROVIDER_TAG = "provider";
    private static final String MODEL_TAG = "model";
    private static final String PROMPT_TOKENS_METRIC = "pms.ai.tokens.prompt";
    private static final String COMPLETION_TOKENS_METRIC = "pms.ai.tokens.completion";
    private static final String TOTAL_TOKENS_METRIC = "pms.ai.tokens.total";
    private static final String REQUEST_DURATION_METRIC = "pms.ai.requests.duration";
    private static final double TEMPERATURE = 0.1;
    private static final int MAX_OUTPUT_TOKENS = 1040;
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final HotelSettingsRepository hotelSettingsRepository;
    private final AlloggiatiCredentialEncryptor credentialEncryptor;
    private final AssistantToolCatalog toolCatalog;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    /**
     * Generates the next answer or exact PMS operation for one authenticated tenant.
     *
     * @param hotelId authenticated tenant
     * @param roles authenticated roles
     * @param conversation provider-neutral conversation
     * @return next assistant response
     */
    @CircuitBreaker(name = "aiProvider")
    @Retry(name = "aiProvider")
    @RateLimiter(name = "aiProvider")
    public AssistantChatResponse chat(
            final UUID hotelId,
            final Set<String> roles,
            final AssistantChatRequest conversation) {
        final HotelSettings settings = hotelSettingsRepository.findById(hotelId)
                .orElseThrow(() -> new BadRequestException("AI_ASSISTANT_NOT_CONFIGURED"));
        ensureConfigured(settings);
        final String selectedModel =
                safe(settings.getAiModel(), "qwen3:4b-instruct-2507-q4_K_M");
        final boolean deepSeek = isDeepSeekModel(selectedModel);

        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(
                deepSeek ? DEEPSEEK_CHAT_URI : ollamaChatUri())
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json");

        if (deepSeek) {
            final String apiKey = credentialEncryptor.decrypt(
                    settings.getAiApiKeyEncrypted());
            requestBuilder.header("Authorization", "Bearer " + apiKey);
        }

        final HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofString(
                        buildRequestBody(settings, roles, conversation.messages())))
                .build();
        return send(request, deepSeek ? "deepseek" : "ollama", selectedModel);
    }

    private void ensureConfigured(final HotelSettings settings) {
        if (!settings.isAiEnabled()) {
            throw new BadRequestException("AI_ASSISTANT_NOT_CONFIGURED");
        }
        if (isDeepSeekModel(settings.getAiModel())
                && !settings.hasAiApiKey()) {
            throw new IllegalStateException(
                    "AI_API_KEY_NOT_CONFIGURED_FOR_HOTEL");
        }
    }

    private String buildRequestBody(
            final HotelSettings settings,
            final Set<String> roles,
            final List<AssistantMessage> messages) {
        final ObjectNode body = objectMapper.createObjectNode();
        body.put(
                "model",
                safe(settings.getAiModel(), "qwen3:4b-instruct-2507-q4_K_M"));
        final String selectedModel =
                safe(settings.getAiModel(), "qwen3:4b-instruct-2507-q4_K_M");

        if (isDeepSeekModel(selectedModel)) {
            final ObjectNode thinking = objectMapper.createObjectNode();
            thinking.put("type", "disabled");
            body.set("thinking", thinking);
        } else {
            body.put("reasoning_effort", "none");
        }
        body.put("max_tokens", MAX_OUTPUT_TOKENS);
        body.put("temperature", TEMPERATURE);

        final ArrayNode providerMessages = body.putArray("messages");
        providerMessages.addObject()
                .put("role", "system")
                .put(CONTENT_FIELD, buildSystemPrompt(settings));
        messages.forEach(message -> providerMessages.add(toProviderMessage(message)));
        final ArrayNode tools = toolCatalog.toolsFor(roles);
        body.set("tools", tools);
        body.put("tool_choice", "auto");
        try {
            return objectMapper.writeValueAsString(body);
        } catch (final JsonProcessingException ex) {
            throw new ExternalServiceException("AI_REQUEST_SERIALIZATION_FAILED", ex);
        }
    }

    private ObjectNode toProviderMessage(final AssistantMessage message) {
        final ObjectNode providerMessage = objectMapper.createObjectNode();
        providerMessage.put("role", message.role());
        if (message.content() == null) {
            providerMessage.putNull(CONTENT_FIELD);
        } else {
            providerMessage.put(CONTENT_FIELD, message.content());
        }
        if ("tool".equals(message.role())) {
            providerMessage.put("tool_call_id", message.toolCallId());
            providerMessage.put(NAME_FIELD, message.toolName());
        }
        if (!message.toolCalls().isEmpty()) {
            final ArrayNode calls = providerMessage.putArray(TOOL_CALLS_FIELD);
            message.toolCalls().forEach(call -> calls.add(toProviderToolCall(call)));
        }
        return providerMessage;
    }

    private ObjectNode toProviderToolCall(final AssistantToolCall call) {
        final ObjectNode node = objectMapper.createObjectNode();
        node.put("id", call.id());
        node.put("type", FUNCTION_FIELD);
        final ObjectNode function = node.putObject(FUNCTION_FIELD);
        function.put(NAME_FIELD, call.name());
        function.put("arguments", writeArguments(call.arguments()));
        return node;
    }

    private String writeArguments(final JsonNode arguments) {
        try {
            return objectMapper.writeValueAsString(arguments);
        } catch (final JsonProcessingException ex) {
            throw new ExternalServiceException("AI_REQUEST_SERIALIZATION_FAILED", ex);
        }
    }

    private String buildSystemPrompt(final HotelSettings settings) {
        return "Eres el agente operativo del PMS de un hotel mexicano. Responde siempre en español. "
                + "Usa herramientas para toda consulta o acción sobre datos; no inventes IDs ni resultados. "
                + "Nunca pidas IDs técnicos al usuario: resuélvelos con las consultas del PMS. "
                + "En propuestas usa sólo valores dados por el usuario u obtenidos del PMS; nunca fabriques "
                + "nombres, apellidos, correos, teléfonos, fechas ni documentos de ejemplo. "
                + "Las consultas son automáticas. Las acciones son sólo propuestas y jamás se ejecutan hasta "
                + "que el usuario autenticado revise los parámetros y pulse el botón Confirmar y ejecutar. "
                + "No solicites ni proceses contraseñas, llaves API, tokens ni credenciales. No uses flujos "
                + "fiscales CFDI 4.0 no proporcionados por el usuario. Si faltan datos, consulta primero o pregunta al usuario. Después de un "
                + "resultado de herramienta, explica claramente qué ocurrió. Hotel: "
                + safe(settings.getHotelName(), "Hotel") + ". Hora local: "
                + ZonedDateTime.now(resolveZone(settings.getTimezone())) + ". Instrucciones: "
                + safe(settings.getAiInstructions(), "ninguna") + ".";
    }

    private AssistantChatResponse send(
            final HttpRequest request,
            final String provider,
            final String model) {
        final Timer.Sample timerSample = Timer.start(meterRegistry);
        try {
            final HttpResponse<String> response = HTTP_CLIENT.send(
                    request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < HTTP_SUCCESS_MIN || response.statusCode() >= HTTP_SUCCESS_MAX) {
                log.warn("AI provider rejected request with status {}: {}",
                        response.statusCode(), sanitizeProviderError(response.body()));
                log.warn("AI provider raw error body: {}", sanitizeRawProviderBody(response.body()));
                if (isRetryableProviderStatus(response.statusCode())) {
                    throw new RetryableAiProviderException("AI_PROVIDER_TRANSIENT_FAILURE");
                }
                throw new PermanentAiProviderException("AI_PROVIDER_REJECTED_REQUEST");
            }
            final JsonNode root = objectMapper.readTree(response.body());
            recordTokenUsage(root.path(USAGE_FIELD), provider, model);
            final JsonNode message = root
                    .path("choices").path(0).path("message");
            final String answer = message.path(CONTENT_FIELD).asText("");
            final List<AssistantToolCall> calls = parseToolCalls(message.path(TOOL_CALLS_FIELD));
            if (answer.isBlank() && calls.isEmpty()) {
                throw new RetryableAiProviderException("AI_PROVIDER_EMPTY_RESPONSE");
            }
            return new AssistantChatResponse(answer, calls);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RetryableAiProviderException("AI_PROVIDER_INTERRUPTED", ex);
        } catch (final IOException ex) {
            throw new RetryableAiProviderException("AI_PROVIDER_UNAVAILABLE", ex);
        } finally {
            final Timer timer = Timer.builder(REQUEST_DURATION_METRIC)
                    .description("AI provider request duration")
                    .tags(PROVIDER_TAG, provider, MODEL_TAG, model)
                    .publishPercentiles(0.5, 0.95, 0.99)
                    .register(meterRegistry);
            timerSample.stop(timer);
        }
    }

    void recordTokenUsage(
            final JsonNode usage,
            final String provider,
            final String model) {
        recordTokenMetric(PROMPT_TOKENS_METRIC, usage.path("prompt_tokens"), provider, model);
        recordTokenMetric(COMPLETION_TOKENS_METRIC, usage.path("completion_tokens"), provider, model);
        recordTokenMetric(TOTAL_TOKENS_METRIC, usage.path("total_tokens"), provider, model);
    }

    private void recordTokenMetric(
            final String metricName,
            final JsonNode tokenCount,
            final String provider,
            final String model) {
        if (tokenCount.isIntegralNumber() && tokenCount.asLong() >= 0) {
            meterRegistry.counter(metricName, PROVIDER_TAG, provider, MODEL_TAG, model)
                    .increment(tokenCount.asLong());
        }
    }

    private List<AssistantToolCall> parseToolCalls(final JsonNode toolCalls) {
        final List<AssistantToolCall> result = new ArrayList<>();
        if (!toolCalls.isArray()) {
            return result;
        }
        for (final JsonNode call : toolCalls) {
            final String id = call.path("id").asText();
            final String name = call.path(FUNCTION_FIELD).path(NAME_FIELD).asText();
            final String rawArguments = call.path(FUNCTION_FIELD).path("arguments").asText("{}");
            try {
                result.add(new AssistantToolCall(
                        id,
                        name,
                        objectMapper.readTree(rawArguments),
                        toolCatalog.requiresConfirmation(name)));
            } catch (final JsonProcessingException ex) {
                throw new ExternalServiceException("AI_PROVIDER_INVALID_TOOL_ARGUMENTS", ex);
            }
        }
        return result;
    }






    /**
     * Resolves the installation-level Ollama OpenAI-compatible endpoint.
     * The model itself remains tenant-configurable in HotelSettings.
     *
     * @return Ollama chat-completions endpoint
     */
    private static URI ollamaChatUri() {
        String baseUrl = System.getenv("OLLAMA_BASE_URL");

        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://host.docker.internal:11434";
        }

        baseUrl = baseUrl.trim();

        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        if (baseUrl.endsWith("/v1")) {
            return URI.create(baseUrl + "/chat/completions");
        }

        return URI.create(baseUrl + "/v1/chat/completions");
    }

    /**
     * DeepSeek model identifiers are provider-qualified by their canonical
     * model prefix. Every other configured model is served by local Ollama.
     *
     * @param model tenant-selected AI model
     * @return true when the request must use DeepSeek
     */
    private static boolean isDeepSeekModel(final String model) {
        return model != null
                && model.trim().toLowerCase(java.util.Locale.ROOT)
                        .startsWith("deepseek-");
    }

    private static ZoneId resolveZone(final String timezone) {
        try {
            return ZoneId.of(safe(timezone, "America/Monterrey"));
        } catch (final DateTimeException ignored) {
            return ZoneId.of("America/Monterrey");
        }
    }

    private static String safe(final String value, final String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String sanitizeProviderError(final String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "empty provider response";
        }

        try {
            final JsonNode root = objectMapper.readTree(responseBody);
            final JsonNode error = root.path("error");

            final String message = error.path("message").asText("");
            final String type = error.path("type").asText("");
            final String code = error.path("code").asText("");

            return "message=" + safeForLog(message)
                    + ", type=" + safeForLog(type)
                    + ", code=" + safeForLog(code);
        } catch (final JsonProcessingException ignored) {
            return "unparseable provider error";
        }
    }

    private static String sanitizeRawProviderBody(final String value) {
        if (value == null || value.isBlank()) {
            return "[EMPTY_BODY]";
        }

        final String sanitized = value
                .replaceAll("[\\r\\n\\t]", " ")
                .replaceAll("(?i)(gsk_|AIza)[A-Za-z0-9._\\-]+", "[REDACTED]");

        return sanitized.substring(0, Math.min(sanitized.length(), MAX_PROVIDER_LOG_BODY));
    }

    private static String safeForLog(final String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }

        return value
                .replaceAll("[\\r\\n\\t]", " ")
                .replaceAll("(?i)(gsk_|AIza)[A-Za-z0-9._\\-]+", "[REDACTED]")
                .substring(0, Math.min(value.length(), MAX_PROVIDER_LOG_FIELD));
    }

    static boolean requiresInitialTool(final List<AssistantMessage> messages) {
        return messages.stream().noneMatch(message -> "tool".equals(message.role()));
    }

    static boolean isRetryableProviderStatus(final int statusCode) {
        return statusCode == 429 || statusCode >= 500;
    }
}
