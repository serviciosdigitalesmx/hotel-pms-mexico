package com.hotelpms.frontdesk.serviceorder.service;

import com.hotelpms.frontdesk.serviceorder.domain.TimeEntry;
import com.hotelpms.frontdesk.serviceorder.dto.TimeEntryRequest;
import com.hotelpms.frontdesk.serviceorder.repository.TimeEntryRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Type domain type. */
@Service
@RequiredArgsConstructor
public class TimeEntryService {
  private final TimeEntryRepository repository;
  /**
   * Executes the operation.
   *
   * @param hotel parameter.
   *
   * @param order parameter.
   *
   * @param r parameter.
   *
   * @return operation result.
   */

  @Transactional
  public TimeEntry add(final UUID hotel, final UUID order, final TimeEntryRequest r) {
    return repository.save(
        TimeEntry.builder()
            .hotelId(hotel)
            .serviceOrderId(order)
            .userId(r.userId())
            .minutes(r.minutes())
            .note(r.note())
            .createdAt(LocalDateTime.now())
            .build());
  }
  /**
   * Executes the operation.
   *
   * @param hotel parameter.
   *
   * @param order parameter.
   *
   * @return operation result.
   */

  @Transactional(readOnly = true)
  public List<TimeEntry> list(final UUID hotel, final UUID order) {
    return repository.findAllByHotelIdAndServiceOrderIdOrderByCreatedAtAsc(hotel, order);
  }
}
