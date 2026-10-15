package com.hotelpms.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelpms.notification.domain.NotificationDelivery;
import com.hotelpms.notification.domain.NotificationDelivery.Status;
import com.hotelpms.notification.dto.CheckinNotificationRequest;
import com.hotelpms.notification.dto.CheckoutNotificationRequest;
import com.hotelpms.notification.dto.EventNotificationRequest;
import com.hotelpms.notification.dto.QuotationNotificationRequest;
import com.hotelpms.notification.dto.ReservationConfirmedRequest;
import com.hotelpms.notification.dto.OperationalEmailRequest;
import com.hotelpms.notification.repository.NotificationDeliveryRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Persists and delivers tenant-scoped notification events idempotently. */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDeliveryService {
  private static final int MAX_ATTEMPTS = 5;
  private static final int BATCH_SIZE = 10;

  private final NotificationDeliveryRepository repository;
  private final NotificationService notificationService;
  private final ObjectMapper objectMapper;
  private final Validator validator;

  @Transactional
  public NotificationDelivery enqueue(
      final EventNotificationRequest request, final UUID authenticatedHotelId) {
    if (!request.hotelId().equals(authenticatedHotelId)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Notification tenant does not match authenticated tenant");
    }
    repository.enqueueIfAbsent(
        authenticatedHotelId, request.eventKey(), request.eventType(), request.payload());
    return repository.findByHotelIdAndEventKey(authenticatedHotelId, request.eventKey())
        .orElseThrow(() -> new IllegalStateException("Notification event was not persisted"));
  }

  @Transactional
  public int deliverPending() {
    final List<NotificationDelivery> batch = repository.lockNextDeliveries(
        MAX_ATTEMPTS, PageRequest.of(0, BATCH_SIZE));
    int delivered = 0;
    for (final NotificationDelivery delivery : batch) {
      delivery.setAttempts(delivery.getAttempts() + 1);
      try {
        dispatch(delivery);
        delivery.setStatus(Status.SENT);
        delivery.setSentAt(LocalDateTime.now());
        delivery.setLastError(null);
        delivery.setNextAttemptAt(null);
        delivery.setPayload("{}");
        delivered++;
      } catch (final Exception exception) {
        delivery.setStatus(Status.FAILED);
        delivery.setLastError(summarizeError(exception));
        delivery.setNextAttemptAt(LocalDateTime.now().plusSeconds(retryDelaySeconds(delivery.getAttempts())));
        log.warn("Notification delivery failed [eventKey={}, attempt={}]: {}",
            delivery.getEventKey(), delivery.getAttempts(), delivery.getLastError());
      }
    }
    repository.saveAll(batch);
    return delivered;
  }

  private void dispatch(final NotificationDelivery delivery) throws Exception {
    switch (delivery.getEventType()) {
      case "RESERVATION_CONFIRMED" -> notificationService.sendReservationConfirmed(
          validatedPayload(delivery, ReservationConfirmedRequest.class));
      case "CHECKIN" -> notificationService.sendCheckin(
          validatedPayload(delivery, CheckinNotificationRequest.class));
      case "CHECKOUT" -> notificationService.sendCheckout(
          validatedPayload(delivery, CheckoutNotificationRequest.class));
      case "QUOTATION" -> notificationService.sendQuotation(
          validatedPayload(delivery, QuotationNotificationRequest.class));
      case "OPERATIONAL_EMAIL" -> notificationService.sendOperationalEmail(
          validatedPayload(delivery, OperationalEmailRequest.class));
      default -> throw new IllegalArgumentException(
          "Unsupported notification event type: " + delivery.getEventType());
    }
  }

  private <T> T validatedPayload(final NotificationDelivery delivery, final Class<T> payloadType)
      throws Exception {
    final T payload = objectMapper.readValue(delivery.getPayload(), payloadType);
    final Set<ConstraintViolation<T>> violations = validator.validate(payload);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
    return payload;
  }

  private String summarizeError(final Exception exception) {
    final String message = exception.getMessage() == null
        ? exception.getClass().getSimpleName()
        : exception.getMessage();
    return message.length() <= 2000 ? message : message.substring(0, 2000);
  }

  private long retryDelaySeconds(final int attempt) {
    return Math.min(900L, 15L << Math.max(0, attempt - 1));
  }
}
