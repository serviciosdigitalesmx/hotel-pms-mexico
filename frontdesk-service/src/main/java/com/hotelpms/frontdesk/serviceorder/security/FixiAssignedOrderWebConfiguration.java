package com.hotelpms.frontdesk.serviceorder.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "fixi.authorization.assigned-orders.enabled", havingValue = "true")
public final class FixiAssignedOrderWebConfiguration implements WebMvcConfigurer {
  private static final int HTTP_NOT_FOUND = HttpServletResponse.SC_NOT_FOUND;
  private static final int SERVICE_ORDER_PATH_SEGMENT_COUNT = 5;
  private static final int DOCUMENT_PATH_SEGMENT_COUNT = 6;
  private static final int RESERVATION_PATH_SEGMENT_COUNT = 7;
  private static final int RESERVATION_RELEASE_PATH_SEGMENT_COUNT = 8;
  private static final int POS_ORDER_PATH_SEGMENT_COUNT = 8;
  private static final int WARRANTY_PATH_SEGMENT_COUNT = 6;
  private static final int SERVICE_ORDER_ID_SEGMENT = 4;
  private static final int DOCUMENT_ORDER_ID_SEGMENT = 5;
  private static final int EVIDENCE_ID_SEGMENT = 4;
  private static final int RESERVATION_ORDER_ID_SEGMENT = 6;
  private static final int RESERVATION_RELEASE_ORDER_ID_SEGMENT = 5;
  private static final int POS_ORDER_ID_SEGMENT = 6;
  private static final int WARRANTY_ORDER_ID_SEGMENT = 5;
  private static final int WARRANTY_CLAIM_ID_SEGMENT = 4;
  private static final int RELEASE_ACTION_SEGMENT = 7;
  private static final int WARRANTY_CLOSE_ACTION_SEGMENT = 5;
  private final FixiAssignedOrderAccess access;

  @Override
  public final void addInterceptors(final InterceptorRegistry registry) {
    registry.addInterceptor(new HandlerInterceptor() {
      @Override
      public boolean preHandle(
          final HttpServletRequest request,
          final HttpServletResponse response,
          final Object handler) throws Exception {
        final Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities().stream()
            .noneMatch(authority -> authority.getAuthority().startsWith("ROLE_"))) {
          return true;
        }
        final UUID tenantId;
        try {
          tenantId = UUID.fromString(String.valueOf(authentication.getDetails()));
        } catch (final IllegalArgumentException invalidTenant) {
          response.sendError(HTTP_NOT_FOUND);
          return false;
        }
        final UUID orderId = orderIdFor(request.getRequestURI(), tenantId);
        if (orderId == null) {
          return true;
        }
        final Object selectedBranch = request.getAttribute("fixi.authenticatedBranchId");
        final UUID selectedBranchId;
        try {
          selectedBranchId = selectedBranch == null
              ? null : UUID.fromString(selectedBranch.toString());
        } catch (final IllegalArgumentException invalidBranch) {
          response.sendError(HTTP_NOT_FOUND);
          return false;
        }
        if (!access.canAccessOrder(
            authentication.getName(), tenantId, orderId, selectedBranchId)) {
          response.sendError(HTTP_NOT_FOUND);
          return false;
        }
        return true;
      }
    });
  }

  final UUID orderIdFor(final String uri, final UUID tenantId) {
    final String[] parts = uri.split("/");
    try {
      if (uri.startsWith("/api/v1/service-orders/")
          && parts.length >= SERVICE_ORDER_PATH_SEGMENT_COUNT
          && !"customer".equals(parts[SERVICE_ORDER_ID_SEGMENT])
          && !"customer-counts".equals(parts[SERVICE_ORDER_ID_SEGMENT])) {
        return UUID.fromString(parts[SERVICE_ORDER_ID_SEGMENT]);
      }
      if (uri.startsWith("/api/v1/documents/order/")
          && parts.length == DOCUMENT_PATH_SEGMENT_COUNT) {
        return UUID.fromString(parts[DOCUMENT_ORDER_ID_SEGMENT]);
      }
      if (uri.startsWith("/api/v1/documents/")
          && parts.length == DOCUMENT_PATH_SEGMENT_COUNT
          && "content".equals(parts[DOCUMENT_ORDER_ID_SEGMENT])) {
        return access.evidenceOrderId(tenantId, UUID.fromString(parts[EVIDENCE_ID_SEGMENT]));
      }
      if (uri.startsWith("/api/v1/inventory/reservations/order/")
          && parts.length == RESERVATION_PATH_SEGMENT_COUNT) {
        return UUID.fromString(parts[RESERVATION_ORDER_ID_SEGMENT]);
      }
      if (uri.startsWith("/api/v1/inventory/reservations/")
          && parts.length == RESERVATION_RELEASE_PATH_SEGMENT_COUNT
          && "release".equals(parts[RELEASE_ACTION_SEGMENT])) {
        return UUID.fromString(parts[RESERVATION_RELEASE_ORDER_ID_SEGMENT]);
      }
      if (uri.startsWith("/api/v1/inventory/pos/orders/")
          && parts.length == POS_ORDER_PATH_SEGMENT_COUNT
          && "consume".equals(parts[RELEASE_ACTION_SEGMENT])) {
        return UUID.fromString(parts[POS_ORDER_ID_SEGMENT]);
      }
      if (uri.startsWith("/api/v1/warranty-claims/service-order/")
          && parts.length == WARRANTY_PATH_SEGMENT_COUNT) {
        return UUID.fromString(parts[WARRANTY_ORDER_ID_SEGMENT]);
      }
      if (uri.startsWith("/api/v1/warranty-claims/")
          && parts.length == WARRANTY_PATH_SEGMENT_COUNT
          && "close".equals(parts[WARRANTY_CLOSE_ACTION_SEGMENT])) {
        return access.warrantyOrderId(tenantId, UUID.fromString(parts[WARRANTY_CLAIM_ID_SEGMENT]));
      }
    } catch (final IllegalArgumentException ignored) {
      // Non-ID routes and malformed identifiers are left to normal MVC validation/404 handling.
    }
    return null;
  }
}
