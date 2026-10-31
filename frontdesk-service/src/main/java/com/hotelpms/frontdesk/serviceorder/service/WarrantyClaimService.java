package com.hotelpms.frontdesk.serviceorder.service;

import com.hotelpms.frontdesk.audit.domain.AuditEvent;
import com.hotelpms.frontdesk.audit.service.AuditEventService;
import com.hotelpms.frontdesk.exception.NotFoundException;
import com.hotelpms.frontdesk.serviceorder.domain.WarrantyClaim;
import com.hotelpms.frontdesk.serviceorder.dto.WarrantyClaimRequest;
import com.hotelpms.frontdesk.serviceorder.repository.ServiceOrderRepository;
import com.hotelpms.frontdesk.serviceorder.repository.WarrantyClaimRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Type domain type. */
@Service
@RequiredArgsConstructor
public class WarrantyClaimService {
    private final WarrantyClaimRepository repository;
    private final ServiceOrderRepository serviceOrders;
    private final AuditEventService auditEvents;

    /**
     * Executes the operation.
     *
     * @param hotel parameter.
     * @param r parameter.
     * @return operation result.
     */
    @Transactional
    public WarrantyClaim open(final UUID hotel, final WarrantyClaimRequest r) {
        final var order =
                serviceOrders
                        .findByIdAndHotelId(r.serviceOrderId(), hotel)
                        .orElseThrow(() -> new NotFoundException("SERVICE_ORDER_NOT_FOUND"));
        if (!order.getCustomerId().equals(r.customerId())) {
            throw new IllegalArgumentException("WARRANTY_CUSTOMER_ORDER_MISMATCH");
        }
        if (order.getStatus()
                != com.hotelpms.frontdesk.serviceorder.domain.ServiceOrderStatus.COMPLETED) {
            throw new IllegalStateException("WARRANTY_REQUIRES_DELIVERED_SERVICE_ORDER");
        }
        final WarrantyClaim claim =
                repository.save(
                        WarrantyClaim.builder()
                                .hotelId(hotel)
                                .serviceOrderId(r.serviceOrderId())
                                .customerId(r.customerId())
                                .expiresAt(r.expiresAt())
                                .description(r.description().trim())
                                .createdAt(LocalDateTime.now())
                                .build());
        auditEvents.record(
                hotel,
                null,
                AuditEvent.Category.OPERATIONAL,
                "WARRANTY_CLAIM_OPENED",
                claim.getId(),
                null,
                "{\"serviceOrderId\":\""
                        + claim.getServiceOrderId()
                        + "\",\"customerId\":\""
                        + claim.getCustomerId()
                        + "\",\"expiresAt\":\""
                        + claim.getExpiresAt()
                        + "\"}");
        return claim;
    }

    /**
     * Executes the operation.
     *
     * @param hotel parameter.
     * @param id parameter.
     * @return operation result.
     */
    @Transactional
    public WarrantyClaim close(final UUID hotel, final UUID id) {
        final WarrantyClaim c =
                repository
                        .findByIdAndHotelId(id, hotel)
                        .orElseThrow(() -> new NotFoundException("WARRANTY_CLAIM_NOT_FOUND"));
        c.setStatus("CLOSED");
        final WarrantyClaim closed = repository.save(c);
        auditEvents.record(
                hotel,
                null,
                AuditEvent.Category.OPERATIONAL,
                "WARRANTY_CLAIM_CLOSED",
                closed.getId(),
                null,
                "{\"serviceOrderId\":\""
                        + closed.getServiceOrderId()
                        + "\",\"customerId\":\""
                        + closed.getCustomerId()
                        + "\"}");
        return closed;
    }

    /**
     * Lists warranty claims belonging to a tenant and customer.
     *
     * @param hotel tenant identifier
     * @param customer customer identifier
     * @return matching tenant-owned warranty claims
     * @implSpec Overrides must retain tenant ownership checks.
     */
    @Transactional(readOnly = true)
    public List<WarrantyClaim> list(final UUID hotel, final UUID customer) {
        return repository.findAllByHotelIdAndCustomerIdOrderByCreatedAtDesc(hotel, customer);
    }

    @Transactional(readOnly = true)
    public List<WarrantyClaim> listForBranches(
            final UUID hotel, final UUID customer, final java.util.Collection<UUID> branchIds) {
        return branchIds == null
                ? list(hotel, customer)
                : branchIds.isEmpty() ? List.of()
                : repository.findAllByTenantCustomerAndBranches(hotel, customer, branchIds);
    }

    /**
     * Lists warranty claims after confirming the repair order belongs to the tenant.
     *
     * @param hotel tenant identifier
     * @param serviceOrderId repair order identifier
     * @return matching tenant-owned warranty claims
     * @implSpec Overrides must validate repair-order tenant ownership before reading claims.
     */
    @Transactional(readOnly = true)
    public List<WarrantyClaim> listByServiceOrder(final UUID hotel, final UUID serviceOrderId) {
        serviceOrders
                .findByIdAndHotelId(serviceOrderId, hotel)
                .orElseThrow(() -> new NotFoundException("SERVICE_ORDER_NOT_FOUND"));
        return repository.findAllByHotelIdAndServiceOrderIdOrderByCreatedAtDesc(
                hotel, serviceOrderId);
    }
}
