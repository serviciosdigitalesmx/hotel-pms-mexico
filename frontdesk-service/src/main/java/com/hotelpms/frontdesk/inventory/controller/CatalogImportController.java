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

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER')")
    public ImportJobResponse ingest(@RequestPart("file") final MultipartFile file) {
        return service.ingest(hotel(), file);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER')")
    public ImportJobResponse get(@PathVariable final UUID id) {
        return service.get(hotel(), id);
    }

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
