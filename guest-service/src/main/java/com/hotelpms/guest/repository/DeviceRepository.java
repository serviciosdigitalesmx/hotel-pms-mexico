package com.hotelpms.guest.repository;

import com.hotelpms.guest.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Tenant-scoped persistence operations for devices. */
public interface DeviceRepository extends JpaRepository<Device, UUID> {
    /**
     * Finds a device by tenant, customer and identifier.
     *
     * @param id device identifier.
     * @param hotelId tenant identifier.
     * @param guestId customer identifier.
     * @return matching device.
     */
    Optional<Device> findByIdAndHotelIdAndGuestId(UUID id, UUID hotelId, UUID guestId);

    /**
     * Lists devices for a tenant and customer.
     *
     * @param hotelId tenant identifier.
     * @param guestId customer identifier.
     * @return matching devices.
     */
    List<Device> findAllByHotelIdAndGuestId(UUID hotelId, UUID guestId);

    /**
     * Finds a device by serial number within a tenant and customer.
     *
     * @param hotelId tenant identifier.
     * @param guestId customer identifier.
     * @param serialNumber serial number.
     * @return matching device.
     */
    Optional<Device> findByHotelIdAndGuestIdAndSerialNumber(UUID hotelId, UUID guestId, String serialNumber);

    /**
     * Finds a device by IMEI within a tenant and customer.
     *
     * @param hotelId tenant identifier.
     * @param guestId customer identifier.
     * @param imei IMEI value.
     * @return matching device.
     */
    Optional<Device> findByHotelIdAndGuestIdAndImei(UUID hotelId, UUID guestId, String imei);
}
