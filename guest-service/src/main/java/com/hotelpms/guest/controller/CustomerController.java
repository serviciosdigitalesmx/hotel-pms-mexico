package com.hotelpms.guest.controller;

import com.hotelpms.guest.dto.request.GuestRequest;
import com.hotelpms.guest.service.GuestService;
import com.hotelpms.guest.dto.response.GuestResponse;
import com.hotelpms.guest.service.DeviceService;
import com.hotelpms.guest.dto.response.DeviceResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/** Fixi customer contract backed by the mature tenant-scoped guest aggregate. */
@RestController @RequestMapping("/api/v1/customers") @RequiredArgsConstructor
public class CustomerController {
  private final GuestService service;
  @GetMapping("/potential-duplicate") public ResponseEntity<GuestResponse> potentialDuplicate(@RequestParam(required=false) String email,@RequestParam(required=false) String phone,@RequestParam(required=false) String rfc){var match=service.findPotentialDuplicate(new GuestRequest("Consulta","Duplicado",email,phone,null,null,null,null,rfc,null,null,null,null,null,null,null,null,null,null,null,null,null));return match==null?ResponseEntity.noContent().build():ResponseEntity.ok(match);}
  private final DeviceService devices;
  @PostMapping public ResponseEntity<GuestResponse> create(@Valid @RequestBody GuestRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(service.createGuest(request));}
  @GetMapping public Page<GuestResponse> list(@PageableDefault(size=50,sort="lastName",direction=Sort.Direction.ASC) Pageable pageable){return service.getAllGuests(pageable);}
  @GetMapping("/{id}") public GuestResponse get(@PathVariable UUID id){return service.getGuestById(id);}
  @PutMapping("/{id}") public GuestResponse update(@PathVariable UUID id,@Valid @RequestBody GuestRequest request){return service.updateGuest(id,request);}
  @GetMapping("/search") public Page<GuestResponse> search(@RequestParam(required=false) String query,@PageableDefault(size=50) Pageable pageable){return service.searchGuests(query,pageable);}
  @GetMapping("/search-devices") public Page<DeviceResponse> searchDevices(@RequestParam String query,@PageableDefault(size=25) Pageable pageable){return devices.search(query,pageable);}
}
