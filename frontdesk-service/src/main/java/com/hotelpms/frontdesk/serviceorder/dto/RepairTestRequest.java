package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record RepairTestRequest(
        @NotBlank String testName, boolean passed, String notes, UUID technicianId) {}
