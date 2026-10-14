package com.hotelpms.guest.controller;

import com.hotelpms.guest.dto.request.DeviceRequest;
import com.hotelpms.guest.dto.response.DeviceResponse;
import com.hotelpms.guest.service.DeviceService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** HTTP endpoints for customer devices. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers/{customerId}/devices")
public final class DeviceController {
  private final DeviceService service;

  /**
   * Creates a device for a customer.
   *
   * @param customerId customer identifier.
   * @param request device data.
   * @return created device response.
   */
  @PostMapping
  public ResponseEntity<DeviceResponse> create(
      @PathVariable final UUID customerId, @Valid @RequestBody final DeviceRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(customerId, request));
  }

  /**
   * Lists the active devices for a customer.
   *
   * @param customerId customer identifier.
   * @return active devices.
   */
  @GetMapping
  public List<DeviceResponse> list(@PathVariable final UUID customerId) {
    return service.list(customerId);
  }

  /**
   * Returns one active customer device.
   *
   * @param customerId customer identifier.
   * @param id device identifier.
   * @return device response.
   */
  @GetMapping("/{id}")
  public DeviceResponse get(
      @PathVariable final UUID customerId, @PathVariable final UUID id) {
    return service.get(customerId, id);
  }

  /** Updates device details without transferring its customer ownership. */
  @PutMapping("/{id}")
  public DeviceResponse update(@PathVariable final UUID customerId, @PathVariable final UUID id,
      @Valid @RequestBody final DeviceRequest request) {
    return service.update(customerId, id, request);
  }

  /** Transfers ownership while retaining an audit trail. */
  @PutMapping("/{id}/transfer")
  public DeviceResponse transfer(@PathVariable final UUID customerId, @PathVariable final UUID id,
      @RequestParam final UUID toCustomerId, @RequestParam(required = false) final String evidence) {
    return service.transfer(id, customerId, toCustomerId, evidence);
  }
}
