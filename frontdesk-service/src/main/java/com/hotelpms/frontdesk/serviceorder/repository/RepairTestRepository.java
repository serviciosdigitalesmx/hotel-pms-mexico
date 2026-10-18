package com.hotelpms.frontdesk.serviceorder.repository;

import com.hotelpms.frontdesk.serviceorder.domain.RepairTest;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface RepairTestRepository extends JpaRepository<RepairTest, UUID> {
    List<RepairTest> findAllByHotelIdAndServiceOrderIdOrderByCreatedAtAsc(
            UUID hotelId, UUID serviceOrderId);
}
