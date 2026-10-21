package com.hotelpms.monolith.integration;

import com.hotelpms.auth.controller.BranchController;
import com.hotelpms.auth.controller.CapabilityController;
import com.hotelpms.auth.controller.UserManagementController;
import com.hotelpms.auth.dto.TenantBranchResponse;
import com.hotelpms.frontdesk.client.BranchClient;
import com.hotelpms.frontdesk.client.CapabilityClient;
import com.hotelpms.frontdesk.client.UserAssignmentClient;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Direct in-process replacements for frontdesk's former auth-service Feign clients. */
@Configuration(proxyBeanMethods = false)
public class AuthInProcessClients {

  @Bean
  BranchClient monolithBranchClient(final BranchController branches) {
    return branchId -> {
      final UUID tenantId = currentTenantId();
      final ResponseEntity<?> response =
          branches.validateBranch(tenantId, branchId, currentAuthentication());
      final Object body = response.getBody();
      if (!(body instanceof TenantBranchResponse branch)) {
        throw new IllegalStateException("BRANCH_VALIDATION_RETURNED_EMPTY_BODY");
      }
      return Map.of(
          "id", branch.id(),
          "hotelId", branch.hotelId(),
          "active", branch.active());
    };
  }

  @Bean
  UserAssignmentClient monolithUserAssignmentClient(final UserManagementController users) {
    return (userId, branchId) ->
        users.validateTechnician(currentTenantId(), userId, branchId, currentAuthentication());
  }

  @Bean
  CapabilityClient monolithCapabilityClient(final CapabilityController capabilities) {
    return capabilityKey -> {
      final ResponseEntity<Map<String, Object>> response =
          capabilities.evaluate(currentTenantId(), capabilityKey, currentAuthentication());
      return response.getBody() == null ? Map.of() : response.getBody();
    };
  }

  private static Authentication currentAuthentication() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new org.springframework.security.access.AccessDeniedException(
          "AUTHENTICATED_CALLER_REQUIRED");
    }
    return authentication;
  }

  private static UUID currentTenantId() {
    final Authentication authentication = currentAuthentication();
    if (!(authentication.getDetails() instanceof String tenantId)) {
      throw new org.springframework.security.access.AccessDeniedException("TENANT_CONTEXT_REQUIRED");
    }
    return UUID.fromString(tenantId);
  }

}
