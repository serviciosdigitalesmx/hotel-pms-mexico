package com.hotelpms.guest.service;

import com.hotelpms.guest.dto.request.DeviceRequest;
import com.hotelpms.guest.dto.response.DeviceResponse;
import java.util.List;
import java.util.UUID;

/** Application service for customer devices. */
public interface DeviceService {
  /**
   * Creates a device for a customer.
   *
   * @param customerId customer identifier.
   * @param request device data.
   * @return created device.
   */
  DeviceResponse create(UUID customerId, DeviceRequest request);

  /** Updates a device's catalog and identity fields without changing ownership. */
  DeviceResponse update(UUID customerId, UUID deviceId, DeviceRequest request);

  /**
   * Lists active devices for a customer.
   *
   * @param customerId customer identifier.
   * @return active devices.
   */
  List<DeviceResponse> list(UUID customerId);

  /**
   * Finds one active device for a customer.
   *
   * @param customerId customer identifier.
   * @param id device identifier.
   * @return device response.
   */
  DeviceResponse get(UUID customerId, UUID id);

  /** Transfers an asset and appends an auditable ownership record. */
  DeviceResponse transfer(UUID deviceId, UUID fromCustomerId, UUID toCustomerId, String evidence);
}
