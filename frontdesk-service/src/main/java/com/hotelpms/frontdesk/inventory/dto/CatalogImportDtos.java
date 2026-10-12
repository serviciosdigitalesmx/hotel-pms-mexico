package com.hotelpms.frontdesk.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

/** DTOs for the review-before-apply catalog ingestion flow. */
public final class CatalogImportDtos {
  private CatalogImportDtos() {}

  public record ImportJobResponse(
      UUID id, String fileName, String contentType, String status, List<Proposal> proposals) {}

  public record Proposal(
      String sku, String name, long quantity, String unitCost, UUID matchedProductId) {}

  public record ReviewRequest(boolean approve, List<Proposal> proposals) {}

  public record UploadMetadata(@NotBlank String fileName, @NotBlank String contentType) {}
}
