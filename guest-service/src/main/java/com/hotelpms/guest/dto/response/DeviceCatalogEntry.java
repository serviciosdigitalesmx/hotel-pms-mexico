package com.hotelpms.guest.dto.response;

/**
 * Tenant-scoped summary of a device model currently registered to customers.
 *
 * @param category device category
 * @param manufacturer manufacturer name, if known
 * @param model model name, if known
 * @param registeredDevices number of matching tenant devices
 */
public record DeviceCatalogEntry(
    String category, String manufacturer, String model, Long registeredDevices) {
}
