package com.hotelpms.notification.config;

import com.hotelpms.notification.dto.CheckinNotificationRequest;
import com.hotelpms.notification.dto.CheckoutNotificationRequest;
import com.hotelpms.notification.dto.OperationalEmailRequest;
import com.hotelpms.notification.dto.QuotationNotificationRequest;
import com.hotelpms.notification.dto.ReservationConfirmedRequest;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.BindingReflectionHintsRegistrar;

/** Reflection metadata for notification payloads decoded dynamically from durable outbox rows. */
public final class NotificationRuntimeHints implements RuntimeHintsRegistrar {
  private static final BindingReflectionHintsRegistrar BINDING_HINTS =
      new BindingReflectionHintsRegistrar();

  @Override
  public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
    BINDING_HINTS.registerReflectionHints(hints.reflection(),
        CheckinNotificationRequest.class,
        CheckoutNotificationRequest.class,
        OperationalEmailRequest.class,
        QuotationNotificationRequest.class,
        ReservationConfirmedRequest.class);
  }
}
