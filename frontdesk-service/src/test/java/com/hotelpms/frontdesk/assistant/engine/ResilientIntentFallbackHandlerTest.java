package com.hotelpms.frontdesk.assistant.engine;

import com.hotelpms.frontdesk.assistant.RetryableAiProviderException;
import com.hotelpms.frontdesk.assistant.dto.AssistantChatResponse;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResilientIntentFallbackHandlerTest {

    private final ResilientIntentFallbackHandler handler = new ResilientIntentFallbackHandler();

    @Test
    void returnsPrimaryResponseWhenProviderSucceeds() {
        final AssistantChatResponse primary = response("primary");

        assertThat(handler.resolve(() -> primary, () -> response("fallback"))).isSameAs(primary);
    }

    @Test
    void returnsDeterministicFallbackWhenProviderFails() {
        final AtomicBoolean fallbackCalled = new AtomicBoolean();

        final AssistantChatResponse result = handler.resolve(
                () -> { throw new RetryableAiProviderException("provider unavailable"); },
                () -> {
                    fallbackCalled.set(true);
                    return response("deterministic");
                });

        assertThat(result.answer()).isEqualTo("deterministic");
        assertThat(fallbackCalled).isTrue();
    }

    @Test
    void propagatesFallbackFailureWithoutMaskingIt() {
        assertThatThrownBy(() -> handler.resolve(
                () -> { throw new RetryableAiProviderException("provider unavailable"); },
                () -> { throw new IllegalArgumentException("fallback failed"); }))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("fallback failed");
    }

    @Test
    void propagatesNonProviderErrorsInsteadOfUsingFallback() {
        assertThatThrownBy(() -> handler.resolve(
                () -> { throw new IllegalStateException("programming error"); },
                () -> response("must not run")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("programming error");
    }

    @Test
    void usesFallbackWhenRateLimiterRejectsCall() {
        assertThat(handler.resolve(
                () -> { throw RequestNotPermitted.createRequestNotPermitted(RateLimiter.ofDefaults("test")); },
                () -> response("rate limited fallback")).answer())
                .isEqualTo("rate limited fallback");
    }

    private static AssistantChatResponse response(final String answer) {
        return new AssistantChatResponse(answer, List.of());
    }
}
