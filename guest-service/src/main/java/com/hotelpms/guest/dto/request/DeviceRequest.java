package com.hotelpms.guest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import static com.hotelpms.guest.model.Device.CATEGORY_MAX_LENGTH;
import static com.hotelpms.guest.model.Device.CUSTOM_FIELDS_MAX_LENGTH;
import static com.hotelpms.guest.model.Device.IDENTITY_MAX_LENGTH;

/**
 * Request to register a customer device.
 *
 * @param category device category.
 * @param manufacturer device manufacturer.
 * @param model device model.
 * @param serialNumber device serial number.
 * @param imei device IMEI.
 * @param customFields custom device fields.
 */
public record DeviceRequest(
    @NotBlank @Size(max = CATEGORY_MAX_LENGTH) String category,
    @Size(max = IDENTITY_MAX_LENGTH) String manufacturer,
    @Size(max = IDENTITY_MAX_LENGTH) String model,
    @Size(max = IDENTITY_MAX_LENGTH) String serialNumber,
    @Size(max = IDENTITY_MAX_LENGTH) String imei,
    @Size(max = CUSTOM_FIELDS_MAX_LENGTH) String customFields) { }
