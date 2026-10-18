package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Validated name, price, and estimated duration for a tenant service catalog entry. */
public record ServiceCatalogRequest(
        @NotBlank @Size(max = SERVICE_NAME_LENGTH) String name,
        @DecimalMin("0.00") BigDecimal basePrice,
        @Positive Integer estimatedMinutes) {
    private static final int SERVICE_NAME_LENGTH = 200;

    /**
     * Creates a catalog entry request with optional price and duration.
     *
     * @param name service name
     */
    public ServiceCatalogRequest(final String name) {
        this(name, null, null);
    }
}
