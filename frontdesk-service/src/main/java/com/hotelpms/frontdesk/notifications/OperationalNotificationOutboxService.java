package com.hotelpms.frontdesk.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelpms.frontdesk.client.NotificationClient;
import com.hotelpms.frontdesk.client.dto.NotificationEventRequest;
import com.hotelpms.frontdesk.client.dto.NotificationEventResponse;
import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrder;
import com.hotelpms.frontdesk.serviceorder.repository.ServiceOrderRepository;

import feign.FeignException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Persists operational alerts with the business transaction and retries forwarding to the mail
 * outbox.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OperationalNotificationOutboxService {
    private static final int MAX_FORWARD_ATTEMPTS = 12;
    private static final int BATCH_SIZE = 10;
    private static final int MAX_ERROR_LENGTH = 2000;
    private static final long MAX_RETRY_DELAY_SECONDS = 3600L;
    private static final long INITIAL_RETRY_DELAY_SECONDS = 30L;
    private static final int MAX_BACKOFF_SHIFT = 7;

    private final OperationalNotificationPreferencesService preferences;
    private final OperationalNotificationOutboxRepository repository;
    private final NotificationClient notificationClient;
    private final NotificationServiceAuthContext authContext;
    private final ObjectMapper objectMapper;
    private final ServiceOrderRepository serviceOrders;

    /**
     * Enqueues recipient-scoped messages atomically with an order or inventory mutation.
     *
     * @implSpec Overrides must retain tenant/event preference filtering and idempotent recipient
     *     keys.
     * @param hotelId authenticated tenant identifier
     * @param eventType operational event key
     * @param subject human-readable notification subject
     * @param message notification body
     * @param idempotencyKey stable source event key
     */
    @Transactional
    public void enqueue(
            final UUID hotelId,
            final String eventType,
            final String subject,
            final String message,
            final String idempotencyKey) {
        final Map<String, Map<String, Boolean>> channels = preferences.get(hotelId);
        if (!Boolean.TRUE.equals(channels.getOrDefault(eventType, Map.of()).get("EMAIL"))) {
            return;
        }
        final List<String> recipients =
                preferences.getEmailRecipients(hotelId).getOrDefault(eventType, List.of());
        for (final String recipient : recipients) {
            final String recipientKey =
                    UUID.nameUUIDFromBytes(
                                    (idempotencyKey + ":" + recipient)
                                            .getBytes(java.nio.charset.StandardCharsets.UTF_8))
                            .toString();
            final String emailPayload =
                    serialize(
                            Map.of(
                                    "recipient",
                                    recipient,
                                    "subject",
                                    "Fixi: " + subject,
                                    "message",
                                    message,
                                    "hotelName",
                                    "Fixi"));
            final NotificationEventRequest notificationEvent =
                    new NotificationEventRequest(
                            hotelId, recipientKey, "OPERATIONAL_EMAIL", emailPayload);
            repository.insertIfAbsent(
                    UUID.randomUUID(),
                    hotelId,
                    recipientKey,
                    idempotencyKey,
                    eventType,
                    serialize(notificationEvent));
        }
    }

    /**
     * Creates an alert for each overdue order using a stable promised-date key.
     *
     * @implSpec Overrides must retain the tenant-specific order query and stable idempotency key.
     * @return number of overdue orders processed
     */
    @Scheduled(fixedDelayString = "${fixi.notifications.due-scan-ms:120000}")
    @Transactional
    public int enqueueOverdueOrders() {
        final List<ServiceOrder> overdue = serviceOrders.findOverdueOrders(LocalDateTime.now());
        for (final ServiceOrder order : overdue) {
            enqueue(
                    order.getHotelId(),
                    "DELIVERY_DUE",
                    "Fecha compromiso vencida",
                    "La orden " + order.getId() + " venció el " + order.getPromisedAt() + ".",
                    "DELIVERY_DUE:" + order.getId() + ":" + order.getPromisedAt().toLocalDate());
        }
        return overdue.size();
    }

    /**
     * Forwards committed outbox rows; the downstream service then owns SMTP delivery and retries.
     *
     * @implSpec Overrides must preserve row locking, bounded retries, and internal tenant
     *     authentication.
     * @return number of events accepted by notification-service
     */
    @Scheduled(fixedDelayString = "${fixi.notifications.outbox-poll-ms:30000}")
    @Transactional
    public int forwardDue() {
        final List<OperationalNotificationOutbox> batch =
                repository.lockDue(MAX_FORWARD_ATTEMPTS, PageRequest.of(0, BATCH_SIZE));
        int forwarded = 0;
        for (final OperationalNotificationOutbox outbox : batch) {
            outbox.setAttempts(outbox.getAttempts() + 1);
            try {
                final NotificationEventRequest event =
                        objectMapper.readValue(outbox.getPayload(), NotificationEventRequest.class);
                final NotificationEventResponse response =
                        authContext.asHotel(
                                outbox.getHotelId(), () -> notificationClient.enqueueEvent(event));
                if (response == null) {
                    throw new IllegalStateException("NOTIFICATION_EVENT_NOT_ACCEPTED");
                }
                outbox.setStatus(OperationalNotificationOutbox.Status.SENT);
                outbox.setSentAt(LocalDateTime.now());
                outbox.setNextAttemptAt(null);
                outbox.setLastError(null);
                outbox.setPayload("{}");
                forwarded++;
            } catch (
                    final JsonProcessingException | FeignException | IllegalStateException exception) {
                outbox.setStatus(OperationalNotificationOutbox.Status.FAILED);
                outbox.setLastError(summarizeError(exception));
                outbox.setNextAttemptAt(
                        LocalDateTime.now()
                                .plusSeconds(
                                        Math.min(
                                                MAX_RETRY_DELAY_SECONDS,
                                                INITIAL_RETRY_DELAY_SECONDS
                                                        << Math.min(
                                                                MAX_BACKOFF_SHIFT,
                                                                outbox.getAttempts() - 1))));
                log.warn(
                        "Operational notification forwarding failed [eventKey={}, attempt={}]: {}",
                        outbox.getEventKey(),
                        outbox.getAttempts(),
                        outbox.getLastError());
            }
        }
        repository.saveAll(batch);
        return forwarded;
    }

    private String serialize(final Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (final JsonProcessingException exception) {
            throw new IllegalStateException(
                    "OPERATIONAL_NOTIFICATION_SERIALIZATION_FAILED", exception);
        }
    }

    private String summarizeError(final Exception exception) {
        final String message =
                exception.getMessage() == null
                        ? exception.getClass().getSimpleName()
                        : exception.getMessage();
        return message.length() <= MAX_ERROR_LENGTH
                ? message
                : message.substring(0, MAX_ERROR_LENGTH);
    }
}
