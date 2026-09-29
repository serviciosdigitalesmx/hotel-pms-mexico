package com.hotelpms.guest.dto.response;
import com.hotelpms.guest.model.Device;
import java.util.UUID;
public record DeviceResponse(UUID id, UUID customerId, String category, String manufacturer,
                             String model, String serialNumber, String imei, String customFields) {
    public static DeviceResponse from(Device d) { return new DeviceResponse(d.getId(), d.getGuestId(), d.getCategory(), d.getManufacturer(), d.getModel(), d.getSerialNumber(), d.getImei(), d.getCustomFields()); }
}
