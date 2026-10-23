package com.hotelpms.auth.controller;

import com.hotelpms.auth.dto.CreateHotelRequest;
import com.hotelpms.auth.dto.HotelSummary;
import com.hotelpms.auth.service.HotelOnboardingService;
import com.hotelpms.auth.service.PlatformAdministrationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Platform-only hotel administration. The root tenant is not a guest hotel. */
@RestController
@RequestMapping("/api/v1/auth/platform/hotels")
@RequiredArgsConstructor
@PreAuthorize("@platformAdminGrantGuard.isActive(authentication.name)")
public class PlatformHotelController {
  private final HotelOnboardingService onboarding;
  private final PlatformAdministrationService platform;

  /**
   * Lists platform-registered hotels for the root administrator.
   *
   * @return hotel summaries
   */
  @GetMapping
  public ResponseEntity<List<HotelSummary>> list() {
    return ResponseEntity.ok(onboarding.list());
  }

  /**
   * Creates a hotel and its first owner under a new tenant ID.
   *
   * @param request validated onboarding details
   * @param authentication authenticated platform principal
   * @return created hotel summary
   */
  @PostMapping
  public ResponseEntity<HotelSummary> create(
      @Valid @RequestBody final CreateHotelRequest request,
      final Authentication authentication) {
    final var created = platform.createTenant(request, authentication.getName());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new HotelSummary(created.id(), created.name(), created.slug()));
  }
}
