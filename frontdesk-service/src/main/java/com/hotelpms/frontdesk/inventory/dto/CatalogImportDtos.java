package com.hotelpms.frontdesk.inventory.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

/**
 * DTOs for the review-before-apply catalog ingestion flow.
 */
public final class CatalogImportDtos {
    private CatalogImportDtos() { }

    /**
     * Status and proposed rows for a catalog import job.
     *
     * @param id import job identifier
     * @param fileName uploaded file name
     * @param contentType uploaded media type
     * @param status current review/application status
     * @param proposals parsed product proposals
     */
    public record ImportJobResponse(
            UUID id,
            String fileName,
            String contentType,
            String status,
            List<Proposal> proposals) { }

    /**
     * Parsed row and optional tenant-local product match.
     *
     * @param sku proposed product code
     * @param name proposed product name
     * @param quantity proposed stock quantity
     * @param unitCost proposed supplier cost
     * @param matchedProductId existing tenant product match, if any
     */
    public record Proposal(
            String sku, String name, long quantity, String unitCost, UUID matchedProductId) { }

    /**
     * Review decision and rows selected for application.
     *
     * @param approve whether to apply the reviewed import
     * @param proposals reviewed product proposals
     */
    public record ReviewRequest(boolean approve, List<Proposal> proposals) { }

    /**
     * Metadata required to begin a catalog upload.
     *
     * @param fileName original file name
     * @param contentType uploaded media type
     */
    public record UploadMetadata(@NotBlank String fileName, @NotBlank String contentType) { }
}
