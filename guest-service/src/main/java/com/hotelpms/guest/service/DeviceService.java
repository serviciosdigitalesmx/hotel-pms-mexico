package com.hotelpms.guest.service;

import com.hotelpms.guest.dto.request.DeviceCatalogMergeRequest;
import com.hotelpms.guest.dto.request.DeviceRequest;
import com.hotelpms.guest.dto.response.DeviceCatalogEntry;
import com.hotelpms.guest.dto.response.DeviceCatalogMergeResponse;
import com.hotelpms.guest.dto.response.DeviceOwnershipTransferResponse;
import com.hotelpms.guest.dto.response.DeviceResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Application service for customer devices. */
public interface DeviceService {
  /**
   * Creates a device for a customer.
   *
   * @param customerId customer identifier.
   * @param request device data.
   * @return created device.
   */
  DeviceResponse create(final UUID customerId, final DeviceRequest request);

  /**
   * Updates a device's catalog and identity fields without changing ownership.
   *
   * @param customerId owner customer identifier
   * @param deviceId device identifier
   * @param request replacement device data
   * @return updated device
   */
  DeviceResponse update(final UUID customerId, final UUID deviceId, final DeviceRequest request);

  /**
   * Lists active devices for a customer.
   *
   * @param customerId customer identifier.
   * @return active devices.
   */
  List<DeviceResponse> list(final UUID customerId);

  /**
   * Searches active tenant devices by category, make, model, serial, or IMEI.
   *
   * @param query search text
   * @param pageable page and sort criteria
   * @return matching tenant devices
   */
  Page<DeviceResponse> search(final String query, final Pageable pageable);

  /**
   * Lists observed device models and their tenant-local registration counts.
   *
   * @return tenant device catalog entries
   */
  List<DeviceCatalogEntry> catalog();

  /**
   * Normalizes selected catalog identities within the active tenant without changing device IDs.
   *
   * @param request selected source and target identities
   * @return merge counts and resulting catalog entry
   */
  DeviceCatalogMergeResponse mergeCatalog(final DeviceCatalogMergeRequest request);

  /**
   * Finds one active device for a customer.
   *
   * @param customerId customer identifier.
   * @param id device identifier.
   * @return device response.
   */
  DeviceResponse get(final UUID customerId, final UUID id);

  /**
   * Transfers an asset and appends an auditable ownership record.
   *
   * @param deviceId device identifier
   * @param fromCustomerId current customer identifier
   * @param toCustomerId destination customer identifier
   * @param evidence transfer evidence
   * @return transferred device
   */
  DeviceResponse transfer(final UUID deviceId, final UUID fromCustomerId,
      final UUID toCustomerId, final String evidence);

  /**
   * Returns the tenant-scoped ownership audit for a device currently owned by the customer.
   *
   * @param customerId current customer identifier
   * @param deviceId device identifier
   * @return ownership transfer history
   */
  List<DeviceOwnershipTransferResponse> ownershipHistory(final UUID customerId, final UUID deviceId);
}
