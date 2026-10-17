package com.hotelpms.notification.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hotelpms.notification.domain.NotificationDelivery;
import com.hotelpms.notification.dto.EventNotificationRequest;
import com.hotelpms.notification.repository.NotificationDeliveryRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationDeliveryServiceTest {
  @Mock
  private NotificationDeliveryRepository repository;

  @Test
  void repeatedEventForSameTenantReturnsExistingDelivery() {
    final UUID hotelId = UUID.randomUUID();
    final EventNotificationRequest request =
        new EventNotificationRequest(hotelId, "event-123", "CHECKOUT", "{}");
    final NotificationDelivery existing = new NotificationDelivery();
    when(repository.findByHotelIdAndEventKey(hotelId, request.eventKey()))
        .thenReturn(Optional.of(existing));

    final NotificationDelivery result = new NotificationDeliveryService(repository).enqueue(request);

    assertSame(existing, result);
    verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
  }
}
