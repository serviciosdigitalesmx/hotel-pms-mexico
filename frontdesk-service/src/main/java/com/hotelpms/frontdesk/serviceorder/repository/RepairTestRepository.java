package com.hotelpms.frontdesk.serviceorder.repository;

import com.hotelpms.frontdesk.serviceorder.domain.RepairTest;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Tenant-scoped diagnostic and quality-control records for repair orders. */
public interface RepairTestRepository extends JpaRepository<RepairTest, UUID> {
    /**
     * Lists repair checks in creation order for one tenant-owned order.
     *
     * @param hotelId tenant identifier
     * @param serviceOrderId repair order identifier
     * @return ordered diagnostic and quality-control records
     */
    List<RepairTest> findAllByHotelIdAndServiceOrderIdOrderByCreatedAtAsc(
            UUID hotelId, UUID serviceOrderId);
}
