package com.hotelpms.monolith.security;

import java.util.HashMap;
import java.util.Arrays;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** Replaces the gateway-to-service HMAC boundary with JWT and double-submit CSRF at the monolith. */
@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
public class MonolithSecurityConfiguration {

  @Bean
  PasswordEncoder passwordEncoder() {
    final Map<String, PasswordEncoder> encoders = new HashMap<>();
    encoders.put("argon2", new Argon2PasswordEncoder(16, 32, 1, 19 * 1024, 2));
    encoders.put("bcrypt", new BCryptPasswordEncoder(12));
    return new DelegatingPasswordEncoder("argon2", encoders);
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource(
      @Value("${GW_CORS_ALLOWED_ORIGINS:http://localhost:5173}") final String configuredOrigins) {
    final CorsConfiguration cors = new CorsConfiguration();
    cors.setAllowedOrigins(
        Arrays.stream(configuredOrigins.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isEmpty())
            .toList());
    cors.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    cors.setAllowedHeaders(
        java.util.List.of(
            HttpHeaders.CONTENT_TYPE,
            HttpHeaders.AUTHORIZATION,
            "X-CSRF-Token",
            "X-Correlation-ID"));
    cors.setAllowCredentials(true);
    cors.setMaxAge(3600L);
    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cors);
    return source;
  }

  @Bean
  FilterRegistrationBean<MonolithJwtAuthenticationFilter> disableContainerJwtFilter(
      final MonolithJwtAuthenticationFilter filter) {
    final FilterRegistrationBean<MonolithJwtAuthenticationFilter> registration =
        new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
  }

  @Bean
  SecurityFilterChain monolithSecurityFilterChain(
      final HttpSecurity http,
      final MonolithJwtAuthenticationFilter jwtFilter,
      final StringRedisTemplate redis,
      @Value("${gateway.security.trust-forwarded-header:false}") final boolean trustProxy)
      throws Exception {
    final MonolithEdgeHeadersFilter edgeHeadersFilter = new MonolithEdgeHeadersFilter(trustProxy);
    final MonolithRateLimitFilter rateLimitFilter = new MonolithRateLimitFilter(redis);
    final MonolithCsrfFilter csrfFilter = new MonolithCsrfFilter();

    http.cors(cors -> {})
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            authorization ->
                authorization
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers(
                        "/api/v1/auth/login",
                        "/api/v1/auth/register",
                        "/api/v1/auth/refresh",
                        "/api/v1/auth/logout",
                        "/api/v1/auth/change-password",
                        "/api/v1/auth/me",
                        "/api/v1/auth/select-branch",
                        "/api/v1/auth/session-security",
                        "/api/v1/auth/account",
                        "/api/v1/auth/public/**",
                        "/api/public/portal/**",
                        "/api/v1/public/requests/**",
                        "/api/public/hotels/*/reservations",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/auth-service/v3/api-docs",
                        "/frontdesk-service/v3/api-docs",
                        "/guest-service/v3/api-docs",
                        "/billing-service/v3/api-docs",
                        "/fb-service/v3/api-docs",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/actuator/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterAfter(edgeHeadersFilter, SecurityContextHolderFilter.class)
        .addFilterAfter(csrfFilter, MonolithEdgeHeadersFilter.class)
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(rateLimitFilter, MonolithJwtAuthenticationFilter.class)
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable);
    return http.build();
  }
}
