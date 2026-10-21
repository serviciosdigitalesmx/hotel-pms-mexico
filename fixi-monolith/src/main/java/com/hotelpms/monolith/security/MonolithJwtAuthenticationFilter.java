package com.hotelpms.monolith.security;

import com.hotelpms.auth.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
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
          "/api/v1/auth/change-password",
          "/api/v1/auth/me",
          "/api/v1/auth/session-security",
          "/actuator/health",
          "/actuator/health/liveness",
          "/actuator/health/readiness");
  private static final Set<String> OPERATIONAL_ROLES =
      Set.of("ADMIN", "OWNER", "MANAGER", "TECHNICIAN", "RECEPTIONIST", "KITCHEN", "HOUSEKEEPER");
  private static final Set<String> PRIVILEGED_ROLES = Set.of("ADMIN", "OWNER", "MANAGER");
  private static final Set<String> WRITE_RESTRICTED_PREFIXES =
      Set.of("/api/v1/room-types", "/api/v1/rate-calendar");

  private final JwtService jwtService;

  public MonolithJwtAuthenticationFilter(final JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain chain)
      throws ServletException, IOException {
    final String path = request.getRequestURI();
    if (path.startsWith("/internal/")) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    final String token = accessToken(request);
    if (!StringUtils.hasText(token)) {
      if (isPublicPath(path)) {
        chain.doFilter(request, response);
      } else {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      }
      return;
    }

    try {
      final String username = jwtService.extractUsername(token);
      if (jwtService.isRefreshToken(token) || !jwtService.isTokenValid(token)) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }
      final String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
      final String tenantId = jwtService.extractHotelId(token).toString();
      final String branchId =
          jwtService.extractBranchId(token) == null ? null : jwtService.extractBranchId(token).toString();
      if (!StringUtils.hasText(username)
          || !StringUtils.hasText(role)
          || !StringUtils.hasText(tenantId)) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }
      if (jwtService.extractMustChangePassword(token) && !isPasswordChangePath(path)) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "PASSWORD_CHANGE_REQUIRED");
        return;
      }
      if (!isRoleAllowed(role, path, request.getMethod())) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
      }

      final var authentication =
          new UsernamePasswordAuthenticationToken(
              username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
      // Existing service code reads tenant identity from Authentication.details.
      authentication.setDetails(tenantId);
      SecurityContextHolder.getContext().setAuthentication(authentication);
      if (branchId != null) {
        request.setAttribute("fixi.authenticatedBranchId", branchId);
      }
      chain.doFilter(request, response);
    } catch (final JwtException | IllegalArgumentException exception) {
      SecurityContextHolder.clearContext();
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }

  private static String accessToken(final HttpServletRequest request) {
    final String authorization = request.getHeader("Authorization");
    if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
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
        || path.startsWith("/api/v1/auth/public/")
        || path.startsWith("/api/public/portal/")
        || path.startsWith("/api/v1/public/requests")
        || (path.startsWith("/api/public/hotels/") && path.endsWith("/reservations"));
  }

  private static boolean isPasswordChangePath(final String path) {
    return path.equals("/api/v1/auth/change-password")
        || path.equals("/api/v1/auth/me")
        || path.equals("/api/v1/auth/logout")
        || path.equals("/api/v1/auth/refresh");
  }

  private static boolean isRoleAllowed(
      final String role, final String path, final String methodName) {
    if (path.startsWith("/api/v1/auth/platform/")) {
      return "ADMIN".equals(role);
    }
    if (!OPERATIONAL_ROLES.contains(role)) {
      return false;
    }
    if (PRIVILEGED_ROLES.contains(role)) {
      return true;
    }
    final HttpMethod method = HttpMethod.valueOf(methodName);
    if ("KITCHEN".equals(role)) {
      return path.startsWith("/api/v1/fb/orders")
          || (path.equals("/api/v1/stays/settings") && method == HttpMethod.GET);
    }
    if ("HOUSEKEEPER".equals(role)) {
      return (method == HttpMethod.GET && path.startsWith("/api/v1/rooms"))
          || (method == HttpMethod.PATCH
              && path.startsWith("/api/v1/rooms/")
              && path.endsWith("/status"))
          || (path.equals("/api/v1/stays/settings") && method == HttpMethod.GET);
    }
    if ("TECHNICIAN".equals(role)) {
      return path.startsWith("/api/v1/service-orders")
          || path.startsWith("/api/v1/inventory")
          || path.startsWith("/api/v1/documents")
          || path.startsWith("/api/v1/workflows");
    }
    if (path.startsWith("/api/v1/auth/users")
        || (path.startsWith("/api/v1/rooms")
            && Set.of("POST", "PUT", "DELETE").contains(methodName))
        || path.startsWith("/api/v1/reports")) {
      return false;
    }
    return WRITE_RESTRICTED_PREFIXES.stream()
        .noneMatch(prefix -> path.startsWith(prefix) && method != HttpMethod.GET);
  }
}
