package com.hotelpms.guest.dto.response;

/** Tenant-scoped summary of a device model currently registered to customers. */
public record DeviceCatalogEntry(
    String category, String manufacturer, String model, Long registeredDevices) { }
