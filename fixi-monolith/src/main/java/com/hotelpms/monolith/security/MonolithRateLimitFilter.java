package com.hotelpms.monolith.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

/** Redis token-bucket equivalent of the API Gateway route limits. */
public final class MonolithRateLimitFilter extends OncePerRequestFilter {

  private static final DefaultRedisScript<Long> TOKEN_BUCKET =
      new DefaultRedisScript<>(
          "local cap=tonumber(ARGV[1]); local rate=tonumber(ARGV[2]); "
              + "local t=redis.call('TIME'); local now=tonumber(t[1])+tonumber(t[2])/1000000; "
              + "local tokens=tonumber(redis.call('HGET',KEYS[1],'tokens')) or cap; "
              + "local last=tonumber(redis.call('HGET',KEYS[1],'updated')) or now; "
              + "tokens=math.min(cap,tokens+math.max(0,now-last)*rate); "
              + "local allowed=0; if tokens>=1 then tokens=tokens-1; allowed=1 end; "
              + "redis.call('HSET',KEYS[1],'tokens',tokens,'updated',now); "
              + "redis.call('PEXPIRE',KEYS[1],math.ceil(cap/rate*2000)); return allowed",
          Long.class);

  private final StringRedisTemplate redis;
  public MonolithRateLimitFilter(final StringRedisTemplate redis) {
    this.redis = redis;
  }

  @Override
  protected void doFilterInternal(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain filterChain)
      throws ServletException, IOException {
    for (final Limit limit : limitsFor(request)) {
      final String identity =
          limit.perUser ? authenticatedUser(request) : "ip:" + trustedClientIp(request);
      final String key = "fixi:rate-limit:" + limit.bucket + ":" + identity;
      try {
        final Long allowed =
            redis.execute(
                TOKEN_BUCKET,
                List.of(key),
                Integer.toString(limit.capacity),
                Integer.toString(limit.refillPerSecond));
        if (!Long.valueOf(1L).equals(allowed)) {
          response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
          response.setHeader("Retry-After", "1");
          return;
        }
      } catch (final DataAccessException exception) {
        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "RATE_LIMITER_UNAVAILABLE");
        return;
      }
    }
    filterChain.doFilter(request, response);
  }

  private static String trustedClientIp(final HttpServletRequest request) {
    final String trustedIp = request.getHeader("X-Client-IP");
    return trustedIp == null || trustedIp.isBlank() ? "unknown" : trustedIp;
  }

  private static String authenticatedUser(final HttpServletRequest request) {
    final String user = request.getHeader("X-Auth-User");
    return user == null || user.isBlank() ? "ip:" + trustedClientIp(request) : "user:" + user;
  }

  private static List<Limit> limitsFor(final HttpServletRequest request) {
    final String path = request.getRequestURI();
    final List<Limit> limits = new ArrayList<>(2);
    if (hasGatewayRoute(path)) {
      limits.add(
          new Limit(
              "default",
              configured("GW_DEFAULT_RATE_LIMIT_REPLENISH_RATE", 10),
              configured("GW_DEFAULT_RATE_LIMIT_BURST_CAPACITY", 20),
              false));
    }
    final Limit routeLimit = routeLimitFor(path);
    if (routeLimit != null) {
      limits.add(routeLimit);
    }
    return limits;
  }

  private static Limit routeLimitFor(final String path) {
    // Keep the exact, route-scoped buckets from Config Server. In particular, a
    // noisy authenticated user must not consume another user's allowance.
    if (startsWithAny(path, "/api/v1/auth/platform/")) {
      return new Limit("auth-platform", 2, 5, true);
    }
    if (MonolithWebhookPath.isWhatsAppWebhook(path)) {
      return new Limit("whatsapp-webhook", 5, 10, false);
    }
    if (startsWithAny(path, "/api/v1/auth/users/")) {
      return new Limit("auth-users", 10, 20, true);
    }
    if (startsWithAny(path, "/api/v1/auth/branches/")) {
      return new Limit("auth-branches", 10, 20, true);
    }
    if (startsWithAny(path, "/api/v1/auth/industry-profile/")) {
      return new Limit("auth-industry-profile", 10, 20, true);
    }
    if (path.startsWith("/api/v1/auth/invitations/") || path.startsWith("/api/v1/auth/onboarding/")) {
      return new Limit("auth-fixi-identity", 5, 10, true);
    }
    if (path.startsWith("/api/v1/auth/capabilities/")) {
      return new Limit("auth-capabilities", 10, 20, true);
    }
    if (path.startsWith("/api/v1/auth/")) {
      return new Limit(
          "auth",
          configured("GW_AUTH_RATE_LIMIT_REPLENISH_RATE", 5),
          configured("GW_AUTH_RATE_LIMIT_BURST_CAPACITY", 10),
          false);
    }
    if (startsWithAny(path, "/api/v1/guests/", "/api/v1/customers/")) {
      return new Limit("guest-service", 20, 50, true);
    }
    // These Fixi-native APIs are consumed by the current frontend but were not
    // present in the legacy Gateway route table. Keep their quotas isolated.
    if (startsWithAny(path, "/api/v1/audit-events/")) {
      return new Limit("audit-events", 20, 50, true);
    }
    if (startsWithAny(path, "/api/v1/services/")) {
      return new Limit("service-catalog", 20, 50, true);
    }
    if (startsWithAny(path, "/api/v1/notifications/preferences/")) {
      return new Limit("notification-preferences", 20, 50, true);
    }
    if (isFrontdeskPath(path)) {
      return new Limit("frontdesk-service", 20, 50, true);
    }
    if (path.startsWith("/api/public/portal/")) {
      return new Limit("public-portal", 20, 50, false);
    }
    if (path.startsWith("/api/v1/public/requests/")) {
      return new Limit("public-service-requests", 5, 10, false);
    }
    if (isPublicBookingPath(path)) {
      return new Limit("public-booking-reservations", 10, 20, false);
    }
    if (startsWithAny(
        path, "/api/v1/invoices/", "/api/v1/reports/", "/api/v1/cash/", "/api/v1/expenses/")) {
      return new Limit("billing-service", 20, 50, true);
    }
    if (startsWithAny(path, "/api/v1/fb/")) {
      return new Limit("fb-service", 20, 50, true);
    }
    return null;
  }

  private static boolean hasGatewayRoute(final String path) {
    return startsWithAny(
            path,
            "/api/v1/auth/",
            "/api/v1/guests/",
            "/api/v1/customers/",
            "/api/v1/rooms/",
            "/api/v1/room-types/",
            "/api/v1/reservations/",
            "/api/v1/stays/",
            "/api/v1/quotations/",
            "/api/v1/rate-calendar/",
            "/api/v1/service-orders/",
            "/api/v1/service-requests/",
            "/api/v1/workflows/",
            "/api/v1/inventory/",
            "/api/v1/procurement/",
            "/api/v1/warranty-claims/",
            "/api/v1/documents/",
            "/api/v1/time-entries/",
            "/api/v1/whatsapp/",
            "/api/v1/portal/",
            "/api/public/portal/",
            "/api/v1/public/requests/",
            "/api/public/hotels/",
            "/api/v1/invoices/",
            "/api/v1/reports/",
            "/api/v1/cash/",
            "/api/v1/fb/",
            "/api/v1/expenses/",
            "/api/v1/services/",
            "/api/v1/audit-events/",
            "/api/v1/notifications/preferences/",
            "/internal/whatsapp/webhook/",
            "/v3/api-docs/",
            "/swagger-ui/",
            "/auth-service/v3/api-docs",
            "/frontdesk-service/v3/api-docs",
            "/guest-service/v3/api-docs",
            "/billing-service/v3/api-docs",
            "/fb-service/v3/api-docs")
        || path.equals("/api/v1/auth")
        || MonolithWebhookPath.isWhatsAppWebhook(path)
        || path.equals("/api/v1/guests")
        || path.equals("/api/v1/customers")
        || path.equals("/api/public/portal")
        || path.equals("/v3/api-docs")
        || path.equals("/swagger-ui.html");
  }

  private static boolean isFrontdeskPath(final String path) {
    return startsWithAny(
        path,
        "/api/v1/rooms/",
        "/api/v1/room-types/",
        "/api/v1/reservations/",
        "/api/v1/stays/",
        "/api/v1/quotations/",
        "/api/v1/rate-calendar/",
        "/api/v1/service-orders/",
        "/api/v1/service-requests/",
        "/api/v1/workflows/",
        "/api/v1/inventory/",
        "/api/v1/procurement/",
        "/api/v1/warranty-claims/",
        "/api/v1/documents/",
        "/api/v1/time-entries/",
        "/api/v1/whatsapp/",
        "/api/v1/portal/");
  }

  private static boolean isPublicBookingPath(final String path) {
    return path.startsWith("/api/public/hotels/") && path.endsWith("/reservations");
  }

  private static boolean startsWithAny(final String path, final String... prefixes) {
    for (final String prefix : prefixes) {
      final String base = prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
      if (path.equals(base) || path.startsWith(base + "/")) {
        return true;
      }
    }
    return false;
  }

  private static int configured(final String name, final int fallback) {
    final String value = System.getenv(name);
    if (value == null || value.isBlank()) {
      return fallback;
    }
    try {
      final int parsed = Integer.parseInt(value);
      return parsed > 0 ? parsed : fallback;
    } catch (final NumberFormatException exception) {
      return fallback;
    }
  }

  private record Limit(String bucket, int refillPerSecond, int capacity, boolean perUser) {}
}
