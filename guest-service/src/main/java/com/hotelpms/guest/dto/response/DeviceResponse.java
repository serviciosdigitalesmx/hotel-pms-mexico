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
 * @param customerName customer display name when returned from a global search.
 */
public record DeviceResponse(
    UUID id, UUID customerId, String category, String manufacturer, String model,
    String serialNumber, String imei, String customFields, String customerName) {

  /** Maps a persisted device to its API response. */
  public static DeviceResponse from(final Device device) {
    return new DeviceResponse(device.getId(), device.getGuestId(), device.getCategory(),
        device.getManufacturer(), device.getModel(), device.getSerialNumber(), device.getImei(),
        device.getCustomFields(), null);
  }

  /** Maps a device with its owning customer's display name. */
  public static DeviceResponse from(final Device device, final String customerName) {
    return new DeviceResponse(device.getId(), device.getGuestId(), device.getCategory(),
        device.getManufacturer(), device.getModel(), device.getSerialNumber(), device.getImei(),
        device.getCustomFields(), customerName);
  }
}
