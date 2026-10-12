package com.hotelpms.frontdesk.reservations.repository;

import com.hotelpms.frontdesk.reservations.domain.ReservationLineItem;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for ReservationLineItem. */
@Repository
public interface ReservationLineItemRepository extends JpaRepository<ReservationLineItem, UUID> { }
