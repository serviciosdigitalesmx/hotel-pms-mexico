package com.hotelpms.monolith.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;

/** Servlet equivalent of the former Gateway double-submit CSRF check. */
public final class MonolithCsrfFilter extends OncePerRequestFilter {

  private static final String CSRF_COOKIE = "csrf_token";
  private static final String CSRF_HEADER = "X-CSRF-Token";
  private static final Set<String> EXCLUDED_EXACT_PATHS =
      Set.of("/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/register");

  @Override
  protected void doFilterInternal(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain chain)
      throws ServletException, IOException {
    if (isSafeMethod(request.getMethod()) || isExcludedPath(request.getRequestURI())) {
      chain.doFilter(request, response);
      return;
    }
    // Bearer tokens are explicitly attached by native/API clients rather than
    // ambient browser credentials. Do not require a browser CSRF cookie when
    // the request is bearer-only; cookie-authenticated browser writes still
    // use the same double-submit check as the former Gateway.
    if (hasBearerAuthenticationWithoutJwtCookie(request)) {
      chain.doFilter(request, response);
      return;
    }

    final String cookieToken = csrfCookie(request.getCookies());
    final String headerToken = request.getHeader(CSRF_HEADER);
    if (cookieToken == null
        || cookieToken.isBlank()
        || headerToken == null
        || headerToken.isBlank()
        || !cookieToken.equals(headerToken)) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return;
    }
    chain.doFilter(request, response);
  }

  private static boolean isSafeMethod(final String method) {
    return HttpMethod.GET.matches(method)
        || HttpMethod.HEAD.matches(method)
        || HttpMethod.OPTIONS.matches(method)
        || HttpMethod.TRACE.matches(method);
  }

  private static boolean isExcludedPath(final String path) {
    return EXCLUDED_EXACT_PATHS.contains(path)
        || path.startsWith("/api/v1/auth/public/")
        || path.startsWith("/api/public/portal/")
        || (path.startsWith("/api/public/hotels/") && path.endsWith("/reservations"));
  }

  private static String csrfCookie(final Cookie[] cookies) {
    if (cookies == null) {
      return null;
    }
    for (final Cookie cookie : cookies) {
      if (CSRF_COOKIE.equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }

  private static boolean hasBearerAuthenticationWithoutJwtCookie(
      final HttpServletRequest request) {
    final String authorization = request.getHeader("Authorization");
    if (authorization == null
        || !authorization.regionMatches(true, 0, "Bearer ", 0, "Bearer ".length())
        || authorization.substring("Bearer ".length()).isBlank()) {
      return false;
    }
    final Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return true;
    }
    for (final Cookie cookie : cookies) {
      if ("jwt".equals(cookie.getName())) {
        return false;
      }
    }
    return true;
  }
}
