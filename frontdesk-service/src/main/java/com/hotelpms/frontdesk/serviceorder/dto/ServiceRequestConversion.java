package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Domain type.
 *
 * @param branchId value.
 *
 * @param customerId value.
 *
 * @param deviceId value.
 */
public record ServiceRequestConversion(@NotNull UUID customerId, UUID deviceId, UUID branchId) { }
