package com.hotelpms.frontdesk.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;
import java.util.UUID;

/** Tenant-scoped branch validation through auth-service. */
@FunctionalInterface
@FeignClient(
        contextId = "branchClient",
        name = "auth-service",
        url = "${APPLICATION_CONFIG_AUTH_SERVICE_URL:http://auth-service:8087}")
public interface BranchClient {
    /**
     * Validates that a branch exists, is active, and belongs to the authenticated tenant.
     *
     * @param branchId branch identifier
     * @return validated branch fields returned by auth-service
     */
    @PostMapping("/api/v1/auth/branches/{branchId}/validate")
    Map<String, Object> validateBranch(@PathVariable("branchId") UUID branchId);
}
