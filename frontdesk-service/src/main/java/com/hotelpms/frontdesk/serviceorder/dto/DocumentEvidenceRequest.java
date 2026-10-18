package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Validated metadata and content reference for evidence attached to a repair order.
 *
 * @param accessLevel visibility scope for the evidence
 * @param contentType uploaded media type
 * @param customerId tenant-owned customer identifier
 * @param fileName original file name
 * @param serviceOrderId tenant-owned repair-order identifier
 * @param storageKey protected storage reference or bounded inline payload
 */
public record DocumentEvidenceRequest(
        @NotNull UUID serviceOrderId,
        @NotNull UUID customerId,
        @NotBlank @Size(max = MAX_FILE_NAME_LENGTH) String fileName,
        @NotBlank @Size(max = MAX_CONTENT_TYPE_LENGTH) String contentType,
        @NotBlank @Size(max = MAX_STORAGE_KEY_LENGTH) String storageKey,
        @NotBlank @Pattern(regexp = "(?i)(INTERNAL|CUSTOMER|ORDER)") @Size(max = 32)
                String accessLevel) {
    private static final int MAX_FILE_NAME_LENGTH = 200;
    private static final int MAX_CONTENT_TYPE_LENGTH = 100;
    private static final int MAX_STORAGE_KEY_LENGTH = 14_000_000;
}
