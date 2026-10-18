package com.hotelpms.frontdesk.notifications;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/** Tenant-aware durable outbox repository with safe concurrent polling. */
public interface OperationalNotificationOutboxRepository
        extends JpaRepository<OperationalNotificationOutbox, UUID> {
    /**
     * Inserts an event only when its tenant-scoped idempotency key is new.
     *
     * @param id outbox row identifier
     * @param hotelId tenant identifier
     * @param eventKey tenant-scoped idempotency key
     * @param sourceKey source event key
     * @param eventType operational event type
     * @param payload serialized downstream event
     * @return affected row count, zero when the event already exists
     */
    @Modifying
    /**
     * Locks due outbox events for concurrent-safe forwarding.
     *
     * @param maxAttempts maximum delivery attempts
     * @param pageable bounded batch page
     * @return locked due events
     */
    @Query(
            value =
                    "insert into operational_notification_outbox"
                        + " (id,hotel_id,event_key,source_key,event_type,payload,status,attempts,next_attempt_at,created_at)"
                        + " values"
                        + " (:id,:hotelId,:eventKey,:sourceKey,:eventType,:payload,'PENDING',0,current_timestamp,current_timestamp)"
                        + " on conflict (hotel_id,event_key) do nothing",
            nativeQuery = true)
    int insertIfAbsent(
            @Param("id") UUID id,
            @Param("hotelId") UUID hotelId,
            @Param("eventKey") String eventKey,
            @Param("sourceKey") String sourceKey,
            @Param("eventType") String eventType,
            @Param("payload") String payload);

    @Query(
            value =
                    "select * from operational_notification_outbox where status in"
                        + " ('PENDING','FAILED') and attempts < :maxAttempts and next_attempt_at <="
                        + " current_timestamp order by created_at for update skip locked",
            nativeQuery = true)
    List<OperationalNotificationOutbox> lockDue(
            @Param("maxAttempts") int maxAttempts, Pageable pageable);
}
