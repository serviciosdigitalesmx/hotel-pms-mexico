package com.hotelpms.frontdesk.serviceorder.dto;

import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrderStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Validated updates to a tenant-owned Fixi repair order.
 *
 * @param status requested order status
 * @param diagnosis updated diagnostic summary
 * @param workLog updated technician work notes
 * @param assignedUserId assigned technician identifier
 * @param priority updated operational priority
 * @param promisedAt updated promised completion time
 * @param clearPromisedAt whether to clear the existing promise date
 */
public record ServiceOrderUpdateRequest(
        @NotNull ServiceOrderStatus status,
        String diagnosis,
        String workLog,
        UUID assignedUserId,
        @Pattern(regexp = "LOW|NORMAL|HIGH|URGENT") String priority,
        LocalDateTime promisedAt,
        Boolean clearPromisedAt) { }
