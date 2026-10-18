package com.hotelpms.frontdesk.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

/** Tenant-scoped technician assignment validation through auth-service. */
@FeignClient(
        contextId = "userAssignmentClient",
        name = "auth-service",
        url = "${APPLICATION_CONFIG_AUTH_SERVICE_URL:http://auth-service:8087}")
public interface UserAssignmentClient {
    @PostMapping("/api/v1/auth/users/{userId}/validate-technician")
    void validateTechnician(
            @PathVariable("userId") UUID userId,
            @RequestParam(value = "branchId", required = false) UUID branchId);
}
