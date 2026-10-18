package com.hotelpms.frontdesk.client.dto;

import java.util.UUID;

/**
 * Minimal customer-device response used to enforce ownership on repair orders.
 *
 * @param id device identifier
 * @param customerId owning customer identifier
 */
public record CustomerDeviceResponse(UUID id, UUID customerId) { }
