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
public class PlatformAdministrationController {
  private final PlatformAdministrationService administration;

  /**
   * Returns database-backed platform totals.
   *
   * @return current tenant, user, owner, and order totals
   * @implSpec Preserve the grant guard on this controller when changing its routes.
   */
  @GetMapping("/dashboard")
  public PlatformDashboardSummary dashboard() {
    return administration.dashboard();
  }

  /**
   * Searches tenant records with lifecycle filters and bounded pagination.
   *
   * @param search optional case-insensitive tenant name or slug fragment
   * @param status optional lifecycle status
   * @param page zero-based page index
   * @param size requested page size, capped by the service
   * @return matching tenant page
   * @implSpec Preserve the grant guard and bounded page size.
   */
  @GetMapping("/tenants")
  public PlatformPage<PlatformTenantSummary> tenants(
      @RequestParam(required = false) final String search,
      @RequestParam(required = false) final String status,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "25") final int size) {
    return administration.tenants(search, status, page, size);
  }

  /**
   * Returns one tenant's administrative summary.
   *
   * @param tenantId tenant identifier
   * @return tenant summary
   * @implSpec Keep tenant lookup behind the platform grant guard.
   */
  @GetMapping("/tenants/{tenantId}")
  public PlatformTenantSummary tenant(@PathVariable final UUID tenantId) {
    return administration.tenant(tenantId);
  }

  /**
   * Changes a tenant's lifecycle state without deleting tenant data.
   *
   * @param tenantId tenant identifier
   * @param request requested status
   * @param actor authenticated platform administrator
   * @return updated tenant summary
   * @implSpec Preserve audit recording and the platform grant guard.
   */
  @PatchMapping("/tenants/{tenantId}/status")
  public PlatformTenantSummary status(
      @PathVariable final UUID tenantId,
      @Valid @RequestBody final UpdatePlatformTenantStatusRequest request,
      final Authentication actor) {
    return administration.updateStatus(tenantId, request, actor.getName());
  }

  /**
   * Assigns an administrative plan key and records its history.
   *
   * @param tenantId tenant identifier
   * @param request requested plan key
   * @param actor authenticated platform administrator
   * @return updated tenant summary
   * @implSpec Preserve history/audit writes and do not treat this as payment processing.
   */
  @PutMapping("/tenants/{tenantId}/plan")
  public PlatformTenantSummary plan(
      @PathVariable final UUID tenantId,
      @Valid @RequestBody final AssignPlatformPlanRequest request,
      final Authentication actor) {
    return administration.assignPlan(tenantId, request, actor.getName());
  }

  /**
   * Lists plan assignments for one tenant.
   *
   * @param tenantId tenant identifier
   * @param page zero-based page index
   * @param size requested page size
   * @return bounded subscription-history page
   * @implSpec Keep the platform grant guard and page bound.
   */
  @GetMapping("/tenants/{tenantId}/subscriptions")
  public PlatformPage<PlatformSubscriptionSummary> tenantSubscriptions(
      @PathVariable final UUID tenantId,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "25") final int size) {
    return administration.subscriptions(tenantId, page, size);
  }

  /**
   * Searches the safe global user directory.
   *
   * @param search optional username, name, or email fragment
   * @param role optional tenant role filter
   * @param active optional account active-state filter
   * @param page zero-based page index
   * @param size requested page size
   * @return user projection page without credentials or tokens
   * @implSpec Do not add sensitive authentication fields to this response.
   */
  @GetMapping("/users")
  public PlatformPage<PlatformUserSummary> users(
      @RequestParam(required = false) final String search,
      @RequestParam(required = false) final String role,
      @RequestParam(required = false) final Boolean active,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "25") final int size) {
    return administration.users(search, role, active, page, size);
  }

  /**
   * Lists plan keys already present in tenant assignments.
   *
   * @return assigned plan keys and counts
   * @implSpec Do not invent catalog entries or imply payment-provider state.
   */
  @GetMapping("/plans")
  public List<PlatformPlanSummary> plans() {
    return administration.plans();
  }

  /**
   * Lists platform plan assignment history, optionally narrowed to one tenant.
   *
   * @param tenantId optional tenant identifier
   * @param page zero-based page index
   * @param size requested page size
   * @return bounded history page
   * @implSpec Preserve the platform grant guard and bounded pagination.
   */
  @GetMapping("/subscriptions")
  public PlatformPage<PlatformSubscriptionSummary> subscriptions(
      @RequestParam(required = false) final UUID tenantId,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "25") final int size) {
    return administration.subscriptions(tenantId, page, size);
  }

  /**
   * Lists platform administrative audit events.
   *
   * @param tenantId optional affected tenant identifier
   * @param action optional action filter
   * @param page zero-based page index
   * @param size requested page size
   * @return audit projection page
   * @implSpec Preserve the platform grant guard and do not expose authentication secrets.
   */
  @GetMapping("/audit")
  public PlatformPage<PlatformAuditSummary> audit(
      @RequestParam(required = false) final UUID tenantId,
      @RequestParam(required = false) final String action,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "25") final int size) {
    return administration.audit(tenantId, action, page, size);
  }
}
