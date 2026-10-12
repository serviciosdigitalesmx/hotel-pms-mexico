package com.hotelpms.frontdesk.serviceorder.service;

import com.hotelpms.frontdesk.exception.NotFoundException;
import com.hotelpms.frontdesk.serviceorder.domain.WarrantyClaim;
import com.hotelpms.frontdesk.serviceorder.dto.WarrantyClaimRequest;
import com.hotelpms.frontdesk.serviceorder.repository.WarrantyClaimRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Type domain type. */
@Service
@RequiredArgsConstructor
public class WarrantyClaimService {
  private final WarrantyClaimRepository repository;
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
  public WarrantyClaim open(final UUID hotel, final WarrantyClaimRequest r) {
    return repository.save(
        WarrantyClaim.builder()
            .hotelId(hotel)
            .serviceOrderId(r.serviceOrderId())
            .customerId(r.customerId())
            .expiresAt(r.expiresAt())
            .description(r.description().trim())
            .createdAt(LocalDateTime.now())
            .build());
  }
  /**
   * Executes the operation.
   *
   * @param hotel parameter.
   *
   * @param id parameter.
   *
   * @return operation result.
   */

  @Transactional
  public WarrantyClaim close(final UUID hotel, final UUID id) {
    final WarrantyClaim c =
        repository
            .findByIdAndHotelId(id, hotel)
            .orElseThrow(() -> new NotFoundException("WARRANTY_CLAIM_NOT_FOUND"));
    c.setStatus("CLOSED");
    return repository.save(c);
  }
  /**
   * Executes the operation.
   *
   * @param hotel parameter.
   *
   * @param customer parameter.
   *
   * @return operation result.
   */

  @Transactional(readOnly = true)
  public List<WarrantyClaim> list(final UUID hotel, final UUID customer) {
    return repository.findAllByHotelIdAndCustomerIdOrderByCreatedAtDesc(hotel, customer);
  }
}
