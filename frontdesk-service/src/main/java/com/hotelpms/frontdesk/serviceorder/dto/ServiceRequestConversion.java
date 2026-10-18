package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Tenant-owned service request fields used to create a repair order.
 *
 * @param customerId owning customer identifier
 * @param deviceId optional customer device identifier
 * @param branchId receiving branch identifier
 */
public record ServiceRequestConversion(@NotNull UUID customerId, UUID deviceId, UUID branchId) { }
