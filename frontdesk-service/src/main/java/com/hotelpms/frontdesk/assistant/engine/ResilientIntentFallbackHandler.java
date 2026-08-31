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
     * The fallback is deliberately supplied by the router so it retains session locking and tenant
     * context instead of bypassing the existing operational flow.
     */
    public AssistantChatResponse resolve(
            final Supplier<AssistantChatResponse> primary,
            final Supplier<AssistantChatResponse> fallback) {
        Objects.requireNonNull(primary, "primary");
        Objects.requireNonNull(fallback, "fallback");
        try {
            return primary.get();
        } catch (final RuntimeException providerFailure) {
            if (!isProviderFailure(providerFailure)) {
                log.error("AI intent fallback boundary rejected non-provider error | type={}",
                        providerFailure.getClass().getSimpleName(), providerFailure);
                throw providerFailure;
            }
            log.warn("AI intent provider unavailable; using deterministic fallback | type={}",
                    providerFailure.getClass().getSimpleName());
            return fallback.get();
        }
    }

    private static boolean isProviderFailure(final RuntimeException failure) {
        return failure instanceof RetryableAiProviderException
                || failure instanceof PermanentAiProviderException
                || failure instanceof CallNotPermittedException
                || failure instanceof RequestNotPermitted;
    }
}
