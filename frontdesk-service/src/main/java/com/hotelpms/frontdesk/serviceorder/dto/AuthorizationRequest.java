package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Customer authorization evidence for a quoted repair order.
 *
 * @param evidence authorization record or reference
 */
public record AuthorizationRequest(@NotBlank String evidence) { }
