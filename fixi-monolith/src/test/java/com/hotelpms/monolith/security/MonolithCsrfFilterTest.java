package com.hotelpms.monolith.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class MonolithCsrfFilterTest {

  private final MonolithCsrfFilter filter = new MonolithCsrfFilter();

  @Test
  void safeMethodsAndAuthenticationBootstrapRoutesDoNotRequireCsrf() throws Exception {
    for (final String method : new String[] {"GET", "HEAD", "OPTIONS", "TRACE"}) {
      assertPassed(method, "/api/v1/customers");
    }
    for (final String path : new String[] {
        "/api/v1/auth/login",
        "/api/v1/auth/refresh",
        "/api/v1/auth/register",
        "/api/v1/auth/public/verification",
        "/api/public/portal/track",
        "/api/public/hotels/123/reservations",
        "/internal/whatsapp/webhook/00000000-0000-0000-0000-000000000001"
    }) {
      assertPassed("POST", path);
    }
  }

  @Test
  void requiresMatchingDoubleSubmitTokensForCookieAuthenticatedWrites() throws Exception {
    final MockHttpServletResponse missingResponse = new MockHttpServletResponse();
    final MockFilterChain missingChain = new MockFilterChain();
    filter.doFilter(request("POST", "/api/v1/customers"), missingResponse, missingChain);
    assertEquals(403, missingResponse.getStatus());
    assertNull(missingChain.getRequest());

    final MockHttpServletRequest mismatchRequest = request("PATCH", "/api/v1/customers/123");
    mismatchRequest.setCookies(new jakarta.servlet.http.Cookie("csrf_token", "cookie-value"));
    mismatchRequest.addHeader("X-CSRF-Token", "different-value");
    final MockHttpServletResponse mismatchResponse = new MockHttpServletResponse();
    final MockFilterChain mismatchChain = new MockFilterChain();
    filter.doFilter(mismatchRequest, mismatchResponse, mismatchChain);
    assertEquals(403, mismatchResponse.getStatus());
    assertNull(mismatchChain.getRequest());

    final MockHttpServletRequest matchingRequest = request("DELETE", "/api/v1/customers/123");
    matchingRequest.setCookies(new jakarta.servlet.http.Cookie("csrf_token", "same-value"));
    matchingRequest.addHeader("X-CSRF-Token", "same-value");
    final MockHttpServletResponse matchingResponse = new MockHttpServletResponse();
    final MockFilterChain matchingChain = new MockFilterChain();
    filter.doFilter(matchingRequest, matchingResponse, matchingChain);
    assertEquals(200, matchingResponse.getStatus());
    assertNotNull(matchingChain.getRequest());
  }

  @Test
  void bearerOnlyWritesDoNotUseAmbientCookieCsrfButJwtCookieWritesDo() throws Exception {
    final MockHttpServletRequest bearerRequest = request("POST", "/api/v1/customers");
    bearerRequest.addHeader("Authorization", "Bearer signed-api-token");
    final MockHttpServletResponse bearerResponse = new MockHttpServletResponse();
    final MockFilterChain bearerChain = new MockFilterChain();
    filter.doFilter(bearerRequest, bearerResponse, bearerChain);
    assertEquals(200, bearerResponse.getStatus());
    assertNotNull(bearerChain.getRequest());

    final MockHttpServletRequest mixedRequest = request("POST", "/api/v1/customers");
    mixedRequest.addHeader("Authorization", "Bearer signed-api-token");
    mixedRequest.setCookies(new jakarta.servlet.http.Cookie("jwt", "browser-session"));
    final MockHttpServletResponse mixedResponse = new MockHttpServletResponse();
    final MockFilterChain mixedChain = new MockFilterChain();
    filter.doFilter(mixedRequest, mixedResponse, mixedChain);
    assertEquals(403, mixedResponse.getStatus());
    assertNull(mixedChain.getRequest());
  }

  @Test
  void rejectsBlankAndMalformedBearerCredentialsAsCsrfExemptions() throws Exception {
    for (final String authorization : new String[] {"Bearer ", "Basic token", "Bearer"}) {
      final MockHttpServletRequest request = request("POST", "/api/v1/customers");
      request.addHeader("Authorization", authorization);
      final MockHttpServletResponse response = new MockHttpServletResponse();
      final MockFilterChain chain = new MockFilterChain();
      filter.doFilter(request, response, chain);
      assertEquals(403, response.getStatus(), authorization);
      assertNull(chain.getRequest(), authorization);
    }
  }

  private void assertPassed(final String method, final String path)
      throws ServletException, IOException {
    final MockHttpServletResponse response = new MockHttpServletResponse();
    final MockFilterChain chain = new MockFilterChain();
    filter.doFilter(request(method, path), response, chain);
    assertEquals(200, response.getStatus(), method + " " + path);
    assertNotNull(chain.getRequest(), method + " " + path);
  }

  private static MockHttpServletRequest request(final String method, final String path) {
    final MockHttpServletRequest request = new MockHttpServletRequest(method, path);
    request.setRequestURI(path);
    return request;
  }
}
