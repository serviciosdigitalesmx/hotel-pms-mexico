package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ServiceCatalogRequest(
    @NotBlank @Size(max = 200) String name,
    @DecimalMin("0.00") BigDecimal basePrice,
    @Positive Integer estimatedMinutes) {
  public ServiceCatalogRequest(final String name) { this(name, null, null); }
}
