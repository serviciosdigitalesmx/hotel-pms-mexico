package com.hotelpms.frontdesk.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelpms.frontdesk.client.NotificationClient;
import com.hotelpms.frontdesk.client.dto.NotificationEventRequest;
import com.hotelpms.frontdesk.client.dto.NotificationEventResponse;
import com.hotelpms.frontdesk.serviceorder.repository.ServiceOrderRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class OperationalNotificationOutboxServiceTest {
  private static final UUID HOTEL_ID = UUID.randomUUID();

  @Mock private OperationalNotificationPreferencesService preferences;
  @Mock private OperationalNotificationOutboxRepository repository;
  @Mock private NotificationClient notificationClient;
  @Mock private ServiceOrderRepository serviceOrders;

  private OperationalNotificationOutboxService service;

  @BeforeEach
  void setUp() {
    service = new OperationalNotificationOutboxService(preferences, repository, notificationClient,
        new NotificationServiceAuthContext(), new ObjectMapper(), serviceOrders);
  }

  @Test
  void persistsRecipientScopedMessageWhenEmailIsEnabled() throws Exception {
    when(preferences.get(HOTEL_ID)).thenReturn(Map.of("ORDER_ASSIGNED", Map.of("EMAIL", true)));
    when(preferences.getEmailRecipients(HOTEL_ID))
        .thenReturn(Map.of("ORDER_ASSIGNED", List.of("ops@example.com")));

    service.enqueue(HOTEL_ID, "ORDER_ASSIGNED", "Orden asignada", "Orden 123", "event-123");

    final ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
    verify(repository).insertIfAbsent(any(UUID.class), eq(HOTEL_ID), anyString(), eq("event-123"),
        eq("ORDER_ASSIGNED"), payload.capture());
    final NotificationEventRequest event = new ObjectMapper().readValue(payload.getValue(),
        NotificationEventRequest.class);
    assertEquals(HOTEL_ID, event.hotelId());
    assertEquals("OPERATIONAL_EMAIL", event.eventType());
    assertEquals("ops@example.com", new ObjectMapper().readTree(event.payload()).path("recipient").asText());
  }

  @Test
  void doesNotPersistEmailWhenChannelIsDisabled() {
    when(preferences.get(HOTEL_ID)).thenReturn(Map.of("STOCK_LOW", Map.of("EMAIL", false)));

    service.enqueue(HOTEL_ID, "STOCK_LOW", "Existencia baja", "Producto 1", "stock-1");

    verify(repository, never()).insertIfAbsent(any(UUID.class), any(UUID.class), anyString(),
        anyString(), anyString(), anyString());
  }

  @Test
  void marksForwardedOutboxRowAndRemovesRecipientPayload() throws Exception {
    final ObjectMapper mapper = new ObjectMapper();
    final String emailPayload = mapper.writeValueAsString(
        Map.of("recipient", "ops@example.com", "subject", "Fixi", "message", "Alert"));
    final String serialized = mapper.writeValueAsString(new NotificationEventRequest(
        HOTEL_ID, "event-1", "OPERATIONAL_EMAIL", emailPayload));
    final OperationalNotificationOutbox row = new OperationalNotificationOutbox();
    row.setId(UUID.randomUUID());
    row.setHotelId(HOTEL_ID);
    row.setEventKey("event-1");
    row.setEventType("ORDER_ASSIGNED");
    row.setSourceKey("order-1");
    row.setPayload(serialized);
    row.setStatus(OperationalNotificationOutbox.Status.PENDING);
    row.setNextAttemptAt(LocalDateTime.now());
    when(repository.lockDue(anyInt(), any(Pageable.class))).thenReturn(List.of(row));
    when(notificationClient.enqueueEvent(any(NotificationEventRequest.class)))
        .thenReturn(new NotificationEventResponse(UUID.randomUUID(), "event-1", "PENDING", 0));

    assertEquals(1, service.forwardDue());

    assertEquals(OperationalNotificationOutbox.Status.SENT, row.getStatus());
    assertEquals("{}", row.getPayload());
    assertNotNull(row.getSentAt());
    verify(repository).saveAll(List.of(row));
  }
}
