package com.hotelpms.guest.controller;
import com.hotelpms.guest.dto.request.DeviceRequest; import com.hotelpms.guest.dto.response.DeviceResponse; import com.hotelpms.guest.service.DeviceService; import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequiredArgsConstructor @RequestMapping("/api/v1/customers/{customerId}/devices") public class DeviceController {
 private final DeviceService service;
 @PostMapping public ResponseEntity<DeviceResponse> create(@PathVariable UUID customerId,@Valid @RequestBody DeviceRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(service.create(customerId,r));}
 @GetMapping public List<DeviceResponse> list(@PathVariable UUID customerId){return service.list(customerId);}
 @GetMapping("/{id}") public DeviceResponse get(@PathVariable UUID customerId,@PathVariable UUID id){return service.get(customerId,id);}
}
