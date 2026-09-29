package com.hotelpms.guest.service;
import com.hotelpms.guest.dto.request.DeviceRequest;
import com.hotelpms.guest.dto.response.DeviceResponse;
import java.util.List; import java.util.UUID;
public interface DeviceService { DeviceResponse create(UUID customerId, DeviceRequest request); List<DeviceResponse> list(UUID customerId); DeviceResponse get(UUID customerId, UUID id); }
