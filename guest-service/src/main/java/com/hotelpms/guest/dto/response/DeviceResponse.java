package com.hotelpms.guest.dto.response;

import com.hotelpms.guest.model.Device;
import java.util.UUID;

/**
 * Response representation of a customer device.
 *
 * @param id device identifier.
 * @param customerId customer identifier.
 * @param category device category.
 * @param manufacturer device manufacturer.
 * @param model device model.
 * @param serialNumber device serial number.
 * @param imei device IMEI.
 * @param customFields custom device fields.
 */
public record DeviceResponse(
    UUID id, UUID customerId, String category, String manufacturer, String model,
    String serialNumber, String imei, String customFields) {

  /** Maps a persisted device to its API response. */
  public static DeviceResponse from(final Device device) {
    return new DeviceResponse(device.getId(), device.getGuestId(), device.getCategory(),
        device.getManufacturer(), device.getModel(), device.getSerialNumber(), device.getImei(),
        device.getCustomFields());
  }
}
