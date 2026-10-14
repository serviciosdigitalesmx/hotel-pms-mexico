package com.hotelpms.frontdesk.serviceorder.service;

import com.hotelpms.frontdesk.serviceorder.domain.ServiceCatalogEntry;
import com.hotelpms.frontdesk.serviceorder.dto.ServiceCatalogRequest;
import com.hotelpms.frontdesk.serviceorder.repository.ServiceCatalogRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServiceCatalogService {
  private final ServiceCatalogRepository repository;

  @Transactional(readOnly = true)
  public List<ServiceCatalogEntry> list(UUID tenantId) {
    return repository.findAllByTenantIdOrderByNameAsc(tenantId);
  }

  @Transactional
  public ServiceCatalogEntry create(UUID tenantId, ServiceCatalogRequest request) {
    final String name = request.name().trim();
    if (repository.existsByTenantIdAndNameIgnoreCase(tenantId, name)) {
      throw new IllegalArgumentException("SERVICE_ALREADY_EXISTS");
    }
    return repository.save(ServiceCatalogEntry.builder()
        .tenantId(tenantId).name(name).basePrice(request.basePrice() == null ? BigDecimal.ZERO : request.basePrice())
        .estimatedMinutes(request.estimatedMinutes() == null ? 60 : request.estimatedMinutes())
        .active(true).createdAt(LocalDateTime.now()).build());
  }

  @Transactional
  public ServiceCatalogEntry setActive(UUID tenantId, UUID id, boolean active) {
    final ServiceCatalogEntry service = repository.findByIdAndTenantId(id, tenantId)
        .orElseThrow(() -> new IllegalArgumentException("SERVICE_NOT_FOUND"));
    service.setActive(active);
    return repository.save(service);
  }

  @Transactional
  public ServiceCatalogEntry update(UUID tenantId, UUID id, ServiceCatalogRequest request) {
    final ServiceCatalogEntry service = repository.findByIdAndTenantId(id, tenantId)
        .orElseThrow(() -> new IllegalArgumentException("SERVICE_NOT_FOUND"));
    final String name = request.name().trim();
    if (repository.existsByTenantIdAndNameIgnoreCaseAndIdNot(tenantId, name, id)) {
      throw new IllegalArgumentException("SERVICE_ALREADY_EXISTS");
    }
    service.setName(name);
    if (request.basePrice() != null) service.setBasePrice(request.basePrice());
    if (request.estimatedMinutes() != null) service.setEstimatedMinutes(request.estimatedMinutes());
    return repository.save(service);
  }
}
