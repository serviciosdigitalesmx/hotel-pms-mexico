package com.hotelpms.guest.service.impl;

import com.hotelpms.guest.dto.request.DeviceRequest;
import com.hotelpms.guest.dto.response.DeviceResponse;
import com.hotelpms.guest.exception.NotFoundException;
import com.hotelpms.guest.model.Device;
import com.hotelpms.guest.model.DeviceOwnershipTransfer;
import com.hotelpms.guest.repository.DeviceRepository;
import com.hotelpms.guest.repository.DeviceOwnershipTransferRepository;
import com.hotelpms.guest.repository.GuestRepository;
import com.hotelpms.guest.service.DeviceService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Tenant-scoped device application service. */
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {
  private static final String DEVICE_IDENTITY_ALREADY_EXISTS = "DEVICE_IDENTITY_ALREADY_EXISTS";
  private final DeviceRepository devices;
  private final GuestRepository guests;
  private final DeviceOwnershipTransferRepository transfers;

  private UUID hotel() {
    final Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
    return details instanceof UUID ? (UUID) details : UUID.fromString(String.valueOf(details));
  }

  private void customer(final UUID id) {
    if (guests.findByIdAndHotelId(id, hotel()).isEmpty()) {
      throw new NotFoundException("CUSTOMER_NOT_FOUND");
    }
  }

  /** Creates a tenant-scoped device while enforcing identity uniqueness. */
  @Override
  @Transactional
  public DeviceResponse create(final UUID customerId, final DeviceRequest request) {
    final UUID hotelId = hotel();
    customer(customerId);
    if (request.serialNumber() != null
        && devices.findByHotelIdAndGuestIdAndSerialNumber(
            hotelId, customerId, request.serialNumber()).isPresent()) {
      throw new IllegalArgumentException(DEVICE_IDENTITY_ALREADY_EXISTS);
    }
    if (request.imei() != null
        && devices.findByHotelIdAndGuestIdAndImei(hotelId, customerId, request.imei()).isPresent()) {
      throw new IllegalArgumentException(DEVICE_IDENTITY_ALREADY_EXISTS);
    }
    final LocalDateTime now = LocalDateTime.now();
    final Device device = Device.builder()
        .hotelId(hotelId)
        .guestId(customerId)
        .category(request.category())
        .manufacturer(request.manufacturer())
        .model(request.model())
        .serialNumber(request.serialNumber())
        .imei(request.imei())
        .customFields(request.customFields())
        .createdAt(now)
        .updatedAt(now)
        .build();
    return DeviceResponse.from(devices.save(device));
  }

  /** Updates a tenant-scoped device while protecting serial and IMEI uniqueness. */
  @Override
  @Transactional
  public DeviceResponse update(final UUID customerId, final UUID deviceId, final DeviceRequest request) {
    final UUID hotelId = hotel();
    customer(customerId);
    final Device device = devices.findByIdAndHotelIdAndGuestId(deviceId, hotelId, customerId)
        .orElseThrow(() -> new NotFoundException("DEVICE_NOT_FOUND"));
    if (request.serialNumber() != null && !request.serialNumber().equals(device.getSerialNumber())
        && devices.findByHotelIdAndGuestIdAndSerialNumber(hotelId, customerId, request.serialNumber()).isPresent()) {
      throw new IllegalArgumentException(DEVICE_IDENTITY_ALREADY_EXISTS);
    }
    if (request.imei() != null && !request.imei().equals(device.getImei())
        && devices.findByHotelIdAndGuestIdAndImei(hotelId, customerId, request.imei()).isPresent()) {
      throw new IllegalArgumentException(DEVICE_IDENTITY_ALREADY_EXISTS);
    }
    device.setCategory(request.category());
    device.setManufacturer(request.manufacturer());
    device.setModel(request.model());
    device.setSerialNumber(request.serialNumber());
    device.setImei(request.imei());
    device.setCustomFields(request.customFields());
    device.setUpdatedAt(LocalDateTime.now());
    return DeviceResponse.from(devices.save(device));
  }

  /** Lists tenant-scoped devices for a customer. */
  @Override
  @Transactional(readOnly = true)
  public List<DeviceResponse> list(final UUID customerId) {
    customer(customerId);
    return devices.findAllByHotelIdAndGuestId(hotel(), customerId).stream()
        .map(DeviceResponse::from)
        .toList();
  }

  /** Gets one tenant-scoped device for a customer. */
  @Override
  @Transactional(readOnly = true)
  public DeviceResponse get(final UUID customerId, final UUID id) {
    customer(customerId);
    return devices.findByIdAndHotelIdAndGuestId(id, hotel(), customerId)
        .map(DeviceResponse::from)
        .orElseThrow(() -> new NotFoundException("DEVICE_NOT_FOUND"));
  }

  /** Transfers ownership only inside the current tenant and records the actor/evidence. */
  @Override
  @Transactional
  public DeviceResponse transfer(final UUID deviceId, final UUID fromCustomerId,
      final UUID toCustomerId, final String evidence) {
    final UUID hotelId = hotel();
    customer(fromCustomerId);
    customer(toCustomerId);
    final Device device = devices.findByIdAndHotelIdAndGuestId(deviceId, hotelId, fromCustomerId)
        .orElseThrow(() -> new NotFoundException("DEVICE_NOT_FOUND"));
    if (fromCustomerId.equals(toCustomerId)) {
      throw new IllegalArgumentException("DEVICE_TRANSFER_REQUIRES_NEW_OWNER");
    }
    final Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    UUID actor = null;
    try {
      actor = UUID.fromString(String.valueOf(principal));
    } catch (final IllegalArgumentException ignored) {
      // The authenticated subject can be a username in local/internal calls.
    }
    transfers.save(DeviceOwnershipTransfer.builder().hotelId(hotelId).deviceId(deviceId)
        .fromGuestId(fromCustomerId).toGuestId(toCustomerId).actorId(actor).evidence(evidence)
        .transferredAt(LocalDateTime.now()).build());
    device.setGuestId(toCustomerId);
    device.setUpdatedAt(LocalDateTime.now());
    return DeviceResponse.from(devices.save(device));
  }
}
