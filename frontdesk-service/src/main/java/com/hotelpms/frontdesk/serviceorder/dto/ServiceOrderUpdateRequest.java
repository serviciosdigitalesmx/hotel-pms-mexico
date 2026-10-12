package com.hotelpms.frontdesk.serviceorder.dto;

import com.hotelpms.frontdesk.serviceorder.domain.ServiceOrderStatus;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Domain type.
 *
 * @param assignedUserId value.
 *
 * @param diagnosis value.
 *
 * @param status value.
 *
 * @param workLog value.
 */
public record ServiceOrderUpdateRequest(
    @NotNull ServiceOrderStatus status, String diagnosis, String workLog, UUID assignedUserId) { }
