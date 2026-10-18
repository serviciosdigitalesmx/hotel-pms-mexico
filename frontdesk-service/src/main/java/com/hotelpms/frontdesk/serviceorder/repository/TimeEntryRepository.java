package com.hotelpms.frontdesk.serviceorder.repository;

import com.hotelpms.frontdesk.serviceorder.domain.TimeEntry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** TimeEntryRepository domain type. */
public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {
    /**
     * Executes the operation.
     *
     * @param hotel parameter.
     * @param order parameter.
     * @return operation result.
     */
    List<TimeEntry> findAllByHotelIdAndServiceOrderIdOrderByCreatedAtAsc(UUID hotel, UUID order);
}
