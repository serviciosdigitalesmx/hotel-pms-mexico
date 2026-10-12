package com.hotelpms.frontdesk.serviceorder.service;

import com.hotelpms.frontdesk.serviceorder.domain.ServiceRequest;
import com.hotelpms.frontdesk.serviceorder.dto.ServiceRequestDto;
import com.hotelpms.frontdesk.serviceorder.repository.ServiceRequestRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Type domain type. */
@Service
@RequiredArgsConstructor
public class ServiceRequestService {
  private final ServiceRequestRepository repository;
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
  public ServiceRequest create(final UUID hotel, final ServiceRequestDto r) {
    return repository.save(
        ServiceRequest.builder()
            .hotelId(hotel)
            .requesterName(r.requesterName().trim())
            .requesterContact(r.requesterContact().trim())
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
  public ServiceRequest review(final UUID hotel, final UUID id) {
    final ServiceRequest r =
        repository
            .findByIdAndHotelId(id, hotel)
            .orElseThrow(() -> new IllegalArgumentException("REQUEST_NOT_FOUND"));
    r.setStatus("REVIEW");
    return repository.save(r);
  }
  /**
   * Executes the operation.
   *
   * @param hotel parameter.
   *
   * @return operation result.
   */

  @Transactional(readOnly = true)
  public List<ServiceRequest> list(final UUID hotel) {
    return repository.findAllByHotelIdOrderByCreatedAtDesc(hotel);
  }
}
