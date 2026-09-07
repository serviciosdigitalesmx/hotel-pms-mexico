package com.hotelpms.frontdesk.assistant.engine;

import com.hotelpms.frontdesk.assistant.PermanentAiProviderException;
import com.hotelpms.frontdesk.assistant.RetryableAiProviderException;
import com.hotelpms.frontdesk.assistant.dto.AssistantChatResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.function.Supplier;

/** Executes primary intent resolution and deterministically falls back on provider failures. */
@Component
@Slf4j
public final class ResilientIntentFallbackHandler {

    /**
     * Resolves an intent through the primary provider and invokes the local resolver when it fails.
     *
     * <p>The fallback is deliberately supplied by the router so it retains session locking and tenant
     * context instead of bypassing the existing operational flow.
     *
     * @param primary supplier for the configured AI provider
     * @param fallback supplier for the deterministic local resolver
     * @return the assistant response chosen by the provider or the fallback
     */
    public AssistantChatResponse resolve(
            final Supplier<AssistantChatResponse> primary,
            final Supplier<AssistantChatResponse> fallback) {
        Objects.requireNonNull(primary, "primary");
        Objects.requireNonNull(fallback, "fallback");
        try {
            return primary.get();
        } catch (final RetryableAiProviderException
                | PermanentAiProviderException
                | CallNotPermittedException
                | RequestNotPermitted providerFailure) {
            log.warn("AI intent provider unavailable; using deterministic fallback | type={}",
                    providerFailure.getClass().getSimpleName());
            return fallback.get();
        }
    }
}
