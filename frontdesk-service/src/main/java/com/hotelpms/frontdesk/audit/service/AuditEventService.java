package com.hotelpms.frontdesk.audit.service;

import com.hotelpms.frontdesk.audit.domain.AuditEvent;
import com.hotelpms.frontdesk.audit.repository.AuditEventRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Writes immutable audit facts and reads only the current tenant history. */
@Service
@RequiredArgsConstructor
public class AuditEventService {
  private final AuditEventRepository repository;

  /**
   * Persists an immutable audit fact for the supplied tenant.
   *
   * @param hotelId tenant identifier
   * @param actorId authenticated actor, if available
   * @param category audit category
   * @param eventType stable event name
   * @param aggregateId affected aggregate, if available
   * @param correlationId request correlation identifier, if available
   * @param details serialized event details
   * @return saved audit event
   * @implSpec Overrides must preserve tenant attribution and append-only audit semantics.
   */
  @Transactional
  public AuditEvent record(final UUID hotelId, final UUID actorId, final AuditEvent.Category category,
      final String eventType, final UUID aggregateId, final String correlationId, final String details) {
    final AuditEvent event = new AuditEvent();
    event.setHotelId(hotelId);
    event.setActorId(actorId);
    event.setCategory(category);
    event.setEventType(eventType);
    event.setAggregateId(aggregateId);
    event.setCorrelationId(correlationId);
    event.setDetails(details == null ? "{}" : details);
    return repository.save(event);
  }

  /**
   * Reads the newest audit events for one tenant.
   *
   * @param hotelId tenant identifier
   * @return at most the latest 100 tenant-owned events
   * @implSpec Overrides must not expose audit events across tenants.
   */
  @Transactional(readOnly = true)
  public List<AuditEvent> recent(final UUID hotelId) {
    return repository.findTop100ByHotelIdOrderByCreatedAtDesc(hotelId);
  }
}
