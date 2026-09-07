package com.hotelpms.frontdesk.assistant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelpms.frontdesk.assistant.dto.AssistantMessage;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AssistantServiceTest {

    private static final int RATE_LIMIT_STATUS = 429;
    private static final int SERVER_ERROR_STATUS = 500;
    private static final int GATEWAY_TIMEOUT_STATUS = 504;
    private static final int BAD_REQUEST_STATUS = 400;
    private static final int UNAUTHORIZED_STATUS = 401;
    private static final int FORBIDDEN_STATUS = 403;
    private static final int NOT_FOUND_STATUS = 404;
    private static final int PROMPT_TOKENS = 12;
    private static final int COMPLETION_TOKENS = 7;
    private static final int TOTAL_TOKENS = 19;
    private static final String OLLAMA = "ollama";
    private static final String QWEN_MODEL = "qwen3:4b";
    private static final String PROVIDER_TAG = "provider";
    private static final String MODEL_TAG = "model";

    @Test
    void requiresToolForANewOperatorMessage() {
        final List<AssistantMessage> messages = List.of(message("user"));

        assertThat(AssistantService.requiresInitialTool(messages)).isTrue();
    }

    @Test
    void allowsModelToAnswerAfterARealToolResult() {
        final List<AssistantMessage> messages = List.of(message("user"), message("tool"));

        assertThat(AssistantService.requiresInitialTool(messages)).isFalse();
    }

    @Test
    void retriesRateLimitAndServerFailuresOnly() {
        assertThat(AssistantService.isRetryableProviderStatus(RATE_LIMIT_STATUS)).isTrue();
        assertThat(AssistantService.isRetryableProviderStatus(SERVER_ERROR_STATUS)).isTrue();
        assertThat(AssistantService.isRetryableProviderStatus(GATEWAY_TIMEOUT_STATUS)).isTrue();
        assertThat(AssistantService.isRetryableProviderStatus(BAD_REQUEST_STATUS)).isFalse();
        assertThat(AssistantService.isRetryableProviderStatus(UNAUTHORIZED_STATUS)).isFalse();
        assertThat(AssistantService.isRetryableProviderStatus(FORBIDDEN_STATUS)).isFalse();
        assertThat(AssistantService.isRetryableProviderStatus(NOT_FOUND_STATUS)).isFalse();
    }

    @Test
    void recordsTokenUsageByProviderAndModel() throws JsonProcessingException {
        final SimpleMeterRegistry registry = new SimpleMeterRegistry();
        final AssistantService service = new AssistantService(
                null, null, null, new ObjectMapper(), registry);

        service.recordTokenUsage(new ObjectMapper().readTree("""
                {"prompt_tokens": 12, "completion_tokens": 7, "total_tokens": 19}
                """), OLLAMA, QWEN_MODEL);

        assertThat(registry.get("pms.ai.tokens.prompt")
                .tag(PROVIDER_TAG, OLLAMA)
                .tag(MODEL_TAG, QWEN_MODEL)
                .counter().count()).isEqualTo(PROMPT_TOKENS);
        assertThat(registry.get("pms.ai.tokens.completion")
                .tag(PROVIDER_TAG, OLLAMA)
                .tag(MODEL_TAG, QWEN_MODEL)
                .counter().count()).isEqualTo(COMPLETION_TOKENS);
        assertThat(registry.get("pms.ai.tokens.total")
                .tag(PROVIDER_TAG, OLLAMA)
                .tag(MODEL_TAG, QWEN_MODEL)
                .counter().count()).isEqualTo(TOTAL_TOKENS);
    }

    private static AssistantMessage message(final String role) {
        return new AssistantMessage(role, "contenido", null, null, List.of());
    }
}
