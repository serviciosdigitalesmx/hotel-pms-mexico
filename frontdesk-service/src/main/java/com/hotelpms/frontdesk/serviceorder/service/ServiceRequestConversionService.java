package com.hotelpms.frontdesk.serviceorder.service;

import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrder;
import com.hotelpms.frontdesk.serviceorder.domain.ServiceRequest;
import com.hotelpms.frontdesk.serviceorder.dto.ServiceRequestConversion;
import com.hotelpms.frontdesk.serviceorder.repository.ServiceOrderRepository;
import com.hotelpms.frontdesk.serviceorder.repository.ServiceRequestRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/** Type domain type. */
@Service
@RequiredArgsConstructor
public class ServiceRequestConversionService {
    private final ServiceRequestRepository requests;
    private final ServiceOrderRepository orders;

    /**
     * Executes the operation.
     *
     * @param hotel parameter.
     * @param requestId parameter.
     * @param c parameter.
     * @return operation result.
     */
    @Transactional
    public ServiceOrder convert(
            final UUID hotel, final UUID requestId, final ServiceRequestConversion c) {
        final ServiceRequest r =
                requests.findByIdAndHotelId(requestId, hotel)
                        .orElseThrow(() -> new IllegalArgumentException("REQUEST_NOT_FOUND"));
        if (!"REVIEW".equals(r.getStatus())) {
            throw new IllegalStateException("REQUEST_NOT_REVIEWED");
        }
        final ServiceOrder o =
                ServiceOrder.builder()
                        .hotelId(hotel)
                        .branchId(c.branchId())
                        .customerId(c.customerId())
                        .deviceId(c.deviceId())
                        .summary(r.getDescription())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
        r.setStatus("CONVERTED");
        requests.save(r);
        return orders.save(o);
    }
}
