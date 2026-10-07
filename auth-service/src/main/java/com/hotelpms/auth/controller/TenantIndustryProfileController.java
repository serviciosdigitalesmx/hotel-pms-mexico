package com.hotelpms.auth.controller;

import com.hotelpms.auth.domain.TenantIndustryProfile;
import com.hotelpms.auth.dto.TenantIndustryProfileRequest;
import com.hotelpms.auth.service.CapabilityService;
import com.hotelpms.auth.service.TenantIndustryProfileService;
import com.hotelpms.internalauth.contracts.Capability;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Tenant-scoped industry, fields, forms, labels, catalog and template API. */
@RestController
@RequestMapping("/api/v1/auth/industry-profile")
@RequiredArgsConstructor
public class TenantIndustryProfileController {
  private final TenantIndustryProfileService service;
  private final CapabilityService capabilityService;

  /**
   * Reads the authenticated tenant profile.
   *
   * @param tenantId tenant identifier
   * @param authentication authenticated principal
   * @return tenant profile
   */
  @GetMapping
  public ResponseEntity<TenantIndustryProfile> get(
      @RequestHeader("X-Auth-Hotel") final UUID tenantId, final Authentication authentication) {
    capabilityService.requireCapability(
        authentication.getName(), tenantId, Capability.SETTINGS_READ);
    return ResponseEntity.ok(service.get(tenantId));
  }

  /**
   * Updates the authenticated tenant profile.
   *
   * @param tenantId tenant identifier
   * @param request replacement profile
   * @param authentication authenticated principal
   * @return updated profile
   */
  @PutMapping
  public ResponseEntity<TenantIndustryProfile> update(
      @RequestHeader("X-Auth-Hotel") final UUID tenantId,
      @Valid @RequestBody final TenantIndustryProfileRequest request,
      final Authentication authentication) {
    capabilityService.requireCapability(
        authentication.getName(), tenantId, Capability.SETTINGS_MANAGE);
    return ResponseEntity.ok(service.update(tenantId, request));
  }
}
