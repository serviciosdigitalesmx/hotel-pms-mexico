package com.hotelpms.frontdesk.notifications;

import com.hotelpms.internalauth.feign.FeignAuthContext;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Supplies a narrowly scoped service identity when scheduled outbox delivery has no HTTP request.
 */
@Component
public class NotificationServiceAuthContext {
    private final ThreadLocal<FeignAuthContext> current = new ThreadLocal<>();

    /**
     * Resolves the service identity currently scoped to this thread.
     *
     * @implSpec Overrides must preserve thread-local scope and return empty when no identity exists.
     * @return the current internal identity, if present
     */
    public Optional<FeignAuthContext> resolve() {
        return Optional.ofNullable(current.get());
    }

    /**
     * Runs an operation with a service identity scoped to a tenant.
     *
     * @implSpec Overrides must restore the prior identity even when the operation throws, and must
     *     not broaden the requested tenant scope.
     * @param hotelId tenant whose internal identity is required
     * @param operation work to run with that identity
     * @param <T> result type
     * @return the operation result
     */
    public <T> T asHotel(final UUID hotelId, final java.util.function.Supplier<T> operation) {
        final FeignAuthContext previous = current.get();
        current.set(new FeignAuthContext("frontdesk-service", "ROLE_SERVICE", hotelId.toString()));
        try {
            return operation.get();
        } finally {
            if (previous == null) {
                current.remove();
            } else {
                current.set(previous);
            }
        }
    }
}
