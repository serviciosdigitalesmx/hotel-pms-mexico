package com.hotelpms.frontdesk.serviceorder.service;

import com.hotelpms.frontdesk.serviceorder.domain.ServiceCatalogEntry;
import com.hotelpms.frontdesk.serviceorder.dto.ServiceCatalogRequest;
import com.hotelpms.frontdesk.serviceorder.repository.ServiceCatalogRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Manages tenant-scoped repair services used on Fixi service orders.
 */
@Service
@RequiredArgsConstructor
public class ServiceCatalogService {
    private static final int DEFAULT_ESTIMATED_MINUTES = 60;
    private final ServiceCatalogRepository repository;

    /**
     * Lists the catalog entries owned by one tenant.
     *
     * @param tenantId tenant identifier
     * @return active and inactive services ordered by name
     * @implSpec Overrides must preserve tenant scoping.
     */
    @Transactional(readOnly = true)
    public List<ServiceCatalogEntry> list(final UUID tenantId) {
        return repository.findAllByTenantIdOrderByNameAsc(tenantId);
    }

    /**
     * Creates a service after checking its tenant-local name uniqueness.
     *
     * @param tenantId tenant identifier
     * @param request validated service details
     * @return persisted catalog entry
     * @implSpec Overrides must preserve tenant-scoped uniqueness and default values.
     */
    @Transactional
    public ServiceCatalogEntry create(
            final UUID tenantId, final ServiceCatalogRequest request) {
        final String name = request.name().trim();
        if (repository.existsByTenantIdAndNameIgnoreCase(tenantId, name)) {
            throw new IllegalArgumentException("SERVICE_ALREADY_EXISTS");
        }
        return repository.save(
                ServiceCatalogEntry.builder()
                        .tenantId(tenantId)
                        .name(name)
                        .basePrice(
                                request.basePrice() == null ? BigDecimal.ZERO : request.basePrice())
                        .estimatedMinutes(
                                Objects.requireNonNullElse(
                                        request.estimatedMinutes(), DEFAULT_ESTIMATED_MINUTES))
                        .active(true)
                        .createdAt(LocalDateTime.now())
                        .build());
    }

    /**
     * Changes whether a tenant's service can be selected for new orders.
     *
     * @param tenantId tenant identifier
     * @param id service identifier
     * @param active requested availability
     * @return persisted catalog entry
     * @implSpec Overrides must preserve tenant ownership.
     */
    @Transactional
    public ServiceCatalogEntry updateActive(
            final UUID tenantId, final UUID id, final boolean active) {
        final ServiceCatalogEntry service =
                repository
                        .findByIdAndTenantId(id, tenantId)
                        .orElseThrow(() -> new IllegalArgumentException("SERVICE_NOT_FOUND"));
        service.setActive(active);
        return repository.save(service);
    }

    /**
     * Updates the name, price, and duration of a tenant-owned service.
     *
     * @param tenantId tenant identifier
     * @param id service identifier
     * @param request validated service details
     * @return persisted catalog entry
     * @implSpec Overrides must preserve tenant ownership and name uniqueness.
     */
    @Transactional
    public ServiceCatalogEntry update(
            final UUID tenantId, final UUID id, final ServiceCatalogRequest request) {
        final ServiceCatalogEntry service =
                repository
                        .findByIdAndTenantId(id, tenantId)
                        .orElseThrow(() -> new IllegalArgumentException("SERVICE_NOT_FOUND"));
        final String name = request.name().trim();
        if (repository.existsByTenantIdAndNameIgnoreCaseAndIdNot(tenantId, name, id)) {
            throw new IllegalArgumentException("SERVICE_ALREADY_EXISTS");
        }
        service.setName(name);
        if (request.basePrice() != null) {
            service.setBasePrice(request.basePrice());
        }
        if (request.estimatedMinutes() != null) {
            service.setEstimatedMinutes(request.estimatedMinutes());
        }
        return repository.save(service);
    }
}
