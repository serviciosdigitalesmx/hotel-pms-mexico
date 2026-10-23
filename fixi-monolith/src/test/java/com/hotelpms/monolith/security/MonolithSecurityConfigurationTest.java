package com.hotelpms.monolith.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.hotelpms.auth.repository.HotelRegistryRepository;
import com.hotelpms.auth.repository.PlatformAdminGrantRepository;
import com.hotelpms.auth.service.JwtService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;

class MonolithSecurityConfigurationTest {

  private final MonolithSecurityConfiguration configuration = new MonolithSecurityConfiguration();

  @Test
  void usesArgon2ForNewPasswordsAndRetainsLegacyBcryptVerification() {
    final PasswordEncoder encoder = configuration.passwordEncoder();
    final String encoded = encoder.encode("Strong-test-password-123");

    assertTrue(encoded.startsWith("{argon2}"));
    assertTrue(encoder.matches("Strong-test-password-123", encoded));
    assertFalse(encoder.matches("wrong-password", encoded));
    final String legacyHash =
        new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
            .encode("legacy-password");
    assertTrue(encoder.matches("legacy-password", "{bcrypt}" + legacyHash));
  }

  @Test
  void corsAllowsOnlyConfiguredOriginsAndCredentialedApiMethods() {
    final CorsConfiguration cors = configuration
        .corsConfigurationSource("https://app.fixi.example, http://localhost:5173, ,")
        .getCorsConfiguration(new MockHttpServletRequest("GET", "/api/v1/customers"));

    assertEquals(List.of("https://app.fixi.example", "http://localhost:5173"), cors.getAllowedOrigins());
    assertEquals(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"), cors.getAllowedMethods());
    assertTrue(cors.getAllowedHeaders().contains("Authorization"));
    assertTrue(cors.getAllowedHeaders().contains("X-CSRF-Token"));
    assertTrue(cors.getAllowCredentials());
    assertEquals(3600L, cors.getMaxAge());
  }

  @Test
  void disablesServletContainerRegistrationForTheJwtSecurityFilter() {
    final MonolithJwtAuthenticationFilter jwtFilter = new MonolithJwtAuthenticationFilter(
        mock(JwtService.class),
        mock(PlatformAdminGrantRepository.class),
        mock(HotelRegistryRepository.class));

    final FilterRegistrationBean<MonolithJwtAuthenticationFilter> registration =
        configuration.disableContainerJwtFilter(jwtFilter);

    assertFalse(registration.isEnabled());
    assertEquals(jwtFilter, registration.getFilter());
  }
}
