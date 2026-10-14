package com.hotelpms.frontdesk.serviceorder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ServiceCatalogRequest(@NotBlank @Size(max = 200) String name) { }
