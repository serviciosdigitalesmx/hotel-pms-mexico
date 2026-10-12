package com.hotelpms.frontdesk.serviceorder.controller;

import com.hotelpms.frontdesk.serviceorder.domain.WarrantyClaim;
import com.hotelpms.frontdesk.serviceorder.dto.WarrantyClaimRequest;
import com.hotelpms.frontdesk.serviceorder.service.WarrantyClaimService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Type domain type. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/warranty-claims")
public class WarrantyClaimController {
  private final WarrantyClaimService service;

  /**
   * Opens a warranty claim.
   *
   * @param r warranty claim request
   * @return created warranty claim
   */
  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST','TECHNICIAN')")
  public ResponseEntity<WarrantyClaim> open(@Valid @RequestBody final WarrantyClaimRequest r) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.open(hotel(), r));
  }

  /**
   * Lists warranty claims for a customer.
   *
   * @param customer customer identifier
   * @return matching warranty claims
   */
  @GetMapping("/customer/{customer}")
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST','TECHNICIAN')")
  public List<WarrantyClaim> list(@PathVariable final UUID customer) {
    return service.list(hotel(), customer);
  }

  /**
   * Closes a warranty claim.
   *
   * @param id warranty claim identifier
   * @return closed warranty claim
   */
  @PostMapping("/{id}/close")
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST','TECHNICIAN')")
  public WarrantyClaim close(@PathVariable final UUID id) {
    return service.close(hotel(), id);
  }

  private UUID hotel() {
    return UUID.fromString(
        String.valueOf(SecurityContextHolder.getContext().getAuthentication().getDetails()));
  }
}
