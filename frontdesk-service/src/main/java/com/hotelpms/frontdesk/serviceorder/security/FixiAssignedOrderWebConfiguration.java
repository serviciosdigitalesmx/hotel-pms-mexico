package com.hotelpms.frontdesk.serviceorder.security;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Enforces technician assignment on every ID-addressed Fixi order endpoint and child resource. */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "fixi.authorization.assigned-orders.enabled", havingValue = "true")
public class FixiAssignedOrderWebConfiguration implements WebMvcConfigurer {
  private final FixiAssignedOrderAccess access;

  @Override
  public void addInterceptors(final InterceptorRegistry registry) {
    registry.addInterceptor((HandlerInterceptor) (request, response, handler) -> {
      final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication == null || authentication.getAuthorities().stream()
          .noneMatch(authority -> authority.getAuthority().startsWith("ROLE_"))) {
        return true;
      }
      final UUID tenantId;
      try {
        tenantId = UUID.fromString(String.valueOf(authentication.getDetails()));
      } catch (final IllegalArgumentException invalidTenant) {
        response.sendError(404);
        return false;
      }
      final UUID orderId = orderIdFor(request.getRequestURI(), tenantId);
      if (orderId == null) {
        return true;
      }
      final Object selectedBranch = request.getAttribute("fixi.authenticatedBranchId");
      final UUID selectedBranchId;
      try {
        selectedBranchId = selectedBranch == null ? null : UUID.fromString(selectedBranch.toString());
      } catch (final IllegalArgumentException invalidBranch) {
        response.sendError(404);
        return false;
      }
      if (!access.canAccessOrder(authentication.getName(), tenantId, orderId, selectedBranchId)) {
        response.sendError(404);
        return false;
      }
      return true;
    });
  }

  UUID orderIdFor(final String uri, final UUID tenantId) {
    final String[] parts = uri.split("/");
    try {
      if (uri.startsWith("/api/v1/service-orders/") && parts.length >= 5
          && !"customer".equals(parts[4]) && !"customer-counts".equals(parts[4])) {
        return UUID.fromString(parts[4]);
      }
      if (uri.startsWith("/api/v1/documents/order/") && parts.length == 6) {
        return UUID.fromString(parts[5]);
      }
      if (uri.startsWith("/api/v1/documents/") && parts.length == 6
          && "content".equals(parts[5])) {
        return access.evidenceOrderId(tenantId, UUID.fromString(parts[4]));
      }
      if (uri.startsWith("/api/v1/inventory/reservations/order/") && parts.length == 7) {
        return UUID.fromString(parts[6]);
      }
      if (uri.startsWith("/api/v1/inventory/reservations/") && parts.length == 7
          && "release".equals(parts[6])) {
        return UUID.fromString(parts[5]);
      }
      if (uri.startsWith("/api/v1/inventory/pos/orders/") && parts.length == 8
          && "consume".equals(parts[7])) {
        return UUID.fromString(parts[6]);
      }
      if (uri.startsWith("/api/v1/warranty-claims/service-order/") && parts.length == 6) {
        return UUID.fromString(parts[5]);
      }
      if (uri.startsWith("/api/v1/warranty-claims/") && parts.length == 6
          && "close".equals(parts[5])) {
        return access.warrantyOrderId(tenantId, UUID.fromString(parts[4]));
      }
    } catch (final IllegalArgumentException ignored) {
      // Non-ID routes and malformed identifiers are left to normal MVC validation/404 handling.
    }
    return null;
  }
}
