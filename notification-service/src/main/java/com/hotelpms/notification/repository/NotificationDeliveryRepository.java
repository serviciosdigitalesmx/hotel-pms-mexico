package com.hotelpms.notification.repository;

import com.hotelpms.notification.domain.NotificationDelivery;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/** Tenant-scoped notification delivery store. */
public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, UUID> {
  /**
   * Finds a delivery by tenant and idempotency key.
   *
   * @param hotelId tenant identifier
   * @param eventKey tenant-local idempotency key
   * @return matching delivery, if present
   */
  Optional<NotificationDelivery> findByHotelIdAndEventKey(UUID hotelId, String eventKey);

  /**
   * Inserts an event unless that tenant already has the idempotency key.
   *
   * @param hotelId tenant identifier
   * @param eventKey tenant-local idempotency key
   * @param eventType supported notification event type
   * @param payload serialized notification payload
   * @return number of inserted rows
   */
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

  /**
   * Locks the next eligible pending or failed deliveries for retry.
   *
   * @param maximumAttempts maximum attempts before delivery is exhausted
   * @param pageable bounded batch selection
   * @return locked delivery records in creation order
   */
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

  /**
   * Lists the most recent delivery history for one tenant.
   *
   * @param hotelId tenant identifier
   * @return up to one hundred recent deliveries
   */
  List<NotificationDelivery> findTop100ByHotelIdOrderByCreatedAtDesc(UUID hotelId);
}
