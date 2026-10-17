package com.hotelpms.frontdesk.notifications;

import com.hotelpms.internalauth.feign.FeignAuthContext;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Supplies a narrowly scoped service identity when scheduled outbox delivery has no HTTP request. */
@Component
public class NotificationServiceAuthContext {
  private final ThreadLocal<FeignAuthContext> current = new ThreadLocal<>();

  public Optional<FeignAuthContext> resolve() {
    return Optional.ofNullable(current.get());
  }

  public <T> T asHotel(final UUID hotelId, final java.util.function.Supplier<T> operation) {
    final FeignAuthContext previous = current.get();
    current.set(new FeignAuthContext("frontdesk-service", "ROLE_SERVICE", hotelId.toString()));
    try {
      return operation.get();
    } finally {
      if (previous == null) current.remove();
      else current.set(previous);
    }
  }
}
