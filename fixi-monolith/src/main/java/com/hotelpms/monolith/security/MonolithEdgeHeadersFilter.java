package com.hotelpms.monolith.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Enumeration;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

/** Preserves the gateway's request correlation and defensive response headers in the single process. */
public final class MonolithEdgeHeadersFilter extends OncePerRequestFilter {

  private static final String CORRELATION_HEADER = "X-Correlation-ID";
  private static final String CLIENT_IP_HEADER = "X-Client-IP";
  private static final Pattern SAFE_CORRELATION_ID = Pattern.compile("[A-Za-z0-9._:-]{1,128}");
  private static final Pattern NUMERIC_IP = Pattern.compile("[0-9a-fA-F:.]{2,64}");
  private final boolean trustProxy;

  public MonolithEdgeHeadersFilter(final boolean trustProxy) {
    this.trustProxy = trustProxy;
  }

  @Override
  protected void doFilterInternal(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain filterChain)
      throws ServletException, IOException {
    final String suppliedId = request.getHeader(CORRELATION_HEADER);
    final String correlationId =
        suppliedId != null && SAFE_CORRELATION_ID.matcher(suppliedId).matches()
            ? suppliedId
            : UUID.randomUUID().toString();
    response.setHeader(CORRELATION_HEADER, correlationId);
    response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("X-Frame-Options", "DENY");
    response.setHeader("Referrer-Policy", "no-referrer");
    response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
    if (!usesPageSpecificContentSecurityPolicy(request.getRequestURI())) {
      response.setHeader("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'");
    }
    final String previousCorrelationId = MDC.get("correlationId");
    MDC.put("correlationId", correlationId);
    try {
      filterChain.doFilter(withTrustedHeaders(request, correlationId), response);
    } finally {
      if (previousCorrelationId == null) {
        MDC.remove("correlationId");
      } else {
        MDC.put("correlationId", previousCorrelationId);
      }
    }
  }

  private static boolean usesPageSpecificContentSecurityPolicy(final String path) {
    return "/verify-email".equals(path)
        || "/reset-password".equals(path)
        || "/accept-invitation".equals(path);
  }

  private HttpServletRequest withTrustedHeaders(
      final HttpServletRequest request, final String correlationId) {
    final String trustedIp = resolveClientIp(request);
    return new HttpServletRequestWrapper(request) {
      @Override
      public String getHeader(final String name) {
        if (CLIENT_IP_HEADER.equalsIgnoreCase(name)) {
          return trustedIp;
        }
        return CORRELATION_HEADER.equalsIgnoreCase(name)
            ? correlationId
            : super.getHeader(name);
      }

      @Override
      public Enumeration<String> getHeaders(final String name) {
        if (CLIENT_IP_HEADER.equalsIgnoreCase(name)) {
          return Collections.enumeration(java.util.List.of(trustedIp));
        }
        if (CORRELATION_HEADER.equalsIgnoreCase(name)) {
          return Collections.enumeration(java.util.List.of(correlationId));
        }
        return super.getHeaders(name);
      }

      @Override
      public Enumeration<String> getHeaderNames() {
        final java.util.List<String> names = Collections.list(super.getHeaderNames()).stream()
            .filter(
                header ->
                    !CLIENT_IP_HEADER.equalsIgnoreCase(header)
                        && !CORRELATION_HEADER.equalsIgnoreCase(header))
            .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        names.add(CLIENT_IP_HEADER);
        names.add(CORRELATION_HEADER);
        return Collections.enumeration(names);
      }
    };
  }

  private String resolveClientIp(final HttpServletRequest request) {
    if (trustProxy) {
      final String realIp = request.getHeader("X-Real-IP");
      if (realIp != null && NUMERIC_IP.matcher(realIp).matches()) {
        try {
          return InetAddress.getByName(realIp).getHostAddress();
        } catch (final IOException ignored) {
          // Invalid trusted-proxy input falls back to the TCP peer.
        }
      }
    }
    final String remoteAddress = request.getRemoteAddr();
    return remoteAddress == null || remoteAddress.isBlank() ? "unknown" : remoteAddress;
  }
}
