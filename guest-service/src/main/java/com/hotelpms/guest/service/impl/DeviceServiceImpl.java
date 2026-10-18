package com.hotelpms.guest.service.impl;

import com.hotelpms.guest.dto.request.DeviceRequest;
import com.hotelpms.guest.dto.request.DeviceCatalogMergeRequest;
import com.hotelpms.guest.dto.response.DeviceResponse;
import com.hotelpms.guest.dto.response.DeviceCatalogMergeResponse;
import com.hotelpms.guest.dto.response.DeviceOwnershipTransferResponse;
import com.hotelpms.guest.dto.response.DeviceCatalogEntry;
import com.hotelpms.guest.exception.NotFoundException;
import com.hotelpms.guest.model.Device;
import com.hotelpms.guest.model.DeviceOwnershipTransfer;
import com.hotelpms.guest.repository.DeviceRepository;
import com.hotelpms.guest.repository.DeviceOwnershipTransferRepository;
import com.hotelpms.guest.repository.GuestRepository;
import com.hotelpms.guest.service.DeviceService;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Tenant-scoped device application service. */
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {
  private static final String DEVICE_IDENTITY_ALREADY_EXISTS = "DEVICE_IDENTITY_ALREADY_EXISTS";
  private static final String DEVICE_NOT_FOUND = "DEVICE_NOT_FOUND";
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
    final String serialNumber = normalize(request.serialNumber(), false);
    final String imei = normalize(request.imei(), true);
    if (serialNumber != null
        && devices.existsByHotelIdAndGuestIdAndSerialNumberIgnoreCase(hotelId, customerId, serialNumber)) {
      throw new IllegalArgumentException(DEVICE_IDENTITY_ALREADY_EXISTS);
    }
    if (imei != null && devices.existsByHotelIdAndImeiIgnoreCase(hotelId, imei)) {
      throw new IllegalArgumentException(DEVICE_IDENTITY_ALREADY_EXISTS);
    }
    final LocalDateTime now = LocalDateTime.now();
    final Device device = Device.builder()
        .hotelId(hotelId)
        .guestId(customerId)
        .category(request.category().trim())
        .manufacturer(normalize(request.manufacturer(), false))
        .model(normalize(request.model(), false))
        .serialNumber(serialNumber)
        .imei(imei)
        .customFields(normalize(request.customFields(), false))
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
        .orElseThrow(() -> new NotFoundException(DEVICE_NOT_FOUND));
    final String serialNumber = normalize(request.serialNumber(), false);
    final String imei = normalize(request.imei(), true);
    if (serialNumber != null && !serialNumber.equalsIgnoreCase(device.getSerialNumber())
        && devices.existsByHotelIdAndGuestIdAndSerialNumberIgnoreCaseAndIdNot(
            hotelId, customerId, serialNumber, deviceId)) {
      throw new IllegalArgumentException(DEVICE_IDENTITY_ALREADY_EXISTS);
    }
    if (imei != null && !imei.equalsIgnoreCase(device.getImei())
        && devices.existsByHotelIdAndImeiIgnoreCaseAndIdNot(hotelId, imei, deviceId)) {
      throw new IllegalArgumentException(DEVICE_IDENTITY_ALREADY_EXISTS);
    }
    device.setCategory(request.category().trim());
    device.setManufacturer(normalize(request.manufacturer(), false));
    device.setModel(normalize(request.model(), false));
    device.setSerialNumber(serialNumber);
    device.setImei(imei);
    device.setCustomFields(normalize(request.customFields(), false));
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

  /** Searches devices without crossing the authenticated tenant boundary. */
  @Override
  @Transactional(readOnly = true)
  public Page<DeviceResponse> search(final String query, final Pageable pageable) {
    final String term = query == null ? "" : query.trim();
    if (term.length() < 2) {
      return Page.empty(pageable);
    }
    final UUID hotelId = hotel();
    return devices.searchTenantDevices(hotelId, term, pageable).map(device -> {
      final String customerName = guests.findByIdAndHotelId(device.getGuestId(), hotelId)
          .map(owner -> owner.getFirstName() + " " + owner.getLastName()).orElse("");
      return DeviceResponse.from(device, customerName);
    });
  }

  /** Returns a tenant-local, read-only catalog without exposing customer identity data. */
  @Override
  @Transactional(readOnly = true)
  public List<DeviceCatalogEntry> catalog() {
    return devices.catalogByHotelId(hotel());
  }

  /** Reclassifies duplicate catalog groups without moving ownership or changing order references. */
  @Override
  @Transactional
  public DeviceCatalogMergeResponse mergeCatalog(final DeviceCatalogMergeRequest request) {
    final UUID hotelId = hotel();
    final var target = request.target();
    final String targetCategory = target.category().trim();
    final String targetManufacturer = normalize(target.manufacturer(), false);
    final String targetModel = normalize(target.model(), false);
    if (devices.countCatalogIdentity(hotelId, targetCategory, value(targetManufacturer), value(targetModel)) == 0) {
      throw new NotFoundException("DEVICE_CATALOG_TARGET_NOT_FOUND");
    }
    final Set<String> identities = new HashSet<>();
    int updated = 0;
    for (final var source : request.sources()) {
      final String category = source.category().trim();
      final String manufacturer = normalize(source.manufacturer(), false);
      final String model = normalize(source.model(), false);
      final String identity = key(category, manufacturer, model);
      if (identity.equals(key(targetCategory, targetManufacturer, targetModel)) || !identities.add(identity)) {
        throw new IllegalArgumentException("DEVICE_CATALOG_MERGE_IDENTITIES_MUST_BE_DISTINCT");
      }
      final int changed = devices.normalizeCatalogIdentity(hotelId, category, value(manufacturer), value(model),
          targetCategory, targetManufacturer, targetModel, LocalDateTime.now());
      if (changed == 0) {
        throw new NotFoundException("DEVICE_CATALOG_SOURCE_NOT_FOUND");
      }
      updated = Math.addExact(updated, changed);
    }
    return new DeviceCatalogMergeResponse(updated);
  }

  private static String value(final String text) {
    return text == null ? "" : text;
  }

  private static String key(final String category, final String manufacturer, final String model) {
    return (category + "\u0000" + value(manufacturer) + "\u0000" + value(model)).toLowerCase(Locale.ROOT);
  }

  /** Gets one tenant-scoped device for a customer. */
  @Override
  @Transactional(readOnly = true)
  public DeviceResponse get(final UUID customerId, final UUID id) {
    customer(customerId);
    return devices.findByIdAndHotelIdAndGuestId(id, hotel(), customerId)
        .map(DeviceResponse::from)
        .orElseThrow(() -> new NotFoundException(DEVICE_NOT_FOUND));
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
        .orElseThrow(() -> new NotFoundException(DEVICE_NOT_FOUND));
    if (fromCustomerId.equals(toCustomerId)) {
      throw new IllegalArgumentException("DEVICE_TRANSFER_REQUIRES_NEW_OWNER");
    }
    if (device.getSerialNumber() != null
        && devices.existsByHotelIdAndGuestIdAndSerialNumberIgnoreCase(hotelId, toCustomerId, device.getSerialNumber())) {
      throw new IllegalArgumentException(DEVICE_IDENTITY_ALREADY_EXISTS);
    }
    if (device.getImei() != null
        && devices.existsByHotelIdAndImeiIgnoreCaseAndIdNot(hotelId, device.getImei(), deviceId)) {
      throw new IllegalArgumentException(DEVICE_IDENTITY_ALREADY_EXISTS);
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

  /** Lists ownership changes only after confirming the current device and owner are in-tenant. */
  @Override
  @Transactional(readOnly = true)
  public List<DeviceOwnershipTransferResponse> ownershipHistory(final UUID customerId,
      final UUID deviceId) {
    customer(customerId);
    final UUID hotelId = hotel();
    devices.findByIdAndHotelIdAndGuestId(deviceId, hotelId, customerId)
        .orElseThrow(() -> new NotFoundException(DEVICE_NOT_FOUND));
    return transfers.findAllByHotelIdAndDeviceIdOrderByTransferredAtDesc(hotelId, deviceId)
        .stream().map(DeviceOwnershipTransferResponse::from).toList();
  }

  private static String normalize(final String value, final boolean uppercase) {
    if (value == null) {
      return null;
    }
    final String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      return null;
    }
    return uppercase ? trimmed.toUpperCase(Locale.ROOT) : trimmed;
  }
}
