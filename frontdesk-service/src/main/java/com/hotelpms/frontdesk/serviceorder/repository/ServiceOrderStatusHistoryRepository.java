package com.hotelpms.frontdesk.serviceorder.repository;
import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrderStatusHistory;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ServiceOrderStatusHistoryRepository extends JpaRepository<ServiceOrderStatusHistory,UUID>{List<ServiceOrderStatusHistory> findAllByHotelIdAndServiceOrderIdOrderByCreatedAtAsc(UUID hotelId,UUID serviceOrderId);}
