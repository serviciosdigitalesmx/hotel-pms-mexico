package com.hotelpms.monolith.security;

import com.hotelpms.auth.domain.TenantIndustryProfile;
import com.hotelpms.auth.repository.TenantIndustryProfileRepository;
import com.hotelpms.auth.service.CapabilityService;
import com.hotelpms.internalauth.contracts.Capability;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Applies effective Fixi capabilities to business API routes in the consolidated monolith. */
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class FixiCapabilityAuthorizationConfiguration implements WebMvcConfigurer {
  private final CapabilityService capabilities;
  private final TenantIndustryProfileRepository industryProfiles;

  @Override
  public void addInterceptors(final InterceptorRegistry registry) {
    registry.addInterceptor(this::authorizeFixiCapability).addPathPatterns("/api/v1/**");
  }

  private boolean authorizeFixiCapability(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final Object handler) throws Exception {
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      return true;
    }
    final Capability required = requiredCapability(request.getRequestURI(), request.getMethod());
    if (required == null) {
      return true;
    }
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return false;
    }
    final UUID tenantId;
    try {
      tenantId = UUID.fromString(String.valueOf(authentication.getDetails()));
    } catch (final IllegalArgumentException | NullPointerException invalidTenant) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return false;
    }
    final boolean fixiTenant = industryProfiles.findById(tenantId)
        .map(TenantIndustryProfile::getIndustryKey)
        .filter("ELECTRONICS_REPAIR"::equalsIgnoreCase)
        .isPresent();
    if (!fixiTenant) {
      return true;
    }
    final Object branchAttribute = request.getAttribute("fixi.authenticatedBranchId");
    final UUID branchId;
    try {
      branchId = branchAttribute == null ? null : UUID.fromString(branchAttribute.toString());
    } catch (final IllegalArgumentException invalidBranch) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return false;
    }
    if (!capabilities.isEnabled(authentication.getName(), tenantId, branchId, required.name())) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return false;
    }
    return true;
  }

  static Capability requiredCapability(final String uri, final String method) {
    final String path = uri.toLowerCase(Locale.ROOT);
    final String verb = method.toUpperCase(Locale.ROOT);
    if (path.startsWith("/api/v1/auth/platform/") || path.startsWith("/api/v1/auth/public/")) {
      return null;
    }
    // Hotel-PMS endpoints remain available for legacy tenants, but are not part of Fixi's API.
    if (path.startsWith("/api/v1/quotations") || path.startsWith("/api/v1/guests")
        || path.startsWith("/api/v1/stays") && !path.startsWith("/api/v1/stays/settings")
            && !path.startsWith("/api/v1/stays/assistant")
            && !path.startsWith("/api/v1/stays/whatsapp")
        || path.startsWith("/api/v1/rooms") || path.startsWith("/api/v1/room-types")
        || path.startsWith("/api/v1/reservations") || path.startsWith("/api/v1/rate-calendar")) {
      return Capability.PLATFORM_HOTELS_MANAGE;
    }
    if (path.startsWith("/api/v1/auth/capabilities/users/")) {
      return "GET".equals(verb) ? Capability.CAPABILITIES_READ : Capability.CAPABILITIES_MANAGE;
    }
    if (path.startsWith("/api/v1/auth/users") || path.startsWith("/api/v1/auth/invitations")) {
      return Capability.USERS_MANAGE;
    }
    if (path.startsWith("/api/v1/auth/branches")) {
      return "GET".equals(verb) ? Capability.BRANCHES_READ : Capability.BRANCHES_MANAGE;
    }
    if (path.startsWith("/api/v1/auth/industry-profile")) {
      return "GET".equals(verb) ? Capability.SETTINGS_READ : Capability.SETTINGS_MANAGE;
    }
    if (path.startsWith("/api/v1/customers")) {
      return "GET".equals(verb) ? Capability.CUSTOMERS_READ : Capability.CUSTOMERS_WRITE;
    }
    if (path.startsWith("/api/v1/devices") || path.startsWith("/api/v1/equipment")) {
      return "GET".equals(verb) ? Capability.DEVICES_READ : Capability.DEVICES_WRITE;
    }
    if (path.startsWith("/api/v1/service-orders") || path.startsWith("/api/v1/service-requests")
        || path.startsWith("/api/v1/services") || path.startsWith("/api/v1/documents")
        || path.startsWith("/api/v1/warranty-claims")) {
      if (path.contains("/payments") || path.contains("/payment")) {
        return Capability.PAYMENTS_RECEIVE;
      }
      if (path.contains("/authorize") || path.contains("/reject")) {
        return Capability.QUOTES_AUTHORIZE_RECORD;
      }
      if (path.contains("/quotation") || path.startsWith("/api/v1/quotations")) {
        return "GET".equals(verb) ? Capability.ORDERS_READ : Capability.QUOTES_MODIFY;
      }
      if (path.contains("/transition") || path.endsWith("/status")) {
        return Capability.ORDERS_STATUS_CHANGE;
      }
      if (path.endsWith("/deliver")) {
        return Capability.ORDERS_STATUS_CHANGE;
      }
      if (path.contains("/cancel")) {
        return Capability.ORDERS_CANCEL;
      }
      return switch (verb) {
        case "GET", "HEAD" -> Capability.ORDERS_READ;
        case "POST" -> path.equals("/api/v1/service-orders")
            || path.startsWith("/api/v1/service-requests")
            ? Capability.ORDERS_CREATE : Capability.ORDERS_UPDATE;
        default -> Capability.ORDERS_UPDATE;
      };
    }
    if (path.startsWith("/api/v1/inventory") || path.startsWith("/api/v1/procurement")) {
      if (path.contains("/purchase") || path.contains("/receive") || path.contains("/import")) {
        return Capability.INVENTORY_PURCHASE;
      }
      return switch (verb) {
        case "GET", "HEAD" -> Capability.INVENTORY_READ;
        default -> Capability.INVENTORY_ADJUST;
      };
    }
    if (path.startsWith("/api/v1/expenses") || path.startsWith("/api/v1/cash")) {
      return "GET".equals(verb) ? Capability.FINANCE_READ : Capability.CASH_MOVE;
    }
    if (path.startsWith("/api/v1/invoices")) {
      if (path.endsWith("/refund")) return Capability.PAYMENTS_REFUND;
      if (path.contains("/payments")) return Capability.PAYMENTS_RECEIVE;
      return "GET".equals(verb) || "HEAD".equals(verb)
          ? Capability.BILLING_READ : Capability.BILLING_WRITE;
    }
    if (path.startsWith("/api/v1/reports")) {
      if (path.contains("/owner") || path.contains("/finance")) {
        return Capability.FINANCE_READ;
      }
      return Capability.REPORTS_READ;
    }
    if (path.startsWith("/api/v1/workflows")) {
      return "GET".equals(verb) ? Capability.ORDERS_READ : Capability.WORKFLOWS_MANAGE;
    }
    if (path.startsWith("/api/v1/audit-events")) return Capability.REPORTS_READ;
    if (path.startsWith("/api/v1/whatsapp/inbox")) return Capability.SETTINGS_MANAGE;
    if (path.startsWith("/api/v1/notifications/preferences")) {
      return "GET".equals(verb) || "HEAD".equals(verb)
          ? Capability.SETTINGS_READ : Capability.SETTINGS_MANAGE;
    }
    if (path.startsWith("/api/v1/whatsapp")
        || path.startsWith("/api/v1/notifications/preferences")
        || path.startsWith("/api/v1/stays/whatsapp")) {
      return Capability.SETTINGS_MANAGE;
    }
    if (path.startsWith("/api/v1/stays/assistant")) {
      return Capability.ORDERS_READ;
    }
    if (path.startsWith("/api/v1/settings") || path.startsWith("/api/v1/stays/settings")) {
      return "GET".equals(verb) ? Capability.SETTINGS_READ : Capability.SETTINGS_MANAGE;
    }
    return null;
  }
}
