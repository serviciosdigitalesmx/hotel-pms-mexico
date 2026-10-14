package com.hotelpms.frontdesk.serviceorder.service;

import com.hotelpms.frontdesk.serviceorder.domain.DocumentEvidence;
import com.hotelpms.frontdesk.serviceorder.dto.DocumentEvidenceRequest;
import com.hotelpms.frontdesk.exception.NotFoundException;
import com.hotelpms.frontdesk.serviceorder.repository.DocumentEvidenceRepository;
import com.hotelpms.frontdesk.serviceorder.repository.ServiceOrderRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Type domain type. */
@Service
@RequiredArgsConstructor
public class DocumentEvidenceService {
  private final DocumentEvidenceRepository repository;
  private final ServiceOrderRepository serviceOrders;
  /**
   * Executes the operation.
   *
   * @param hotel parameter.
   *
   * @param r parameter.
   *
   * @return operation result.
   */

  @Transactional
  public DocumentEvidence add(final UUID hotel, final DocumentEvidenceRequest r) {
    final var order = serviceOrders.findByIdAndHotelId(r.serviceOrderId(), hotel)
        .orElseThrow(() -> new NotFoundException("SERVICE_ORDER_NOT_FOUND"));
    if (!r.customerId().equals(order.getCustomerId())) {
      throw new IllegalArgumentException("DOCUMENT_CUSTOMER_MISMATCH");
    }
    return repository.save(
        DocumentEvidence.builder()
            .hotelId(hotel)
            .serviceOrderId(r.serviceOrderId())
            .customerId(r.customerId())
            .fileName(r.fileName().trim())
            .contentType(r.contentType().trim())
            .storageKey(r.storageKey().trim())
            .accessLevel(r.accessLevel().trim().toUpperCase(Locale.ROOT))
            .createdAt(LocalDateTime.now())
            .build());
  }
  /**
   * Executes the operation.
   *
   * @param hotel parameter.
   *
   * @param order parameter.
   *
   * @return operation result.
   */

  @Transactional(readOnly = true)
  public List<DocumentEvidence> list(final UUID hotel, final UUID order) {
    return repository.findAllByHotelIdAndServiceOrderId(hotel, order);
  }
}
