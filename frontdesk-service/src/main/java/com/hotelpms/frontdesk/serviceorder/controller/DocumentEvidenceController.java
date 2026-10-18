package com.hotelpms.frontdesk.serviceorder.controller;

import com.hotelpms.frontdesk.serviceorder.domain.DocumentEvidence;
import com.hotelpms.frontdesk.serviceorder.dto.DocumentEvidenceRequest;
import com.hotelpms.frontdesk.serviceorder.service.DocumentEvidenceService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Exposes tenant-authorized evidence metadata and content for Fixi repair orders.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/documents")
public class DocumentEvidenceController {
    private final DocumentEvidenceService service;

    /**
     * Converts stored evidence into a response without exposing inline data URLs.
     *
     * @param evidence stored evidence record
     * @return safe response view
     */
    public static EvidenceView view(final DocumentEvidence evidence) {
        final String storageKey =
                evidence.getStorageKey() != null && evidence.getStorageKey().startsWith("data:")
                        ? "/api/v1/documents/" + evidence.getId() + "/content"
                        : evidence.getStorageKey();
        return new EvidenceView(
                evidence.getId(),
                evidence.getServiceOrderId(),
                evidence.getCustomerId(),
                evidence.getFileName(),
                evidence.getContentType(),
                storageKey,
                evidence.getAccessLevel(),
                evidence.getCreatedAt());
    }

    /**
     * Adds document evidence to a service order.
     *
     * @param r evidence request
     * @return created evidence
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST','TECHNICIAN')")
    public ResponseEntity<EvidenceView> add(@Valid @RequestBody final DocumentEvidenceRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(view(service.add(hotel(), r)));
    }

    /**
     * Lists evidence for a service order.
     *
     * @param order service order identifier
     * @return matching evidence
     */
    @GetMapping("/order/{order}")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST','TECHNICIAN')")
    public List<EvidenceView> list(@PathVariable final UUID order) {
        return service.list(hotel(), order).stream().map(DocumentEvidenceController::view).toList();
    }

    /**
     * Returns the stored bytes for evidence after service-level tenant and access checks.
     *
     * @implSpec Overrides must preserve service-level authorization and tenant checks.
     * @param id evidence identifier
     * @return stored evidence bytes
     */
    @GetMapping("/{id}/content")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST','TECHNICIAN')")
    public ResponseEntity<byte[]> content(@PathVariable final UUID id) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"evidence-" + id + "\"")
                .body(service.content(hotel(), id));
    }

    private UUID hotel() {
        return UUID.fromString(
                String.valueOf(
                        SecurityContextHolder.getContext().getAuthentication().getDetails()));
    }

    /**
     * Safe response view for evidence attached to a repair order.
     *
     * @param id evidence identifier
     * @param serviceOrderId repair-order identifier
     * @param customerId customer identifier
     * @param fileName original file name
     * @param contentType media type
     * @param storageKey protected storage reference
     * @param accessLevel evidence access level
     * @param createdAt creation timestamp
     */
    public record EvidenceView(
            UUID id,
            UUID serviceOrderId,
            UUID customerId,
            String fileName,
            String contentType,
            String storageKey,
            String accessLevel,
            LocalDateTime createdAt) { }
}
