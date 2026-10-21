package com.hotelpms.monolith.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelpms.frontdesk.client.NotificationClient;
import com.hotelpms.frontdesk.client.dto.NotificationCheckinRequest;
import com.hotelpms.frontdesk.client.dto.NotificationCheckoutRequest;
import com.hotelpms.frontdesk.client.dto.NotificationEventRequest;
import com.hotelpms.frontdesk.client.dto.NotificationEventResponse;
import com.hotelpms.frontdesk.client.dto.NotificationQuotationRequest;
import com.hotelpms.frontdesk.client.dto.NotificationReservationRequest;
import com.hotelpms.frontdesk.notifications.NotificationServiceAuthContext;
import com.hotelpms.internalauth.feign.FeignAuthContext;
import com.hotelpms.notification.dto.EventNotificationRequest;
import com.hotelpms.notification.service.NotificationDeliveryService;
import com.hotelpms.notification.service.NotificationService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.UUID;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

/** Direct adapter for notification delivery, retaining the durable outbox and transactional email service. */
@Configuration(proxyBeanMethods = false)
public class NotificationInProcessClient {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationInProcessClient.class);

  @Bean
  MonolithNotificationClient monolithNotificationClient(
      final NotificationService notifications,
      final NotificationDeliveryService delivery,
      final NotificationServiceAuthContext scheduledAuthContext,
      final CircuitBreakerRegistry circuitBreakers,
      final ObjectMapper mapper) {
    return new MonolithNotificationClient(
        notifications, delivery, scheduledAuthContext, circuitBreakers, mapper);
  }

  /** Concrete adapter type allows Spring AOT to generate its resilience proxy for Native Image. */
  public static class MonolithNotificationClient implements NotificationClient {
    private final NotificationService notifications;
    private final NotificationDeliveryService delivery;
    private final NotificationServiceAuthContext scheduledAuthContext;
    private final CircuitBreakerRegistry circuitBreakers;
    private final ObjectMapper mapper;

    public MonolithNotificationClient(
        final NotificationService notifications,
        final NotificationDeliveryService delivery,
        final NotificationServiceAuthContext scheduledAuthContext,
        final CircuitBreakerRegistry circuitBreakers,
        final ObjectMapper mapper) {
      this.notifications = notifications;
      this.delivery = delivery;
      this.scheduledAuthContext = scheduledAuthContext;
      this.circuitBreakers = circuitBreakers;
      this.mapper = mapper;
    }

    @Override
    public NotificationEventResponse enqueueEvent(final NotificationEventRequest request) {
      final UUID authenticatedTenant = authenticatedTenant(scheduledAuthContext);
      if (!authenticatedTenant.equals(request.hotelId())) {
        throw new AccessDeniedException("CROSS_TENANT_NOTIFICATION_REJECTED");
      }
      return withNotificationFallback(
          "enqueue-event",
          () -> {
            final var persisted = delivery.enqueue(
                convert(request, EventNotificationRequest.class, mapper), authenticatedTenant);
            return convert(com.hotelpms.notification.dto.EventNotificationResponse.from(persisted),
                NotificationEventResponse.class, mapper);
          },
          null);
    }

    @Override
    public boolean sendReservationConfirmed(final NotificationReservationRequest request) {
      authenticatedTenant(scheduledAuthContext);
      return withNotificationFallback(
          "reservation-confirmed",
          () -> {
            notifications.sendReservationConfirmed(convert(request,
                com.hotelpms.notification.dto.ReservationConfirmedRequest.class, mapper));
            return true;
          },
          false);
    }

    @Override
    public boolean sendCheckin(final NotificationCheckinRequest request) {
      authenticatedTenant(scheduledAuthContext);
      return withNotificationFallback(
          "checkin",
          () -> {
            notifications.sendCheckin(convert(request,
                com.hotelpms.notification.dto.CheckinNotificationRequest.class, mapper));
            return true;
          },
          false);
    }

    @Override
    public boolean sendCheckout(final NotificationCheckoutRequest request) {
      authenticatedTenant(scheduledAuthContext);
      return withNotificationFallback(
          "checkout",
          () -> {
            notifications.sendCheckout(convert(request,
                com.hotelpms.notification.dto.CheckoutNotificationRequest.class, mapper));
            return true;
          },
          false);
    }

    @Override
    public boolean sendQuotation(final NotificationQuotationRequest request) {
      authenticatedTenant(scheduledAuthContext);
      return withNotificationFallback(
          "quotation",
          () -> {
            notifications.sendQuotation(convert(request,
                com.hotelpms.notification.dto.QuotationNotificationRequest.class, mapper));
            return true;
          },
          false);
    }

    private <T> T withNotificationFallback(
        final String operation, final Supplier<T> action, final T fallback) {
      final CircuitBreaker circuitBreaker = circuitBreakers.circuitBreaker("notificationService");
      try {
        return CircuitBreaker.decorateSupplier(circuitBreaker, action).get();
      } catch (final RuntimeException exception) {
        LOG.warn("Notification operation {} deferred by circuit breaker: {}",
            operation, exception.getMessage());
        return fallback;
      }
    }
  }

  private static UUID authenticatedTenant(
      final NotificationServiceAuthContext scheduledAuthContext) {
    final var scheduledIdentity = scheduledAuthContext.resolve();
    if (scheduledIdentity.isPresent()) {
      final FeignAuthContext identity = scheduledIdentity.get();
      if (!"frontdesk-service".equals(identity.user())
          || !"ROLE_SERVICE".equals(identity.role())
          || identity.hotelId() == null) {
        throw new AccessDeniedException("INVALID_SCHEDULED_NOTIFICATION_IDENTITY");
      }
      return UUID.fromString(identity.hotelId());
    }
    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getDetails() instanceof String tenant)) {
      throw new AccessDeniedException("TENANT_CONTEXT_REQUIRED");
    }
    return UUID.fromString(tenant);
  }

  private static <T> T convert(final Object source, final Class<T> target, final ObjectMapper mapper) {
    return mapper.convertValue(source, target);
  }
}
