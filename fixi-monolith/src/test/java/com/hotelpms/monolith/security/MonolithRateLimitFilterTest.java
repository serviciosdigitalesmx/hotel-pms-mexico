package com.hotelpms.monolith.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class MonolithRateLimitFilterTest {

  @Mock private StringRedisTemplate redis;

  private MonolithRateLimitFilter filter;

  @BeforeEach
  void setUp() {
    filter = new MonolithRateLimitFilter(redis);
  }

  @Test
  void appliesGatewayAndUserScopedBucketsToFixiRoutes() throws Exception {
    final List<String> keys = new ArrayList<>();
    when(redis.execute(any(RedisScript.class), anyList(), any(Object[].class)))
        .thenAnswer(invocation -> {
          final List<?> redisKeys = invocation.getArgument(1);
          keys.add((String) redisKeys.getFirst());
          return 1L;
        });

    final List<RouteBucket> routes = List.of(
        new RouteBucket("/api/v1/auth/platform/tenants", "auth-platform"),
        new RouteBucket(
            "/internal/whatsapp/webhook/00000000-0000-0000-0000-000000000001",
            "whatsapp-webhook"),
        new RouteBucket("/api/v1/auth/users/123", "auth-users"),
        new RouteBucket("/api/v1/auth/branches/123", "auth-branches"),
        new RouteBucket("/api/v1/auth/industry-profile/123", "auth-industry-profile"),
        new RouteBucket("/api/v1/auth/invitations/123", "auth-fixi-identity"),
        new RouteBucket("/api/v1/auth/onboarding/123", "auth-fixi-identity"),
        new RouteBucket("/api/v1/auth/capabilities/123", "auth-capabilities"),
        new RouteBucket("/api/v1/auth/login", "auth"),
        new RouteBucket("/api/v1/customers/123", "guest-service"),
        new RouteBucket("/api/v1/audit-events/123", "audit-events"),
        new RouteBucket("/api/v1/services/123", "service-catalog"),
        new RouteBucket(
            "/api/v1/notifications/preferences/123", "notification-preferences"),
        new RouteBucket("/api/v1/service-orders/123", "frontdesk-service"),
        new RouteBucket("/api/public/portal/123", "public-portal"),
        new RouteBucket("/api/v1/public/requests/123", "public-service-requests"),
        new RouteBucket(
            "/api/public/hotels/123/reservations", "public-booking-reservations"),
        new RouteBucket("/api/v1/invoices/123", "billing-service"),
        new RouteBucket("/api/v1/fb/orders/123", "fb-service"));

    for (final RouteBucket route : routes) {
      final MockHttpServletRequest request = request(route.path());
      request.addHeader("X-Client-IP", "192.0.2.10");
      request.addHeader("X-Auth-User", "operator-a");
      final MockHttpServletResponse response = new MockHttpServletResponse();
      final MockFilterChain chain = new MockFilterChain();

      filter.doFilter(request, response, chain);

      assertEquals(200, response.getStatus(), route.path());
      assertTrue(chain.getRequest() != null, route.path());
    assertTrue(
        keys.contains("fixi:rate-limit:" + route.bucket() + ":" + expectedIdentity(route.bucket())),
        route.path());
      keys.clear();
    }
  }

  @Test
  void doesNotCallRedisForRoutesWithoutConfiguredLimits() throws Exception {
    final MockHttpServletResponse response = new MockHttpServletResponse();
    final MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request("/api/v1/unknown-resource"), response, chain);

    assertEquals(200, response.getStatus());
    assertTrue(chain.getRequest() != null);
    org.mockito.Mockito.verifyNoInteractions(redis);
  }

  @Test
  void rejectsExhaustedBucketWithRetryAfterHeader() throws Exception {
    when(redis.execute(any(RedisScript.class), anyList(), any(Object[].class))).thenReturn(0L);
    final MockHttpServletResponse response = new MockHttpServletResponse();
    final MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request("/api/v1/customers/123"), response, chain);

    assertEquals(429, response.getStatus());
    assertEquals("1", response.getHeader("Retry-After"));
    assertFalse(chain.getRequest() != null);
  }

  @Test
  void failsClosedWhenRedisIsUnavailable() throws Exception {
    when(redis.execute(any(RedisScript.class), anyList(), any(Object[].class)))
        .thenThrow(new DataAccessResourceFailureException("redis unavailable"));
    final MockHttpServletResponse response = new MockHttpServletResponse();
    final MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request("/api/v1/customers/123"), response, chain);

    assertEquals(503, response.getStatus());
    assertFalse(chain.getRequest() != null);
  }

  @Test
  void validWebhookPathUsesDedicatedPublicIdentityBucketAndInvalidPathIsUnrated() throws Exception {
    final List<String> keys = new ArrayList<>();
    when(redis.execute(any(RedisScript.class), anyList(), any(Object[].class)))
        .thenAnswer(invocation -> {
          final List<?> redisKeys = invocation.getArgument(1);
          keys.add((String) redisKeys.getFirst());
          return 1L;
        });
    final MockHttpServletResponse validResponse = new MockHttpServletResponse();

    filter.doFilter(
        request("/internal/whatsapp/webhook/00000000-0000-0000-0000-000000000001"),
        validResponse,
        new MockFilterChain());

    assertTrue(keys.contains("fixi:rate-limit:whatsapp-webhook:ip:unknown"));
    keys.clear();
    final MockHttpServletResponse invalidResponse = new MockHttpServletResponse();
    final MockFilterChain invalidChain = new MockFilterChain();

    filter.doFilter(request("/internal/whatsapp/webhook/not-a-uuid"), invalidResponse, invalidChain);

    assertEquals(200, validResponse.getStatus());
    assertEquals(200, invalidResponse.getStatus());
    assertTrue(invalidChain.getRequest() != null);
    assertTrue(keys.isEmpty());
  }

  private static String expectedIdentity(final String bucket) {
    return bucket.startsWith("public-")
            || bucket.equals("whatsapp-webhook")
            || bucket.equals("auth")
        ? "ip:192.0.2.10"
        : "user:operator-a";
  }

  private static MockHttpServletRequest request(final String path) {
    final MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
    request.setRequestURI(path);
    return request;
  }

  private record RouteBucket(String path, String bucket) {}
}
