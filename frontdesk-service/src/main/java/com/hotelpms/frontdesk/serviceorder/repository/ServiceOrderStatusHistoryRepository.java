package com.hotelpms.frontdesk.serviceorder.repository;

import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrderStatusHistory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Tenant-scoped, chronological status history for repair orders. */
public interface ServiceOrderStatusHistoryRepository
        extends JpaRepository<ServiceOrderStatusHistory, UUID> {
    /**
     * Lists the status changes for one tenant-owned repair order.
     *
     * @param hotelId tenant identifier
     * @param serviceOrderId repair order identifier
     * @return history entries in creation order
     */
    List<ServiceOrderStatusHistory> findAllByHotelIdAndServiceOrderIdOrderByCreatedAtAsc(
            UUID hotelId, UUID serviceOrderId);
}
