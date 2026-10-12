package com.hotelpms.guest.repository;

import com.hotelpms.guest.model.DeviceOwnershipTransfer;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Tenant-scoped ownership history repository. */
public interface DeviceOwnershipTransferRepository
    extends JpaRepository<DeviceOwnershipTransfer, UUID> {
  List<DeviceOwnershipTransfer> findAllByHotelIdAndDeviceIdOrderByTransferredAtDesc(
      UUID hotelId, UUID deviceId);
}
