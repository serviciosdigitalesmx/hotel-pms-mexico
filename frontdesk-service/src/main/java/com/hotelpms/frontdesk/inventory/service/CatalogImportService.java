package com.hotelpms.frontdesk.inventory.service;

import com.hotelpms.frontdesk.inventory.domain.CatalogImportJob;
import com.hotelpms.frontdesk.inventory.domain.InventoryProduct;
import com.hotelpms.frontdesk.inventory.dto.CatalogImportDtos.ImportJobResponse;
import com.hotelpms.frontdesk.inventory.dto.CatalogImportDtos.Proposal;
import com.hotelpms.frontdesk.inventory.dto.CatalogImportDtos.ReviewRequest;
import com.hotelpms.frontdesk.inventory.repository.CatalogImportJobRepository;
import com.hotelpms.frontdesk.inventory.repository.InventoryProductRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Implements ingest, extraction, matching, review and transactional apply. */
@Service
@RequiredArgsConstructor
public class CatalogImportService {
    private final CatalogImportJobRepository jobs;
    private final InventoryProductRepository products;

    /**
     * Stores an uploaded catalog for human review without applying its proposed products.
     *
     * @param hotelId tenant identifier
     * @param file uploaded catalog
     * @return import job and extracted proposals
     * @implSpec Overrides must keep uploaded data tenant-scoped and review-gated.
     */
    @Transactional
    public ImportJobResponse ingest(final UUID hotelId, final MultipartFile file) {
        try {
            final String raw = new String(file.getBytes(), StandardCharsets.UTF_8);
            final CatalogImportJob job = new CatalogImportJob();
            job.setHotelId(hotelId);
            job.setFileName(
                    file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename());
            job.setContentType(
                    file.getContentType() == null
                            ? "application/octet-stream"
                            : file.getContentType());
            job.setRawText(raw);
            job.setExtractedRows(raw);
            job.setStatus(CatalogImportJob.Status.REVIEW_REQUIRED);
            return response(jobs.save(job), hotelId);
        } catch (final java.io.IOException ex) {
            throw new IllegalArgumentException("CATALOG_INGESTION_FAILED", ex);
        }
    }

    /**
     * Reads one import job and its proposals for the owning tenant.
     *
     * @param hotelId tenant identifier
     * @param id import-job identifier
     * @return import job with review proposals
     * @implSpec Overrides must reject jobs owned by another tenant.
     */
    @Transactional(readOnly = true)
    public ImportJobResponse get(final UUID hotelId, final UUID id) {
        return response(
                jobs.findById(id)
                        .filter(x -> hotelId.equals(x.getHotelId()))
                        .orElseThrow(() -> new IllegalArgumentException("IMPORT_JOB_NOT_FOUND")),
                hotelId);
    }

    /**
     * Applies approved proposals or records their rejection for the owning tenant.
     *
     * @param hotelId tenant identifier
     * @param id import-job identifier
     * @param request review decision and selected proposals
     * @return updated import job
     * @implSpec Overrides must preserve tenant ownership and explicit human approval.
     */
    @Transactional
    public ImportJobResponse reviewAndApply(
            final UUID hotelId, final UUID id, final ReviewRequest request) {
        final CatalogImportJob job =
                jobs.findById(id)
                        .filter(x -> hotelId.equals(x.getHotelId()))
                        .orElseThrow(() -> new IllegalArgumentException("IMPORT_JOB_NOT_FOUND"));
        if (!request.approve()) {
            job.setStatus(CatalogImportJob.Status.REJECTED);
            job.setReviewedAt(LocalDateTime.now());
            return response(jobs.save(job), hotelId);
        }
        for (final Proposal proposal : request.proposals()) {
            if (proposal.matchedProductId() == null) {
                final InventoryProduct product = new InventoryProduct();
                product.setHotelId(hotelId);
                product.setSku(proposal.sku().trim());
                product.setName(proposal.name().trim());
                product.setOptional(false);
                products.save(product);
            } else {
                products.findById(proposal.matchedProductId())
                        .filter(p -> hotelId.equals(p.getHotelId()))
                        .orElseThrow(() -> new IllegalArgumentException("PRODUCT_NOT_FOUND"));
            }
        }
        job.setStatus(CatalogImportJob.Status.APPLIED);
        job.setReviewedAt(LocalDateTime.now());
        return response(jobs.save(job), hotelId);
    }

    private ImportJobResponse response(final CatalogImportJob job, final UUID hotelId) {
        final List<Proposal> proposals = new ArrayList<>();
        for (final String line : job.getExtractedRows().split("\\R")) {
            final String[] parts = line.split(",", -1);
            if (parts.length >= 2
                    && !parts[0].isBlank()
                    && !"sku".equalsIgnoreCase(parts[0].trim())) {
                final UUID match =
                        products.findAllByHotelId(hotelId).stream()
                                .filter(p -> p.getSku().equalsIgnoreCase(parts[0].trim()))
                                .map(InventoryProduct::getId)
                                .findFirst()
                                .orElse(null);
                proposals.add(new Proposal(parts[0].trim(), parts[1].trim(), 0, null, match));
            }
        }
        return new ImportJobResponse(
                job.getId(),
                job.getFileName(),
                job.getContentType(),
                job.getStatus().name(),
                proposals);
    }
}
