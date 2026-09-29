package com.hotelpms.guest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeviceRequest(@NotBlank @Size(max = 80) String category,
                            @Size(max = 150) String manufacturer,
                            @Size(max = 150) String model,
                            @Size(max = 150) String serialNumber,
                            @Size(max = 150) String imei,
                            @Size(max = 2000) String customFields) { }
