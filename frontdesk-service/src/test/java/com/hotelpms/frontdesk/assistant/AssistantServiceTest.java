package com.hotelpms.frontdesk.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelpms.frontdesk.assistant.dto.AssistantMessage;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AssistantServiceTest {

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
        assertThat(AssistantService.isRetryableProviderStatus(429)).isTrue();
        assertThat(AssistantService.isRetryableProviderStatus(500)).isTrue();
        assertThat(AssistantService.isRetryableProviderStatus(504)).isTrue();
        assertThat(AssistantService.isRetryableProviderStatus(400)).isFalse();
        assertThat(AssistantService.isRetryableProviderStatus(401)).isFalse();
        assertThat(AssistantService.isRetryableProviderStatus(403)).isFalse();
        assertThat(AssistantService.isRetryableProviderStatus(404)).isFalse();
    }

    @Test
    void recordsTokenUsageByProviderAndModel() throws Exception {
        final SimpleMeterRegistry registry = new SimpleMeterRegistry();
        final AssistantService service = new AssistantService(
                null, null, null, new ObjectMapper(), registry);

        service.recordTokenUsage(new ObjectMapper().readTree("""
                {"prompt_tokens": 12, "completion_tokens": 7, "total_tokens": 19}
                """), "ollama", "qwen3:4b");

        assertThat(registry.get("pms.ai.tokens.prompt").tag("provider", "ollama")
                .tag("model", "qwen3:4b").counter().count()).isEqualTo(12);
        assertThat(registry.get("pms.ai.tokens.completion").tag("provider", "ollama")
                .tag("model", "qwen3:4b").counter().count()).isEqualTo(7);
        assertThat(registry.get("pms.ai.tokens.total").tag("provider", "ollama")
                .tag("model", "qwen3:4b").counter().count()).isEqualTo(19);
    }

    private static AssistantMessage message(final String role) {
        return new AssistantMessage(role, "contenido", null, null, List.of());
    }
}
