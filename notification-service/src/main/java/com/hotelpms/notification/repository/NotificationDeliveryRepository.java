package com.hotelpms.notification.repository;

import com.hotelpms.notification.domain.NotificationDelivery;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Tenant-scoped notification delivery store. */
public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, UUID> {
  java.util.Optional<NotificationDelivery> findByHotelIdAndEventKey(UUID hotelId, String eventKey);
  List<NotificationDelivery> findTop100ByHotelIdOrderByCreatedAtDesc(UUID hotelId);
}
