package com.hotelpms.monolith.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class MonolithEdgeHeadersFilterTest {

  @AfterEach
  void clearMdc() {
    MDC.clear();
  }

  @Test
  void preservesSafeCorrelationIdAndSetsDefensiveResponseHeaders() throws Exception {
    final MonolithEdgeHeadersFilter filter = new MonolithEdgeHeadersFilter(false);
    final MockHttpServletRequest request = request("/api/v1/customers");
    request.addHeader("X-Correlation-ID", "request-123:trace");
    final MockHttpServletResponse response = new MockHttpServletResponse();
    final List<String> observed = new ArrayList<>();
    final FilterChain chain = (wrappedRequest, wrappedResponse) -> {
      final jakarta.servlet.http.HttpServletRequest httpRequest = asHttpRequest(wrappedRequest);
      observed.add(httpRequest.getHeader("X-Correlation-ID"));
      observed.add(httpRequest.getHeader("X-Client-IP"));
      observed.add(
          String.join(",", java.util.Collections.list(httpRequest.getHeaders("X-Correlation-ID"))));
      observed.add(
          String.join(",", java.util.Collections.list(httpRequest.getHeaders("X-Client-IP"))));
      observed.add(String.join(",", java.util.Collections.list(httpRequest.getHeaderNames())));
      observed.add(MDC.get("correlationId"));
    };

    filter.doFilter(request, response, chain);

    assertEquals("request-123:trace", response.getHeader("X-Correlation-ID"));
    assertEquals("request-123:trace", observed.get(0));
    assertEquals("127.0.0.1", observed.get(1));
    assertEquals("request-123:trace", observed.get(2));
    assertEquals("127.0.0.1", observed.get(3));
    assertTrue(observed.get(4).contains("X-Client-IP"));
    assertTrue(observed.get(4).contains("X-Correlation-ID"));
    assertEquals("request-123:trace", observed.get(5));
    assertEquals("max-age=31536000; includeSubDomains", response.getHeader("Strict-Transport-Security"));
    assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
    assertEquals("DENY", response.getHeader("X-Frame-Options"));
    assertEquals("no-referrer", response.getHeader("Referrer-Policy"));
    assertEquals("camera=(), microphone=(), geolocation=()", response.getHeader("Permissions-Policy"));
    assertEquals("default-src 'none'; frame-ancestors 'none'", response.getHeader("Content-Security-Policy"));
    assertFalse(MDC.get("correlationId") != null);
  }

  @Test
  void replacesUnsafeCorrelationIdAndRestoresExistingMdcValue() throws Exception {
    MDC.put("correlationId", "outer-request");
    final MonolithEdgeHeadersFilter filter = new MonolithEdgeHeadersFilter(false);
    final MockHttpServletRequest request = request("/api/v1/customers");
    request.addHeader("X-Correlation-ID", "invalid correlation id");
    request.addHeader("X-Client-IP", "spoofed-client");
    final MockHttpServletResponse response = new MockHttpServletResponse();
    final List<String> observed = new ArrayList<>();
    final FilterChain chain = (wrappedRequest, wrappedResponse) -> {
      final jakarta.servlet.http.HttpServletRequest httpRequest = asHttpRequest(wrappedRequest);
      observed.add(httpRequest.getHeader("X-Correlation-ID"));
      observed.add(httpRequest.getHeader("X-Client-IP"));
    };

    filter.doFilter(request, response, chain);

    assertNotNull(response.getHeader("X-Correlation-ID"));
    assertTrue(response.getHeader("X-Correlation-ID").matches("[0-9a-f-]{36}"));
    assertEquals(response.getHeader("X-Correlation-ID"), observed.get(0));
    assertEquals("127.0.0.1", observed.get(1));
    assertEquals("outer-request", MDC.get("correlationId"));
  }

  @Test
  void leavesNonceProtectedAccountPagesWithOnlyTheirPageSpecificPolicy() throws Exception {
    final MonolithEdgeHeadersFilter filter = new MonolithEdgeHeadersFilter(false);
    for (final String path :
        List.of("/verify-email", "/reset-password", "/accept-invitation")) {
      final MockHttpServletResponse response = new MockHttpServletResponse();
      filter.doFilter(
          request(path),
          response,
          (wrappedRequest, wrappedResponse) ->
              ((jakarta.servlet.http.HttpServletResponse) wrappedResponse)
                  .setHeader(
                      "Content-Security-Policy",
                      "default-src 'none'; script-src 'nonce-test'; style-src 'nonce-test'; connect-src 'self'"));

      assertEquals(
          List.of(
              "default-src 'none'; script-src 'nonce-test'; style-src 'nonce-test'; connect-src 'self'"),
          response.getHeaders("Content-Security-Policy"),
          path);
    }
  }

  @Test
  void trustsOnlyNumericRealIpWhenProxyTrustIsEnabledAndFallsBackOnInvalidInput() throws Exception {
    final MonolithEdgeHeadersFilter filter = new MonolithEdgeHeadersFilter(true);
    final MockHttpServletRequest request = request("/api/v1/customers");
    request.setRemoteAddr("198.51.100.7");
    request.addHeader("X-Real-IP", "192.0.2.44");
    request.addHeader("X-Client-IP", "spoofed-client");
    assertEquals("192.0.2.44", clientIpSeenByChain(filter, request));

    final MockHttpServletRequest invalidRequest = request("/api/v1/customers");
    invalidRequest.setRemoteAddr("198.51.100.8");
    invalidRequest.addHeader("X-Real-IP", "999.999.999.999");
    assertEquals("198.51.100.8", clientIpSeenByChain(filter, invalidRequest));
  }

  @Test
  void missingOrBlankPeerAddressUsesUnknownIdentity() throws Exception {
    final MonolithEdgeHeadersFilter filter = new MonolithEdgeHeadersFilter(false);
    final MockHttpServletRequest request = request("/api/v1/customers");
    request.setRemoteAddr(" ");

    assertEquals("unknown", clientIpSeenByChain(filter, request));
  }

  private static String clientIpSeenByChain(
      final MonolithEdgeHeadersFilter filter, final MockHttpServletRequest request)
      throws Exception {
    final List<String> clientIp = new ArrayList<>();
    filter.doFilter(
        request,
        new MockHttpServletResponse(),
        (wrappedRequest, wrappedResponse) ->
            clientIp.add(asHttpRequest(wrappedRequest).getHeader("X-Client-IP")));
    return clientIp.getFirst();
  }

  private static jakarta.servlet.http.HttpServletRequest asHttpRequest(
      final ServletRequest request) {
    return (jakarta.servlet.http.HttpServletRequest) request;
  }

  private static MockHttpServletRequest request(final String path) {
    final MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
    request.setRequestURI(path);
    return request;
  }
}
