package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.NotBlank;
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
    UUID serviceOrderId,
    UUID customerId,
    @NotBlank String fileName,
    @NotBlank String contentType,
    @NotBlank String storageKey,
    @NotBlank String accessLevel) { }
