package com.hotelpms.auth.controller;

import com.hotelpms.auth.dto.CreateHotelRequest;
import com.hotelpms.auth.dto.HotelSummary;
import com.hotelpms.auth.service.HotelOnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Platform-only hotel administration. The root tenant is not a guest hotel. */
@RestController
@RequestMapping("/api/v1/auth/platform/hotels")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PlatformHotelController {
    private static final UUID PLATFORM_HOTEL = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final HotelOnboardingService onboarding;

    @GetMapping
    public ResponseEntity<List<HotelSummary>> list(@RequestHeader("X-Auth-Hotel") final UUID hotelId) {
        verifyPlatform(hotelId);
        return ResponseEntity.ok(onboarding.list());
    }

    @PostMapping
    public ResponseEntity<HotelSummary> create(@RequestHeader("X-Auth-Hotel") final UUID hotelId,
                                               @Valid @RequestBody final CreateHotelRequest request) {
        verifyPlatform(hotelId);
        return ResponseEntity.status(HttpStatus.CREATED).body(onboarding.create(request));
    }

    private static void verifyPlatform(final UUID hotelId) {
        if (!PLATFORM_HOTEL.equals(hotelId)) {
            throw new org.springframework.security.access.AccessDeniedException("PLATFORM_ONLY");
        }
    }
}
