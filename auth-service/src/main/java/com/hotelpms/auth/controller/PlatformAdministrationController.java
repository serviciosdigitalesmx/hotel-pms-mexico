package com.hotelpms.auth.controller;

import com.hotelpms.auth.dto.AssignPlatformPlanRequest;
import com.hotelpms.auth.dto.PlatformAuditSummary;
import com.hotelpms.auth.dto.PlatformDashboardSummary;
import com.hotelpms.auth.dto.PlatformPage;
import com.hotelpms.auth.dto.PlatformPlanSummary;
import com.hotelpms.auth.dto.PlatformSubscriptionSummary;
import com.hotelpms.auth.dto.PlatformTenantSummary;
import com.hotelpms.auth.dto.PlatformUserSummary;
import com.hotelpms.auth.dto.UpdatePlatformTenantStatusRequest;
import com.hotelpms.auth.service.PlatformAdministrationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Read and mutate platform-level Fixi administration data. */
@RestController
@RequestMapping("/api/v1/auth/platform")
@RequiredArgsConstructor
@PreAuthorize("@platformAdminGrantGuard.isActive(authentication.name)")
@ConditionalOnProperty(name = "fixi.platform.console.enabled", havingValue = "true")
public class PlatformAdministrationController {
  private final PlatformAdministrationService administration;

  @GetMapping("/dashboard")
  public PlatformDashboardSummary dashboard() {
    return administration.dashboard();
  }

  @GetMapping("/tenants")
  public PlatformPage<PlatformTenantSummary> tenants(
      @RequestParam(required = false) final String search,
      @RequestParam(required = false) final String status,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "25") final int size) {
    return administration.tenants(search, status, page, size);
  }

  @GetMapping("/tenants/{tenantId}")
  public PlatformTenantSummary tenant(@PathVariable final UUID tenantId) {
    return administration.tenant(tenantId);
  }

  @PatchMapping("/tenants/{tenantId}/status")
  public PlatformTenantSummary status(
      @PathVariable final UUID tenantId,
      @Valid @RequestBody final UpdatePlatformTenantStatusRequest request,
      final Authentication actor) {
    return administration.updateStatus(tenantId, request, actor.getName());
  }

  @PutMapping("/tenants/{tenantId}/plan")
  public PlatformTenantSummary plan(
      @PathVariable final UUID tenantId,
      @Valid @RequestBody final AssignPlatformPlanRequest request,
      final Authentication actor) {
    return administration.assignPlan(tenantId, request, actor.getName());
  }

  @GetMapping("/tenants/{tenantId}/subscriptions")
  public PlatformPage<PlatformSubscriptionSummary> tenantSubscriptions(
      @PathVariable final UUID tenantId,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "25") final int size) {
    return administration.subscriptions(tenantId, page, size);
  }

  @GetMapping("/users")
  public PlatformPage<PlatformUserSummary> users(
      @RequestParam(required = false) final String search,
      @RequestParam(required = false) final String role,
      @RequestParam(required = false) final Boolean active,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "25") final int size) {
    return administration.users(search, role, active, page, size);
  }

  @GetMapping("/plans")
  public List<PlatformPlanSummary> plans() {
    return administration.plans();
  }

  @GetMapping("/subscriptions")
  public PlatformPage<PlatformSubscriptionSummary> subscriptions(
      @RequestParam(required = false) final UUID tenantId,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "25") final int size) {
    return administration.subscriptions(tenantId, page, size);
  }

  @GetMapping("/audit")
  public PlatformPage<PlatformAuditSummary> audit(
      @RequestParam(required = false) final UUID tenantId,
      @RequestParam(required = false) final String action,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "25") final int size) {
    return administration.audit(tenantId, action, page, size);
  }
}
