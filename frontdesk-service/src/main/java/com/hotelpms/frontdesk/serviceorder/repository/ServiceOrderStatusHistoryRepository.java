package com.hotelpms.frontdesk.serviceorder.repository;

import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrderStatusHistory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface ServiceOrderStatusHistoryRepository
        extends JpaRepository<ServiceOrderStatusHistory, UUID> {
    List<ServiceOrderStatusHistory> findAllByHotelIdAndServiceOrderIdOrderByCreatedAtAsc(
            UUID hotelId, UUID serviceOrderId);
}
