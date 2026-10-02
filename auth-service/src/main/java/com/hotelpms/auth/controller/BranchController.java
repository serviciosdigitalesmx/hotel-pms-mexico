package com.hotelpms.auth.controller;

import com.hotelpms.auth.domain.TenantBranch;
import com.hotelpms.auth.dto.CreateBranchRequest;
import com.hotelpms.auth.dto.TenantBranchResponse;
import com.hotelpms.auth.service.BranchAccessService;
import com.hotelpms.auth.service.CapabilityService;
import com.hotelpms.internalauth.contracts.Capability;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Tenant-scoped branch administration. The tenant comes from the
 * gateway-injected {@code X-Auth-Hotel} header; the branch is always validated
 * with the tenant, and authorization is enforced through capabilities, not
 * role names alone.
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
     * @param auth    the authenticated caller
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
     * Creates a branch under the authenticated tenant and gives the requesting
     * user membership.
     *
     * @param hotelId the tenant UUID from the gateway-injected header
     * @param request branch details
     * @param auth    the authenticated caller
     * @return the created branch (HTTP 201)
     */
    @PostMapping
    public ResponseEntity<TenantBranchResponse> createBranch(
            @NonNull @RequestHeader(HEADER_HOTEL) final UUID hotelId,
            @NonNull @Valid @RequestBody final CreateBranchRequest request,
            @NonNull final Authentication auth) {
        capabilityService.requireCapability(auth.getName(), hotelId, Capability.BRANCHES_MANAGE);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(branchAccessService.createBranch(hotelId, request.name(), auth.getName()));
    }

    /**
     * Returns a single branch, validating it belongs to the authenticated tenant.
     *
     * @param hotelId  the tenant UUID from the gateway-injected header
     * @param branchId the branch UUID
     * @param auth     the authenticated caller
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
     * Server-to-server validation endpoint: verifies the tenant+branch binding
     * without trusting any client-supplied selector.
     *
     * @param hotelId  the tenant UUID from the gateway-injected header
     * @param branchId the branch UUID to validate
     * @param auth     the authenticated caller
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
                branch.getCreatedAt());
    }
}
