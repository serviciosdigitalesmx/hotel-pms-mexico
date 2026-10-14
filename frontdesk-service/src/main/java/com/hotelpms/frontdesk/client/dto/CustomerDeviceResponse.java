package com.hotelpms.frontdesk.client.dto;

import java.util.UUID;

/** Minimal guest-service response used to enforce device ownership on repair orders. */
public record CustomerDeviceResponse(UUID id, UUID customerId) { }
