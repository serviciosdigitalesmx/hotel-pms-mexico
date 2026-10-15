package com.hotelpms.notification.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.hotelpms.notification.domain.NotificationDelivery;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class NotificationDeliveryRepositoryTest {
  @Autowired
  private NotificationDeliveryRepository repository;

  @Test
  void sameEventKeyCanBePersistedIndependentlyForDifferentTenants() {
    final UUID firstHotel = UUID.randomUUID();
    final UUID secondHotel = UUID.randomUUID();
    final String eventKey = "event-123";

    repository.saveAndFlush(delivery(firstHotel, eventKey));
    repository.saveAndFlush(delivery(secondHotel, eventKey));

    assertEquals(2, repository.count());
    assertNotEquals(
        repository.findByHotelIdAndEventKey(firstHotel, eventKey).orElseThrow().getId(),
        repository.findByHotelIdAndEventKey(secondHotel, eventKey).orElseThrow().getId());
  }

  private static NotificationDelivery delivery(final UUID hotelId, final String eventKey) {
    final NotificationDelivery delivery = new NotificationDelivery();
    delivery.setHotelId(hotelId);
    delivery.setEventKey(eventKey);
    delivery.setEventType("CHECKOUT");
    delivery.setPayload("{}");
    return delivery;
  }
}
