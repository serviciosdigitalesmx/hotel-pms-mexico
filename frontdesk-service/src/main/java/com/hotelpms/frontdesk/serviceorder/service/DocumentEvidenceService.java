package com.hotelpms.frontdesk.serviceorder.service;

import com.hotelpms.frontdesk.serviceorder.domain.DocumentEvidence;
import com.hotelpms.frontdesk.serviceorder.dto.DocumentEvidenceRequest;
import com.hotelpms.frontdesk.serviceorder.repository.DocumentEvidenceRepository;
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
