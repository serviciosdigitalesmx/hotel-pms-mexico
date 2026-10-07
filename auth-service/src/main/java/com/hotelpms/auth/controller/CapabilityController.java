package com.hotelpms.auth.controller;

import com.hotelpms.auth.dto.UpdateUserCapabilityGrantRequest;
import com.hotelpms.auth.dto.UserCapabilityGrantResponse;
import com.hotelpms.auth.service.CapabilityService;
import com.hotelpms.internalauth.contracts.Capability;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Handles tenant-scoped capability evaluation requests. */
@RestController
@RequestMapping("/api/v1/auth/capabilities")
@RequiredArgsConstructor
public class CapabilityController {
  private final CapabilityService service;

  /**
   * Evaluates whether a capability is enabled for the authenticated tenant.
   *
   * @param tenantId requested tenant identifier
   * @param capabilityKey capability to evaluate
   * @param authentication authenticated principal
   * @return the capability evaluation result
   * @implSpec Tenant isolation is enforced by {@link CapabilityService}.
   */
  @GetMapping("/{capabilityKey}")
  public ResponseEntity<Map<String, Object>> evaluate(
      @RequestHeader("X-Auth-Hotel") final UUID tenantId,
      @PathVariable final String capabilityKey,
      final Authentication authentication) {
    final boolean enabled = service.isEnabled(authentication.getName(), tenantId, capabilityKey);
    return ResponseEntity.ok(
        Map.of("tenantId", tenantId, "capability", capabilityKey, "enabled", enabled));
  }

  /**
   * Sets a tenant or branch-scoped user capability grant.
   *
   * @param tenantId tenant identifier
   * @param userId target user identifier
   * @param request grant payload
   * @param authentication authenticated administrator
   * @return persisted grant
   */
  @PutMapping("/users/{userId}")
  public ResponseEntity<UserCapabilityGrantResponse> updateUserGrant(
      @RequestHeader("X-Auth-Hotel") final UUID tenantId,
      @PathVariable final UUID userId,
      @Valid @RequestBody final UpdateUserCapabilityGrantRequest request,
      final Authentication authentication) {
    service.requireCapability(authentication.getName(), tenantId, Capability.CAPABILITIES_MANAGE);
    return ResponseEntity.ok(service.updateUserGrant(tenantId, userId, request));
  }

  /** Lists the explicit tenant/branch overrides for a user. */
  @GetMapping("/users/{userId}")
  public ResponseEntity<List<UserCapabilityGrantResponse>> listUserGrants(
      @RequestHeader("X-Auth-Hotel") final UUID tenantId,
      @PathVariable final UUID userId,
      final Authentication authentication) {
    service.requireCapability(authentication.getName(), tenantId, Capability.CAPABILITIES_READ);
    return ResponseEntity.ok(service.listUserGrants(tenantId, userId));
  }
}
