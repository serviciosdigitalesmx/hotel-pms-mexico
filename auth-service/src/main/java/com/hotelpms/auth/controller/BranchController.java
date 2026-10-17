package com.hotelpms.auth.controller;

import com.hotelpms.auth.domain.TenantBranch;
import com.hotelpms.auth.dto.CreateBranchRequest;
import com.hotelpms.auth.dto.TenantBranchResponse;
import com.hotelpms.auth.service.BranchAccessService;
import com.hotelpms.auth.service.CapabilityService;
import com.hotelpms.internalauth.contracts.Capability;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tenant-scoped branch administration. The tenant comes from the gateway-injected {@code
 * X-Auth-Hotel} header; the branch is always validated with the tenant, and authorization is
 * enforced through capabilities, not role names alone.
 */
@RestController
@RequestMapping("/api/v1/auth/branches")
@RequiredArgsConstructor
public class BranchController {

  private static final String HEADER_HOTEL = "X-Auth-Hotel";

  private final BranchAccessService branchAccessService;
  private final CapabilityService capabilityService;

  /**
   * Lists active branches for the authenticated tenant.
   *
   * @param hotelId the tenant UUID from the gateway-injected header
   * @param auth the authenticated caller
   * @return branch responses
   */
  @GetMapping
  public ResponseEntity<List<TenantBranchResponse>> listBranches(
      @NonNull @RequestHeader(HEADER_HOTEL) final UUID hotelId,
      @NonNull final Authentication auth) {
    capabilityService.requireCapability(auth.getName(), hotelId, Capability.BRANCHES_READ);
    return ResponseEntity.ok(branchAccessService.listBranches(hotelId));
  }

  /**
   * Lists member identifiers for an active branch within the authenticated tenant.
   *
   * @param hotelId tenant identifier from the gateway
   * @param branchId branch identifier
   * @param auth authenticated caller
   * @return member user identifiers
   */
  @GetMapping("/{branchId}/members")
  public ResponseEntity<List<UUID>> listBranchMembers(
      @NonNull @RequestHeader(HEADER_HOTEL) final UUID hotelId,
      @NonNull @PathVariable final UUID branchId,
      @NonNull final Authentication auth) {
    capabilityService.requireCapability(auth.getName(), hotelId, Capability.BRANCHES_READ);
    return ResponseEntity.ok(branchAccessService.listBranchMembers(hotelId, branchId));
  }

  /**
   * Assigns a tenant user to a branch after validating both IDs server-side.
   *
   * @param hotelId tenant identifier
   * @param branchId branch identifier
   * @param userId user identifier
   * @param auth authenticated caller
   */
  @PostMapping("/{branchId}/members/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void assignUserToBranch(
      @NonNull @RequestHeader(HEADER_HOTEL) final UUID hotelId,
      @NonNull @PathVariable final UUID branchId,
      @NonNull @PathVariable final UUID userId,
      @NonNull final Authentication auth) {
    capabilityService.requireCapability(auth.getName(), hotelId, Capability.BRANCHES_MANAGE);
    branchAccessService.assignUserToBranch(hotelId, userId, branchId);
  }

  /**
   * Removes a tenant user's membership from a branch without deleting history.
   *
   * @param hotelId tenant identifier
   * @param branchId branch identifier
   * @param userId user identifier
   * @param auth authenticated caller
   */
  @DeleteMapping("/{branchId}/members/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeUserFromBranch(
      @NonNull @RequestHeader(HEADER_HOTEL) final UUID hotelId,
      @NonNull @PathVariable final UUID branchId,
      @NonNull @PathVariable final UUID userId,
      @NonNull final Authentication auth) {
    capabilityService.requireCapability(auth.getName(), hotelId, Capability.BRANCHES_MANAGE);
    branchAccessService.removeUserFromBranch(hotelId, userId, branchId);
  }

  /**
   * Creates a branch under the authenticated tenant and gives the requesting user membership.
   *
   * @param hotelId the tenant UUID from the gateway-injected header
   * @param request branch details
   * @param auth the authenticated caller
   * @return the created branch (HTTP 201)
   */
  @PostMapping
  public ResponseEntity<TenantBranchResponse> createBranch(
      @NonNull @RequestHeader(HEADER_HOTEL) final UUID hotelId,
      @NonNull @Valid @RequestBody final CreateBranchRequest request,
      @NonNull final Authentication auth) {
    capabilityService.requireCapability(auth.getName(), hotelId, Capability.BRANCHES_MANAGE);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(branchAccessService.createBranch(hotelId, request, auth.getName()));
  }

  /**
   * Returns a single branch, validating it belongs to the authenticated tenant.
   *
   * @param hotelId the tenant UUID from the gateway-injected header
   * @param branchId the branch UUID
   * @param auth the authenticated caller
   * @return branch response or 404 if the branch is in another tenant
   */
  @GetMapping("/{branchId}")
  public ResponseEntity<TenantBranchResponse> getBranch(
      @NonNull @RequestHeader(HEADER_HOTEL) final UUID hotelId,
      @NonNull @PathVariable final UUID branchId,
      @NonNull final Authentication auth) {
    capabilityService.requireCapability(auth.getName(), hotelId, Capability.BRANCHES_READ);
    return ResponseEntity.ok(toResponse(branchAccessService.getBranchForTenant(hotelId, branchId)));
  }

  /**
   * Updates a branch after checking the caller's tenant capability.
   *
   * @param hotelId tenant UUID supplied by the trusted gateway
   * @param branchId branch UUID
   * @param request validated branch changes
   * @param auth authenticated caller
   * @return the updated branch
   */
  @PatchMapping("/{branchId}")
  public ResponseEntity<TenantBranchResponse> updateBranch(
      @RequestHeader(HEADER_HOTEL) final UUID hotelId,
      @PathVariable final UUID branchId,
      @Valid @RequestBody final com.hotelpms.auth.dto.UpdateBranchRequest request,
      final Authentication auth) {
    capabilityService.requireCapability(auth.getName(), hotelId, Capability.BRANCHES_MANAGE);
    return ResponseEntity.ok(branchAccessService.updateBranch(hotelId, branchId, request));
  }

  /**
   * Enables or disables a tenant branch without changing its ownership.
   *
   * @param hotelId tenant UUID supplied by the trusted gateway
   * @param branchId branch UUID
   * @param value desired active state
   * @param auth authenticated caller
   */
  @PatchMapping("/{branchId}/active")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void active(
      @RequestHeader(HEADER_HOTEL) final UUID hotelId,
      @PathVariable final UUID branchId,
      @RequestParam final boolean value,
      final Authentication auth) {
    capabilityService.requireCapability(auth.getName(), hotelId, Capability.BRANCHES_MANAGE);
    branchAccessService.setBranchActive(hotelId, branchId, value);
  }

  /**
   * Server-to-server validation endpoint: verifies the tenant+branch binding without trusting any
   * client-supplied selector.
   *
   * @param hotelId the tenant UUID from the gateway-injected header
   * @param branchId the branch UUID to validate
   * @param auth the authenticated caller
   * @return the validated branch
   */
  @PostMapping("/{branchId}/validate")
  public ResponseEntity<TenantBranchResponse> validateBranch(
      @NonNull @RequestHeader(HEADER_HOTEL) final UUID hotelId,
      @NonNull @PathVariable final UUID branchId,
      @NonNull final Authentication auth) {
    capabilityService.requireCapability(auth.getName(), hotelId, Capability.BRANCHES_READ);
    return ResponseEntity.ok(toResponse(branchAccessService.getBranchForTenant(hotelId, branchId)));
  }

  private static TenantBranchResponse toResponse(final TenantBranch branch) {
    return new TenantBranchResponse(
        branch.getId(),
        branch.getHotelId(),
        branch.getName(),
        branch.isActive(),
        branch.getCreatedAt(),
        branch.getPhone(),
        branch.getExtraField(),
        branch.getAddress(),
        branch.getTimeZone(),
        branch.getDateFormat(),
        branch.getCountry(),
        branch.getCurrency());
  }
}
