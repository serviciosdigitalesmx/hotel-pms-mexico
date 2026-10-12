package com.hotelpms.frontdesk.serviceorder.repository;
import com.hotelpms.frontdesk.serviceorder.domain.RepairTest;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RepairTestRepository extends JpaRepository<RepairTest,UUID>{List<RepairTest> findAllByHotelIdAndServiceOrderIdOrderByCreatedAtAsc(UUID hotelId,UUID serviceOrderId);}
