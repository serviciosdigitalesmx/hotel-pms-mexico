package com.hotelpms.notification.repository;

import com.hotelpms.notification.domain.NotificationDelivery;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/** Tenant-scoped notification delivery store. */
public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, UUID> {
  java.util.Optional<NotificationDelivery> findByHotelIdAndEventKey(UUID hotelId, String eventKey);

  @Modifying
  @Transactional
  @Query(value = """
      INSERT INTO notification_deliveries
        (id, hotel_id, event_key, event_type, payload, status, attempts, created_at)
      VALUES
        (gen_random_uuid(), :hotelId, :eventKey, :eventType, :payload, 'PENDING', 0, CURRENT_TIMESTAMP)
      ON CONFLICT (hotel_id, event_key) DO NOTHING
      """, nativeQuery = true)
  int enqueueIfAbsent(
      @Param("hotelId") UUID hotelId,
      @Param("eventKey") String eventKey,
      @Param("eventType") String eventType,
      @Param("payload") String payload);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
      SELECT delivery FROM NotificationDelivery delivery
      WHERE delivery.status IN (
        com.hotelpms.notification.domain.NotificationDelivery.Status.PENDING,
        com.hotelpms.notification.domain.NotificationDelivery.Status.FAILED)
        AND delivery.attempts < :maximumAttempts
        AND (delivery.nextAttemptAt IS NULL OR delivery.nextAttemptAt <= CURRENT_TIMESTAMP)
      ORDER BY delivery.createdAt ASC
      """)
  List<NotificationDelivery> lockNextDeliveries(
      @Param("maximumAttempts") int maximumAttempts, Pageable pageable);

  List<NotificationDelivery> findTop100ByHotelIdOrderByCreatedAtDesc(UUID hotelId);
}
