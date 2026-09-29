package com.hotelpms.guest.repository;

import com.hotelpms.guest.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    Optional<Device> findByIdAndHotelIdAndGuestId(UUID id, UUID hotelId, UUID guestId);
    List<Device> findAllByHotelIdAndGuestId(UUID hotelId, UUID guestId);
    Optional<Device> findByHotelIdAndGuestIdAndSerialNumber(UUID hotelId, UUID guestId, String serialNumber);
    Optional<Device> findByHotelIdAndGuestIdAndImei(UUID hotelId, UUID guestId, String imei);
}
