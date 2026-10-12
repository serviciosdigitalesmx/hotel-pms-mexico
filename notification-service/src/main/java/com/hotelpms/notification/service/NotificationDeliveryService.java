package com.hotelpms.notification.service;

import com.hotelpms.notification.domain.NotificationDelivery;
import com.hotelpms.notification.dto.EventNotificationRequest;
import com.hotelpms.notification.repository.NotificationDeliveryRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Persists event notifications before provider delivery, with duplicate suppression. */
@Service
@RequiredArgsConstructor
public class NotificationDeliveryService {
  private final NotificationDeliveryRepository repository;

  @Transactional
  public NotificationDelivery enqueue(final EventNotificationRequest request) {
    return repository.findByHotelIdAndEventKey(request.hotelId(), request.eventKey()).orElseGet(() -> {
      final NotificationDelivery delivery = new NotificationDelivery();
      delivery.setHotelId(request.hotelId());
      delivery.setEventKey(request.eventKey());
      delivery.setEventType(request.eventType());
      delivery.setPayload(request.payload());
      return repository.save(delivery);
    });
  }
}
