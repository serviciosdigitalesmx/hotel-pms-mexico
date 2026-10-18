package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Domain type.
 *
 * @param description value.
 * @param requesterContact value.
 * @param requesterName value.
 */
public record ServiceRequestDto(
        @NotBlank String requesterName,
        @NotBlank String requesterContact,
        @NotBlank @Size(max = 1000) String description) {}
