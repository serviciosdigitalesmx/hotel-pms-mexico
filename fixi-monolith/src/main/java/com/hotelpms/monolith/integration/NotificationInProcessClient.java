package com.hotelpms.monolith.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelpms.frontdesk.client.NotificationClient;
import com.hotelpms.frontdesk.client.dto.NotificationCheckinRequest;
import com.hotelpms.frontdesk.client.dto.NotificationCheckoutRequest;
import com.hotelpms.frontdesk.client.dto.NotificationEventRequest;
import com.hotelpms.frontdesk.client.dto.NotificationEventResponse;
import com.hotelpms.frontdesk.client.dto.NotificationQuotationRequest;
import com.hotelpms.frontdesk.client.dto.NotificationReservationRequest;
import com.hotelpms.notification.dto.EventNotificationRequest;
import com.hotelpms.notification.service.NotificationDeliveryService;
import com.hotelpms.notification.service.NotificationService;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

/** Direct adapter for notification delivery, retaining the durable outbox and transactional email service. */
@Configuration(proxyBeanMethods = false)
public class NotificationInProcessClient {

  @Bean
  NotificationClient monolithNotificationClient(
      final NotificationService notifications,
      final NotificationDeliveryService delivery,
      final ObjectMapper mapper) {
    return new NotificationClient() {
      @Override
      public NotificationEventResponse enqueueEvent(final NotificationEventRequest request) {
        final UUID authenticatedTenant = tenantId();
        if (!authenticatedTenant.equals(request.hotelId())) {
          throw new AccessDeniedException("CROSS_TENANT_NOTIFICATION_REJECTED");
        }
        final var persisted = delivery.enqueue(
            convert(request, EventNotificationRequest.class, mapper), authenticatedTenant);
        return convert(com.hotelpms.notification.dto.EventNotificationResponse.from(persisted),
            NotificationEventResponse.class, mapper);
      }

      @Override
      public boolean sendReservationConfirmed(final NotificationReservationRequest request) {
        notifications.sendReservationConfirmed(convert(request,
            com.hotelpms.notification.dto.ReservationConfirmedRequest.class, mapper));
        return true;
      }

      @Override
      public boolean sendCheckin(final NotificationCheckinRequest request) {
        notifications.sendCheckin(convert(request,
            com.hotelpms.notification.dto.CheckinNotificationRequest.class, mapper));
        return true;
      }

      @Override
      public boolean sendCheckout(final NotificationCheckoutRequest request) {
        notifications.sendCheckout(convert(request,
            com.hotelpms.notification.dto.CheckoutNotificationRequest.class, mapper));
        return true;
      }

      @Override
      public boolean sendQuotation(final NotificationQuotationRequest request) {
        notifications.sendQuotation(convert(request,
            com.hotelpms.notification.dto.QuotationNotificationRequest.class, mapper));
        return true;
      }
    };
  }

  private static UUID tenantId() {
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
