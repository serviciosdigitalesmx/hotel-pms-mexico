package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Domain type.
 *
 * @param evidence value.
 */
public record AuthorizationRequest(@NotBlank String evidence) {}
