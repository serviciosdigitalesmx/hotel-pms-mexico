package com.hotelpms.frontdesk.inventory.controller;

import com.hotelpms.frontdesk.inventory.dto.CatalogImportDtos.ImportJobResponse;
import com.hotelpms.frontdesk.inventory.dto.CatalogImportDtos.ReviewRequest;
import com.hotelpms.frontdesk.inventory.service.CatalogImportService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/** Review-gated catalog ingestion endpoints. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inventory/catalog-imports")
public class CatalogImportController {
    private final CatalogImportService service;

    /**
     * Ingests a catalog file for review by an authorized tenant user.
     *
     * @param file uploaded catalog
     * @return review-gated import job
     * @implSpec Overrides must retain role authorization and tenant scoping.
     */
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER')")
    public ImportJobResponse ingest(@RequestPart("file") final MultipartFile file) {
        return service.ingest(hotel(), file);
    }

    /**
     * Reads an import job belonging to the authenticated tenant.
     *
     * @param id import-job identifier
     * @return import job and its proposals
     * @implSpec Overrides must retain role authorization and tenant scoping.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER')")
    public ImportJobResponse get(@PathVariable final UUID id) {
        return service.get(hotel(), id);
    }

    /**
     * Records the authorized review decision and applies approved proposals.
     *
     * @param id import-job identifier
     * @param request review decision
     * @return updated import job
     * @implSpec Overrides must retain role authorization and tenant scoping.
     */
    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER')")
    public ImportJobResponse review(
            @PathVariable final UUID id, @RequestBody final ReviewRequest request) {
        return service.reviewAndApply(hotel(), id, request);
    }

    private UUID hotel() {
        return UUID.fromString(
                String.valueOf(
                        SecurityContextHolder.getContext().getAuthentication().getDetails()));
    }
}
