package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Customer-submitted repair request awaiting review.
 *
 * @param requesterName customer name
 * @param requesterContact customer contact details
 * @param description requested repair details
 */
public record ServiceRequestDto(
        @NotBlank String requesterName,
        @NotBlank String requesterContact,
        @NotBlank @Size(max = 1000) String description) { }
