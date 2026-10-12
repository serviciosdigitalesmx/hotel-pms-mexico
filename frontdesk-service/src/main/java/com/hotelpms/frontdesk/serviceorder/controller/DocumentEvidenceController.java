package com.hotelpms.frontdesk.serviceorder.controller;

import com.hotelpms.frontdesk.serviceorder.domain.DocumentEvidence;
import com.hotelpms.frontdesk.serviceorder.dto.DocumentEvidenceRequest;
import com.hotelpms.frontdesk.serviceorder.service.DocumentEvidenceService;
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
@RequestMapping("/api/v1/documents")
public class DocumentEvidenceController {
  private final DocumentEvidenceService service;

  /**
   * Adds document evidence to a service order.
   *
   * @param r evidence request
   * @return created evidence
   */
  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST','TECHNICIAN')")
  public ResponseEntity<DocumentEvidence> add(@Valid @RequestBody final DocumentEvidenceRequest r) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.add(hotel(), r));
  }

  /**
   * Lists evidence for a service order.
   *
   * @param order service order identifier
   * @return matching evidence
   */
  @GetMapping("/order/{order}")
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST','TECHNICIAN')")
  public List<DocumentEvidence> list(@PathVariable final UUID order) {
    return service.list(hotel(), order);
  }

  private UUID hotel() {
    return UUID.fromString(
        String.valueOf(SecurityContextHolder.getContext().getAuthentication().getDetails()));
  }
}
