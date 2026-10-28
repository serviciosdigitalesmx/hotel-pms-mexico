package com.hotelpms.monolith.security;

import com.hotelpms.auth.service.JwtService;
import com.hotelpms.auth.repository.HotelRegistryRepository;
import com.hotelpms.auth.repository.PlatformAdminGrantRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Enumeration;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/** Validates the existing Fixi access JWT at the monolith boundary. */
@Component
public class MonolithJwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Set<String> PUBLIC_EXACT_PATHS =
      Set.of(
          "/api/v1/auth/login",
          "/api/v1/auth/register",
          "/api/v1/auth/refresh",
          "/api/v1/auth/logout",
          "/actuator/health",
          "/actuator/health/liveness",
          "/actuator/health/readiness");
  private static final Set<String> OPERATIONAL_ROLES =
      Set.of("ADMIN", "OWNER", "MANAGER", "TECHNICIAN", "RECEPTIONIST", "KITCHEN", "HOUSEKEEPER");
  private static final Set<String> PRIVILEGED_ROLES = Set.of("ADMIN", "OWNER", "MANAGER");
  private static final Set<String> WRITE_RESTRICTED_PREFIXES =
      Set.of("/api/v1/room-types", "/api/v1/rate-calendar");

  private final JwtService jwtService;
  private final PlatformAdminGrantRepository platformAdminGrants;
  private final HotelRegistryRepository hotelRegistry;

  public MonolithJwtAuthenticationFilter(
      final JwtService jwtService,
      final PlatformAdminGrantRepository platformAdminGrants,
      final HotelRegistryRepository hotelRegistry) {
    this.jwtService = jwtService;
    this.platformAdminGrants = platformAdminGrants;
    this.hotelRegistry = hotelRegistry;
  }

  @Override
  protected void doFilterInternal(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain chain)
      throws ServletException, IOException {
    final String path = request.getRequestURI();
    if (path.startsWith("/internal/") && !MonolithWebhookPath.isWhatsAppWebhook(path)) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    if (HttpMethod.OPTIONS.matches(request.getMethod())) {
      chain.doFilter(request, response);
      return;
    }
    if (!isGatewayRoutedPath(path)) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    if (isSelfAuthenticatingPath(path) || isPublicPath(path)) {
      chain.doFilter(request, response);
      return;
    }

    final String token = accessToken(request);
    if (!StringUtils.hasText(token)) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    final AuthenticatedIdentity identity;
    try {
      identity = extractIdentity(token);
    } catch (final JwtException | IllegalArgumentException exception) {
      SecurityContextHolder.clearContext();
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    if (identity.mustChangePassword() && !isPasswordChangePath(path)) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "PASSWORD_CHANGE_REQUIRED");
      return;
    }
    final boolean platformPath = startsWithPath(path, "/api/v1/auth/platform");
    final boolean platformGrant =
        "SUPER_ADMIN".equals(identity.platformRole())
            && platformAdminGrants.hasActiveGrantForUsername(identity.username());
    if (platformPath && !platformGrant) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }
    if (identity.tenantId() == null
        && (!platformGrant || (!platformPath && !isPlatformSelfServicePath(path)))) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }
    if (!platformPath
        && identity.tenantId() != null
        && hotelRegistry.findById(java.util.UUID.fromString(identity.tenantId()))
            .map(tenant -> "SUSPENDED".equalsIgnoreCase(tenant.getStatus()))
            .orElse(false)) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "TENANT_SUSPENDED");
      return;
    }
    if (!isRoleAllowed(identity.role(), identity.platformRole(), path, request.getMethod())) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    final java.util.List<SimpleGrantedAuthority> authorities =
        new java.util.ArrayList<>(List.of(new SimpleGrantedAuthority("ROLE_" + identity.role())));
    if (platformGrant) {
      authorities.add(new SimpleGrantedAuthority("PLATFORM_SUPER_ADMIN"));
    }
    final var authentication =
        new UsernamePasswordAuthenticationToken(
            identity.username(), null, authorities);
    // Existing service code reads tenant identity from Authentication.details.
    authentication.setDetails(identity.tenantId());
    SecurityContextHolder.getContext().setAuthentication(authentication);
    if (identity.branchId() != null) {
      request.setAttribute("fixi.authenticatedBranchId", identity.branchId());
    }
    chain.doFilter(
        withTrustedIdentity(
            request,
            identity.username(),
            identity.role(),
            identity.tenantId(),
            identity.branchId(),
            token),
        response);
  }

  private AuthenticatedIdentity extractIdentity(final String token) {
    final String username = jwtService.extractUsername(token);
    if (jwtService.isRefreshToken(token) || !jwtService.isTokenValid(token)) {
      throw new JwtException("INVALID_ACCESS_TOKEN");
    }
    final String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
    final String platformRole = jwtService.extractPlatformRole(token);
    final java.util.UUID tenantUuid = jwtService.extractHotelId(token);
    final String tenantId = tenantUuid == null ? null : tenantUuid.toString();
    final java.util.UUID branchUuid = jwtService.extractBranchId(token);
    final String branchId = branchUuid == null ? null : branchUuid.toString();
    final boolean mustChangePassword = jwtService.extractMustChangePassword(token);
    if (!StringUtils.hasText(username) || !StringUtils.hasText(role)) {
      throw new JwtException("INVALID_ACCESS_TOKEN_CLAIMS");
    }
    return new AuthenticatedIdentity(
        username, role, platformRole, tenantId, branchId, mustChangePassword);
  }

  private record AuthenticatedIdentity(
      String username,
      String role,
      String platformRole,
      String tenantId,
      String branchId,
      boolean mustChangePassword) {}

  /** Recreates identity headers formerly injected by the trusted API Gateway. */
  private static HttpServletRequest withTrustedIdentity(
      final HttpServletRequest request,
      final String username,
      final String role,
      final String tenantId,
      final String branchId,
      final String accessToken) {
    final Map<String, String> identityHeaders = new LinkedHashMap<>();
    identityHeaders.put("x-auth-user", username);
    identityHeaders.put("x-auth-role", role);
    if (tenantId != null) {
      identityHeaders.put("x-auth-hotel", tenantId);
    }
    if (branchId != null) {
      identityHeaders.put("x-auth-branch", branchId);
    }
    return new HttpServletRequestWrapper(request) {
      @Override
      public Cookie[] getCookies() {
        final Cookie[] existing = super.getCookies();
        final java.util.List<Cookie> authenticatedCookies =
            existing == null ? new java.util.ArrayList<>() : new java.util.ArrayList<>(java.util.Arrays.asList(existing));
        authenticatedCookies.removeIf(cookie -> "jwt".equals(cookie.getName()));
        authenticatedCookies.add(new Cookie("jwt", accessToken));
        return authenticatedCookies.toArray(Cookie[]::new);
      }

      @Override
      public String getHeader(final String name) {
        return isIdentityHeader(name)
            ? identityHeaders.get(normalizeHeader(name))
            : super.getHeader(name);
      }

      @Override
      public Enumeration<String> getHeaders(final String name) {
        if (isIdentityHeader(name)) {
          final String trustedValue = identityHeaders.get(normalizeHeader(name));
          return trustedValue == null
              ? Collections.emptyEnumeration()
              : Collections.enumeration(List.of(trustedValue));
        }
        return super.getHeaders(name);
      }

      @Override
      public Enumeration<String> getHeaderNames() {
        final java.util.List<String> names = Collections.list(super.getHeaderNames()).stream()
            .filter(name -> !isIdentityHeader(name))
            .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        names.addAll(identityHeaders.keySet());
        return Collections.enumeration(names);
      }
    };
  }

  private static boolean isIdentityHeader(final String name) {
    if (name == null) {
      return false;
    }
    final String normalized = normalizeHeader(name);
    return normalized.equals("x-auth-user")
        || normalized.equals("x-auth-role")
        || normalized.equals("x-auth-hotel")
        || normalized.equals("x-auth-branch");
  }

  private static String normalizeHeader(final String name) {
    return name.toLowerCase(Locale.ROOT);
  }

  private static String accessToken(final HttpServletRequest request) {
    final String authorization = request.getHeader("Authorization");
    if (StringUtils.hasText(authorization)
        && authorization.regionMatches(true, 0, "Bearer ", 0, "Bearer ".length())) {
      return authorization.substring(7).trim();
    }
    final Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (final Cookie cookie : cookies) {
        if ("jwt".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  private static boolean isPublicPath(final String path) {
    return PUBLIC_EXACT_PATHS.contains(path)
        || path.equals("/verify-email")
        || path.equals("/accept-invitation")
        || path.equals("/reset-password")
        || path.equals("/actuator")
        || path.startsWith("/actuator/")
        || MonolithWebhookPath.isWhatsAppWebhook(path)
        || path.startsWith("/api/v1/auth/public/")
        || path.startsWith("/api/public/portal/")
        || path.startsWith("/api/v1/public/requests")
        || isOpenApiPath(path)
        || (path.startsWith("/api/public/hotels/") && path.endsWith("/reservations"));
  }

  private static boolean isSelfAuthenticatingPath(final String path) {
    return path.equals("/api/v1/auth/login")
        || path.equals("/api/v1/auth/register")
        || path.equals("/api/v1/auth/refresh")
        || path.equals("/api/v1/auth/logout")
        || path.startsWith("/api/v1/auth/public/")
        || isOpenApiPath(path);
  }

  private static boolean isPasswordChangePath(final String path) {
    return path.equals("/api/v1/auth/change-password")
        || path.equals("/api/v1/auth/me")
        || path.equals("/api/v1/auth/logout")
        || path.equals("/api/v1/auth/refresh");
  }

  private static boolean isPlatformSelfServicePath(final String path) {
    return path.equals("/api/v1/auth/me") || path.equals("/api/v1/auth/change-password");
  }

  private static boolean isRoleAllowed(
      final String role, final String platformRole, final String path, final String methodName) {
    if (startsWithPath(path, "/api/v1/auth/platform")) {
      return "SUPER_ADMIN".equals(platformRole);
    }
    // The Gateway applies its authentication/RBAC filter only to these auth-service route
    // groups. Other auth routes deliberately rely on their own token-aware controller/service
    // contract; do not add a second, stricter role policy merely because they share a prefix.
    if (startsWithPath(path, "/api/v1/auth") && !isGatewayFilteredAuthPath(path)) {
      return true;
    }
    if (!OPERATIONAL_ROLES.contains(role)) {
      return false;
    }
    if (PRIVILEGED_ROLES.contains(role)) {
      return true;
    }
    if ("KITCHEN".equals(role)) {
      return path.startsWith("/api/v1/fb/orders")
          || (path.equals("/api/v1/stays/settings") && "GET".equals(methodName));
    }
    if ("HOUSEKEEPER".equals(role)) {
      return ("GET".equals(methodName) && path.startsWith("/api/v1/rooms"))
          || ("PATCH".equals(methodName)
              && path.startsWith("/api/v1/rooms/")
              && path.endsWith("/status"))
          || (path.equals("/api/v1/stays/settings") && "GET".equals(methodName));
    }
    if ("TECHNICIAN".equals(role)) {
      return path.startsWith("/api/v1/service-orders")
          || path.startsWith("/api/v1/inventory")
          || path.startsWith("/api/v1/documents")
          || path.startsWith("/api/v1/workflows");
    }
    if (path.startsWith("/api/v1/reports")) {
      return false;
    }
    if (path.startsWith("/api/v1/auth/users")
        || (path.startsWith("/api/v1/rooms")
            && Set.of("POST", "PUT", "DELETE").contains(methodName))) {
      return false;
    }
    return WRITE_RESTRICTED_PREFIXES.stream()
        .noneMatch(prefix -> path.startsWith(prefix) && !"GET".equals(methodName));
  }

  private static boolean isGatewayFilteredAuthPath(final String path) {
    return startsWithPath(path, "/api/v1/auth/users")
        || startsWithPath(path, "/api/v1/auth/platform")
        || startsWithPath(path, "/api/v1/auth/branches")
        || startsWithPath(path, "/api/v1/auth/industry-profile")
        || startsWithPath(path, "/api/v1/auth/invitations")
        || startsWithPath(path, "/api/v1/auth/onboarding")
        || startsWithPath(path, "/api/v1/auth/capabilities");
  }

  /**
   * Only exposes paths that the former Gateway actually routed. Component-scanning all bounded
   * contexts must not accidentally publish their internal-only controllers in the monolith.
   */
  private static boolean isGatewayRoutedPath(final String path) {
    // Services expose management endpoints on a dedicated internal port. Keep
    // Actuator access public there just as the former service security chains did.
    if (path.equals("/actuator") || path.startsWith("/actuator/")) {
      return true;
    }
    return MonolithWebhookPath.isWhatsAppWebhook(path)
        || path.equals("/verify-email")
        || path.equals("/accept-invitation")
        || path.equals("/reset-password")
        || path.startsWith("/api/v1/auth/")
        || path.equals("/api/v1/auth")
        || startsWithPath(path, "/api/v1/guests")
        || startsWithPath(path, "/api/v1/customers")
        || startsWithPath(path, "/api/v1/rooms")
        || startsWithPath(path, "/api/v1/room-types")
        || startsWithPath(path, "/api/v1/reservations")
        || startsWithPath(path, "/api/v1/stays")
        || startsWithPath(path, "/api/v1/quotations")
        || startsWithPath(path, "/api/v1/rate-calendar")
        || startsWithPath(path, "/api/v1/service-orders")
        || startsWithPath(path, "/api/v1/service-requests")
        || startsWithPath(path, "/api/v1/workflows")
        || startsWithPath(path, "/api/v1/inventory")
        || startsWithPath(path, "/api/v1/procurement")
        || startsWithPath(path, "/api/v1/warranty-claims")
        || startsWithPath(path, "/api/v1/documents")
        || startsWithPath(path, "/api/v1/time-entries")
        || startsWithPath(path, "/api/v1/whatsapp")
        || startsWithPath(path, "/api/v1/portal")
        || startsWithPath(path, "/api/v1/services")
        || startsWithPath(path, "/api/v1/audit-events")
        || startsWithPath(path, "/api/v1/notifications/preferences")
        || startsWithPath(path, "/api/v1/expenses")
        || startsWithPath(path, "/api/public/portal")
        || startsWithPath(path, "/api/v1/public/requests")
        || (path.startsWith("/api/public/hotels/") && path.endsWith("/reservations"))
        || startsWithPath(path, "/api/v1/invoices")
        || startsWithPath(path, "/api/v1/reports")
        || startsWithPath(path, "/api/v1/cash")
        || path.startsWith("/api/v1/fb/")
        || isOpenApiPath(path)
        || path.equals("/actuator/health")
        || path.startsWith("/actuator/health/");
  }

  private static boolean startsWithPath(final String path, final String prefix) {
    return path.equals(prefix) || path.startsWith(prefix + "/");
  }

  private static boolean isOpenApiPath(final String path) {
    return path.equals("/v3/api-docs")
        || path.startsWith("/v3/api-docs/")
        || path.equals("/swagger-ui.html")
        || path.startsWith("/swagger-ui/")
        || path.matches("/(auth|frontdesk|guest|billing|fb)-service/v3/api-docs");
  }
}
