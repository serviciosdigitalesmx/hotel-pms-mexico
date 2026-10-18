package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * Diagnostic or quality-control result recorded for a repair order.
 *
 * @param testName test or inspection name
 * @param passed whether the check passed
 * @param notes optional technician notes
 * @param technicianId assigned technician identifier
 */
public record RepairTestRequest(
        @NotBlank String testName, boolean passed, String notes, UUID technicianId) { }
