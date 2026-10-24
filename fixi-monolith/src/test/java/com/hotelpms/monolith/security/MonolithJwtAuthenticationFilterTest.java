package com.hotelpms.monolith.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hotelpms.auth.domain.HotelRegistry;
import com.hotelpms.auth.domain.Role;
import com.hotelpms.auth.repository.HotelRegistryRepository;
import com.hotelpms.auth.repository.PlatformAdminGrantRepository;
import com.hotelpms.auth.service.JwtService;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

class MonolithJwtAuthenticationFilterTest {
  private static final String USERNAME = "platform-operator";
  private static final UUID TENANT_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");

  private final PlatformAdminGrantRepository grants = mock(PlatformAdminGrantRepository.class);
  private final HotelRegistryRepository hotels = mock(HotelRegistryRepository.class);
  private final JwtService jwt = new JwtService();
  private MonolithJwtAuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(
        jwt,
        "secretKey",
        Base64.getEncoder().encodeToString(
            "fixi-monolith-filter-test-secret-32-bytes".getBytes(StandardCharsets.UTF_8)));
    ReflectionTestUtils.setField(jwt, "jwtExpiration", 900_000L);
    ReflectionTestUtils.setField(jwt, "refreshExpiration", 604_800_000L);
    filter = new MonolithJwtAuthenticationFilter(jwt, grants, hotels);
  }

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void platformRoleClaimWithoutActivePersistedGrantIsRejected() throws ServletException, IOException {
    when(grants.hasActiveGrantForUsername(USERNAME)).thenReturn(false);
    final MockFilterChain chain = new MockFilterChain();

    final MockHttpServletResponse response = invoke(platformToken(true), chain);

    assertEquals(403, response.getStatus());
    assertNull(chain.getRequest());
  }

  @Test
  void forgedIdentityHeadersCannotReplaceMissingSignedPlatformClaim() throws ServletException, IOException {
    when(grants.hasActiveGrantForUsername(USERNAME)).thenReturn(true);
    final MockHttpServletRequest request = request(platformToken(false));
    request.addHeader("X-Auth-Role", "SUPER_ADMIN");
    request.addHeader("X-Auth-Tenant", TENANT_ID.toString());
    final MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new MockFilterChain());

    assertEquals(403, response.getStatus());
  }

  @Test
  void activePersistedGrantAndSignedPlatformClaimAllowGlobalConsole() throws ServletException, IOException {
    when(grants.hasActiveGrantForUsername(USERNAME)).thenReturn(true);
    final MockFilterChain chain = new MockFilterChain();

    final MockHttpServletResponse response = invoke(platformToken(true), chain);

    assertEquals(200, response.getStatus());
    assertNotNull(chain.getRequest());
    assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
        .anyMatch(authority -> "PLATFORM_SUPER_ADMIN".equals(authority.getAuthority())));
    verify(grants).hasActiveGrantForUsername(USERNAME);
  }

  @Test
  void tenantlessGrantedPlatformIdentityCanUseConsoleWithoutTenantHeader()
      throws ServletException, IOException {
    when(grants.hasActiveGrantForUsername(USERNAME)).thenReturn(true);
    final MockFilterChain chain = new MockFilterChain();

    final MockHttpServletResponse response = invoke(tenantlessPlatformToken(), chain);

    assertEquals(200, response.getStatus());
    assertNotNull(chain.getRequest());
    assertNull(chain.getRequest().getHeader("X-Auth-Hotel"));
    assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
        .anyMatch(authority -> "PLATFORM_SUPER_ADMIN".equals(authority.getAuthority())));
  }

  @Test
  void tenantlessPlatformIdentityCannotAccessTenantBusinessRoutes()
      throws ServletException, IOException {
    when(grants.hasActiveGrantForUsername(USERNAME)).thenReturn(true);
    final MockFilterChain chain = new MockFilterChain();

    final MockHttpServletResponse response =
        invoke(tenantlessPlatformToken(), chain, "/api/v1/customers");

    assertEquals(403, response.getStatus());
    assertNull(chain.getRequest());
  }

  @Test
  void existingTenantAccessTokenIsBlockedAfterSuspension() throws ServletException, IOException {
    final HotelRegistry tenant = new HotelRegistry();
    tenant.setStatus("SUSPENDED");
    when(hotels.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
    final MockFilterChain chain = new MockFilterChain();

    final MockHttpServletResponse response = invoke(tenantToken(), chain, "/api/v1/customers");

    assertEquals(403, response.getStatus());
    assertNull(chain.getRequest());
  }

  private String platformToken(final boolean superAdmin) {
    return jwt.generateToken(USERNAME, Role.ADMIN, TENANT_ID, null, 0, false, superAdmin);
  }

  private String tenantToken() {
    return jwt.generateToken(USERNAME, Role.ADMIN, TENANT_ID, null, 0, false, false);
  }

  private String tenantlessPlatformToken() {
    return jwt.generateToken(USERNAME, Role.GUEST, null, null, 0, false, true);
  }

  private MockHttpServletResponse invoke(final String token, final MockFilterChain chain)
      throws ServletException, IOException {
    return invoke(token, chain, "/api/v1/auth/platform/dashboard");
  }

  private MockHttpServletResponse invoke(
      final String token, final MockFilterChain chain, final String path)
      throws ServletException, IOException {
    final MockHttpServletResponse response = new MockHttpServletResponse();
    filter.doFilter(request(token, path), response, chain);
    return response;
  }

  private MockHttpServletRequest request(final String token) {
    return request(token, "/api/v1/auth/platform/dashboard");
  }

  private MockHttpServletRequest request(final String token, final String path) {
    final MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
    request.addHeader("Authorization", "Bearer " + token);
    return request;
  }
}
