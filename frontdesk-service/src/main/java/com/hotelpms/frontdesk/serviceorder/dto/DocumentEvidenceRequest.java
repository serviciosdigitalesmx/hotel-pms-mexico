package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Domain type.
 *
 * @param accessLevel value.
 *
 * @param contentType value.
 *
 * @param customerId value.
 *
 * @param fileName value.
 *
 * @param serviceOrderId value.
 *
 * @param storageKey value.
 */
public record DocumentEvidenceRequest(
    @NotNull UUID serviceOrderId,
    @NotNull UUID customerId,
    @NotBlank @Size(max = 200) String fileName,
    @NotBlank @Size(max = 100) String contentType,
    @NotBlank @Size(max = 14000000) String storageKey,
    @NotBlank @Pattern(regexp = "(?i)(INTERNAL|CUSTOMER|ORDER)") @Size(max = 32) String accessLevel) { }
